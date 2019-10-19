(ns truth.env.dev
  (:require
   [datomic.client.api :as d]

   [truth.cloud :as cloud]
   [truth.env.prod.data :as data]
   [truth.schema :as schema]
   [truth.search :as search]
   [again.core :as again]
   [truth.rdf :as rdf]
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
                              {:claim/quoting [:source/url]}
                              {:claim/sources [:source/url]}
                              {:claim/evidence [:evidence/supports {:evidence/claim [:claim/slug]}]}])
          :where
          [?claim :claim/id _]
          ]
        db)))

(defn evidence [db]
  (d/q '[:find ?slug ?created-at ?supports ?evidence-slug
         :where
         [?claim :claim/slug ?slug]
         [?claim :claim/evidence ?evidence]
         [?evidence :evidence/created-at ?created-at]
         [?evidence :evidence/claim ?evidence-claim]
         [?evidence :evidence/supports ?supports]
         [?evidence-claim :claim/slug ?evidence-slug]]
       db)

  )

(comment

  (map #(rdf/print-triples (rdf/claim->rdf %)) (all-claims (d/db (get-conn))))


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
