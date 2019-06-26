(ns truth.env.prod
  (:require
   [datomic.client.api :as d]

   [truth.cloud :as cloud]
   [truth.env.prod.data :as data]
   [truth.schema :as schema]
   [truth.search :as search]
   [again.core :as again]
   ))


(defn db-name []
  "ohhey-prod")

(def db-spec {:db-name (db-name)})
(def search-domain (db-name))

(defn make-client [] (d/client cloud/cfg))
(def client (memoize make-client))
(defn get-conn [] (d/connect (client) db-spec))

(defn make-search-client [] (search/client-for-domain search-domain))
(def search-client (memoize make-search-client))

(defn client-load [conn]
  (d/transact conn {:tx-data data/data}))

(comment

  (d/transact (get-conn)
              {:tx-data [{:db/id [:claim/slug "god-exists"]
                          :claim/featured true}]})


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
