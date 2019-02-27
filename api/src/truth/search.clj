(ns truth.search
  (:require
   ;; https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudsearchv2/AmazonCloudSearchClient.html
   [amazonica.aws.cloudsearchv2 :as cs]
   ;; https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudsearchdomain/AmazonCloudSearchDomainClient.html
   [amazonica.aws.cloudsearchdomain :as csd]
   [clojure.data.json :as json]
   [clojure.java.io :as io]))

(defn make-creds [base-creds domain]
  (let [{{search :endpoint} :search-service
         {doc :endpoint} :doc-service}
        (first (filter (fn [{domain-name :domain-name}] (= domain-name domain))
                       (:domain-status-list (cs/describe-domains base-creds))))]
    {:doc (assoc base-creds :endpoint (str "http://"doc))
     :search (assoc base-creds :endpoint (str "http://"search))}))

(defn upload-documents [doc-creds documents]
  (csd/upload-documents
   doc-creds
   :content-type "application/json"
   :documents (io/input-stream (.getBytes (json/write-str documents)))))

(defn upload-claims [doc-creds claims]
  (upload-documents
   doc-creds
   (map (fn [{id :claim/id body :claim/body}]
          {:type "add" :id id :fields {:body body}}) claims)))

(defn delete-claims [doc-creds claims]
  (upload-documents
   doc-creds
   (map (fn [{id :claim/id body :claim/body}]
          {:type "delete" :id id}) claims)))

(defn search [search-creds query]
  (csd/search search-creds :query query))

(defn suggest [search-creds query]
  (csd/suggest search-creds
               :query query
               :suggester "body_suggester"))

(comment


  (def base-creds {:profile "ohhey"})
  (def domain "ohhey-dev")

  (cs/create-domain base-creds :domain-name domain)
  (cs/index-documents base-creds :domain-name domain)
  (cs/delete-domain base-creds :domain-name domain)

  (def ohhey-dev-creds (make-creds base-creds domain))
  (def doc-creds (:doc ohhey-dev-creds))
  (def search-creds (:search ohhey-dev-creds :suggestor))


  (cs/define-index-field creds
    :domain-name domain
    :index-field {:index-field-name "body" :index-field-type "text"})

  (csd/upload-documents
   doc-creds
   :content-type "application/json"
   :documents (io/input-stream
               (.getBytes
                (json/write-str [{:type "add"
                                  :id "fuz"
                                  :fields {:body "bang"}}]))))

  (upload-documents)
  (csd/search search-creds :query "bang")

  )
