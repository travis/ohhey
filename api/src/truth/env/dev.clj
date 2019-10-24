(ns truth.env.dev
  (:require
   [datomic.client.api :as d]

   [truth.cloud :as cloud]
   [truth.env.prod.data :as data]
   [truth.schema :as schema]
   [truth.search :as search]
   [again.core :as again]
   [truth.rdf :as rdf]

   [clojure.data.json :as json]
   ))


(defn db-name []
  "ohhey-dev")

(def db-spec {:db-name (db-name)})
(def search-domain (db-name))

(defn make-client [] (d/client cloud/cfg))
(def client (memoize make-client))
(defn get-conn [] (d/connect (client) db-spec))

(defn make-search-client [] (search/client-for-domain search-domain))
(def search-client (memoize make-search-client))

(defn client-load [conn]
  (d/transact conn {:tx-data data/data})
  )

(defn all-claims [db]
  (apply concat
   (d/q '[:find (pull ?claim [:claim/slug :claim/created-at :claim/body
                              {:claim/quoting [:source/url {:source/book [:book/url]}]}
                              {:claim/sources [:source/url {:source/book [:book/url]}]}
                              {:claim/evidence [:evidence/supports {:evidence/claim [:claim/slug]}]}])
          :where
          [?claim :claim/id _]
          ]
        db)))

(defn all-sources [db]
  (apply concat
         (d/q '[:find (pull ?source [:source/url :source/title
                                     {:source/publication [:publication/url :publication/name]}
                                     {:source/book [:book/url :book/title :book/author]}])
                :where
                [?source :source/id _]
                ]
              db)))


(defn claim->ttl-file [dir claim]
  (binding [*out* (clojure.java.io/writer (str dir (:claim/slug claim)))]
    (rdf/print-triples (rdf/claim->rdf claim))))

(defn claim->ttl [claim]
  (with-out-str
    (rdf/print-triples (rdf/claim->rdf claim))))

(defn source->ttl [source]
  (with-out-str
    (rdf/print-triples (rdf/source->rdf source))))

(comment
  (spit "sources.json"
        (json/write-str
         (apply str (apply concat (map source->ttl (all-sources (d/db (get-conn))))))))


  (spit "claims.json"
   (json/write-str
    (let [claims (all-claims (d/db (get-conn)))]
      (reduce (fn [m claim] (assoc m (:claim/slug claim) (claim->ttl claim)))
              {} claims))))


  ()


  (clojure.core.memoize/memo-clear! client)

  (d/create-database (client) db-spec)

  (schema/client-load (get-conn))

  (client-load (get-conn))
  (d/delete-database (client) db-spec)

  (do
    (d/delete-database (client) db-spec)
    (again/with-retries
      [10000 20000 30000 40000]
      (do
        (clojure.core.memoize/memo-clear! client)
        (d/create-database (client) db-spec)
        (println
         "loaded schema"
         (schema/client-load (get-conn)))
        (println
         "loaded data"
         (client-load (get-conn)))
        )
      )
    )
  )
