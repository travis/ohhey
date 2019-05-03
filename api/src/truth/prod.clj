(ns truth.ions
  (:require
   [datomic.client.api :as d]

   [truth.cloud :as cloud]
   [truth.prod.data :as data]
   [truth.schema :as schema]
   [truth.search :as search]
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
  (d/transact conn {:tx-data data/users})
  (d/transact conn {:tx-data data/claims}))

(comment
  (clojure.core.memoize/memo-clear! client)

  (d/create-database (client) db-spec)

  (schema/client-load (get-conn))

  (client-load (get-conn))
  (d/delete-database (client) db-spec)


  )
