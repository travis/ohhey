(ns truth.search
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as io])
  (:import
   (software.amazon.awssdk.core.sync RequestBody)

   ;;https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/services/cloudsearchdomain/package-summary.html
   (software.amazon.awssdk.services.cloudsearchdomain.model UploadDocumentsRequest UploadDocumentsResponse SearchRequest SuggestRequest)
   (software.amazon.awssdk.services.cloudsearchdomain CloudSearchDomainClient )
   ;; https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/services/cloudsearch/package-summary.html
   (software.amazon.awssdk.services.cloudsearch CloudSearchClient)))

(defn mock-search-domain-client []
  (proxy [CloudSearchDomainClient] []
    (uploadDocuments [request body]
      (-> (UploadDocumentsResponse/builder)
          (.adds 0)
          (.deletes 0)
          (.status "success")
          .build))))

(defn search-domains []
  (.domainStatusList (.describeDomains (CloudSearchClient/create))))

(defn search-domain-named [name]
  (some (fn [domain] (when (= (.domainName domain) name) domain)) (search-domains)))

(defn client-for-domain [domain-name]
  (-> (CloudSearchDomainClient/builder)
      (.endpointOverride (java.net.URI. (str "https://" (.endpoint (.docService (search-domain-named domain-name))))))
      (.build)))

(defn upload-documents [client documents]
  (let [response (.uploadDocuments client
                                   (-> (UploadDocumentsRequest/builder)
                                       (.contentType "application/json")
                                       (.build))
                                   (RequestBody/fromString (json/write-str documents)))]
    {:status (.status response)
     :adds (.adds response)
     :deletes (.deletes response)
     :warnings (.warnings response)}))

(defn upload-claims [client claims]
  (upload-documents
   client
   (map (fn [{id :claim/id body :claim/body}]
          {:type "add" :id id :fields {:body body}}) claims)))

(defn delete-claims [client claims]
  (upload-documents
   client
   (map (fn [{id :claim/id}]
          {:type "delete" :id id}) claims)))

(defn delete-ids [client ids]
  (upload-documents
   client
   (map (fn [id]
          {:type "delete" :id id}) ids)))

(defn search [client query]
  (let [response (-> client
                     (.search (-> (SearchRequest/builder)
                                  (.query query)
                                  (.build))))
        hits (.hits response)
        ]
    {:found (.found hits)
     :start (.start hits)
     :hits (map (fn [hit] {:id (.id hit) :fields (.fields hit) :exprs (.exprs hit) :highlights (.highlights hit)})
                (.hit hits))}))

(defn suggest [client query]
  (let [response (.suggest client
                           (-> (SuggestRequest/builder)
                               (.query query)
                               (.suggester "body_suggester")
                               (.build)))
        status (.status response)
        suggest (.suggest response)
        ]
    {:timems (.timems status)
     :rid (.rid status)
     :suggestions (map (fn [suggestion] {:suggestion (.suggestion suggestion)
                                         :score (.score suggestion)
                                         :id (.id suggestion)})
                       (.suggestions suggest))}))

(defn all-ids [client]
  (map :id (:hits (search client "-b0d83906-eac3-4447-ac26-55b59ffdc51d-this-should-never-be-a-real-id"))))

(defn delete-all [client]
  (delete-ids client (all-ids client)))


(comment
  (all-ids test-client)
  (delete-all test-client)

  (.searchService (search-domain-named "ohhey-dev"))

  (.domainStatusList (.describeDomains (CloudSearchClient/create)))

  (.endpoint (.docService (first (.domainStatusList (.describeDomains (CloudSearchClient/create))))))
  (.endpoint (.searchService (first (.domainStatusList (.describeDomains (CloudSearchClient/create))))))

  (def test-client (client-for-domain "ohhey-dev"))
  (search-domain-named "ohhey-dev")
  (upload-documents test-client [{:type "add" :id "test3" :fields {:body "testing body 3"}}])
  (upload-documents test-client [{:type "delete" :id "test2"}])

  (map (fn [hit] (.id hit)) (.hit (search test-client "-awoijfaiowejfa")))

  (-> test-client
      (.suggest (-> (SuggestRequest/builder)
                    (.query "cat")
                    (.suggester "body_suggester")
                    (.build)))
      )

  (map (fn [hit] (.id hit)) (.hit (suggest test-client "-awoijfaiowejfa")))
  (-> test-client
      (.search (-> (SearchRequest/builder)
                   (.query "-awoijfaiowejfa")
                   (.build)))
      (.hits))

  ;; WORKS!
  (let [doc-endpoint (.endpoint (.docService (search-domain-named "ohhey-dev")))
        client (-> (CloudSearchDomainClient/builder)
                   (.endpointOverride (java.net.URI. (str "https://" doc-endpoint)))
                   (.build))]
    (.uploadDocuments client
                      (-> (UploadDocumentsRequest/builder)
                          (.contentType "application/json")
                          (.build))
                      (RequestBody/fromString (json/write-str [{:type "add" :id "test2" :fields {:body "test body 2"}}]))
                      ))


  (let [doc-endpoint (.endpoint (.docService (search-domain-named "ohhey-dev")))
        client (-> (CloudSearchDomainClient/builder)
                   (.endpointOverride (java.net.URI. (str "https://" doc-endpoint)))
                   (.build))]
    (-> client
        (.search (-> (SearchRequest/builder)
                     (.query "test")
                     (.build)))
        (.hits)
        .hit)
    )
p
  (java.net.URI. (.endpoint (.docService (first (.domainStatusList (.describeDomains (CloudSearchClient/create)))))))
  (.endpoint (.searchService (first (.domainStatusList (.describeDomains (CloudSearchClient/create))))))
  )
