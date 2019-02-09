(ns truth.graphql
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.walmartlabs.lacinia :refer [execute]]
            [com.walmartlabs.lacinia.util :refer [attach-resolvers]]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.parser.schema :refer [parse-schema]]
            [truth.domain :refer [get-user-by-email get-all-claims get-contributors get-evidence-for-claim]]
            [datomic.api :as d]))

(defn dkey
  [key]
  (fn [context args value]
    (get value key)))

(def resolvers
  {:resolvers
   {:Query
    {:currentUser
     (fn [{db :db} arguments query]
       (get-user-by-email db "travis@truth.com"))

     :claims
     (fn [{db :db} arguments query]
       (get-all-claims db))
     }
    :User
    {:username (dkey :user/username)}
    :Claim
    {:body (dkey :claim/body)
     :contributors
     (fn [{db :db} arguments {id :db/id contributors :claim/contributors}]
       (or contributors (get-contributors db id)))
     :supportingEvidence
     (fn [{db :db} a {id :db/id}]
       {:edges (map (fn [claim] {:node claim}) (get-evidence-for-claim db id [true]))})
     :opposingEvidence
     (fn [{db :db} a {id :db/id}]
       {:edges (map (fn [claim] {:node claim}) (get-evidence-for-claim db id [false]))})
     }}})

(def schema
  (-> (parse-schema (slurp (clojure.java.io/resource "schema.gql")) resolvers)
      schema/compile))
