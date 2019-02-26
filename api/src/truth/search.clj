(ns truth.search
  (:require
   ;; https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudsearchv2/AmazonCloudSearchClient.html
   [amazonica.aws.cloudsearchv2 :as cs]
   ;; https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudsearchdomain/AmazonCloudSearchDomainClient.html
   [amazonica.aws.cloudsearchdomain :as csd]
   [clojure.data.json :as json]
   [clojure.java.io :as io]))

(def creds {:profile "ohhey"})

(defn make-creds [base-creds domain]
  (let [{{search :endpoint} :search-service
         {doc :endpoint} :doc-service}
        (first (filter (fn [{domain-name :domain-name}] (= domain-name domain))
                       (:domain-status-list (cs/describe-domains base-creds))))]
    {:doc (assoc base-creds :endpoint (str "http://"doc))
     :search (assoc base-creds :endpoint (str "http://"search))}))

(comment

  (def hams-creds (make-creds creds "hams"))
  (def doc-creds (:doc hams-creds))
  (def search-creds (:search hams-creds))

 (cs/create-domain creds :domain-name "hams")
 (cs/index-documents creds :domain-name "hams")
 (cs/delete-domain creds :domain-name "hams")

 (cs/define-index-field creds
   :domain-name "hams"
   :index-field {:index-field-name "body" :index-field-type "text"})



 (csd/upload-documents
  doc-creds
  :content-type "application/json"
  :documents (io/input-stream
              (.getBytes
               (json/write-str [{:type "add"
                                 :id "fuz"
                                 :fields {
                                          :body "bang"
                                          }}]))
              ))
 (csd/search search-creds :query "bang")

 )
