(ns truth.resolvers
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.walmartlabs.lacinia :refer [execute]]
            [com.walmartlabs.lacinia.util :refer [attach-resolvers]]
            [com.walmartlabs.lacinia.schema :as schema]
            [truth.core :refer [conn get-user-by-email get-all-claims get-contributors]]
            [datomic.client.api :as d]))

(defn key-factory
  [key]
  (fn [context args value]
    (get value key)))

(def resolvers
  {:Claim/contributors (fn [context arguments user]
                         (or (:claim/contributors user)
                             (get-contributors (d/db conn) user)))
   :current-user (fn [context arguments value]
                   (get-user-by-email (d/db conn) "travis@truth.com"))
   :claims (fn [context arguments value]
             (get-all-claims (d/db conn)))
   :key key-factory})

(def schema
  (-> (with-open [rdr (io/reader (io/resource "schema.edn"))]
        (edn/read-string (slurp rdr)))
      (attach-resolvers resolvers
       )
      schema/compile))


(comment

  (execute schema "{currentUser {username} }" nil nil)
  (execute schema "{claims {body, contributors {username}} }" nil nil)
  )
