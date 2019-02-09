(ns truth.resolvers
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.walmartlabs.lacinia :refer [execute]]
            [com.walmartlabs.lacinia.util :refer [attach-resolvers]]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.parser.schema :refer [parse-schema]]
            [truth.core :refer [conn]]
            [truth.domain :refer [get-user-by-email get-all-claims get-contributors get-evidence-for-claim]]
            [datomic.client.api :as d]))

(defn dkey
  [key]
  (fn [context args value]
    (get value key)))

(def resolvers
  {:resolvers
   {:Query
    {:currentUser
     (fn [context arguments value]
       (get-user-by-email (d/db conn) "travis@truth.com"))

     :claims
     (fn [context arguments value]
       (get-all-claims (d/db conn)))
     }
    :User
    {:username (dkey :user/username)}
    :Claim
    {:body (dkey :claim/body)
     :contributors
     (fn [context arguments claim]
       (or (:claim/contributors claim)
           (get-contributors (d/db conn) claim)))
     :supportingEvidence
     (fn [c a claim]
       {:edges (map (fn [claim] {:node claim}) (get-evidence-for-claim (d/db conn) claim true))})
     :opposingEvidence
     (fn [c a claim]
       {:edges (map (fn [claim] {:node claim}) (get-evidence-for-claim (d/db conn) claim false))})
     }}})

(def schema
  (-> (parse-schema (slurp (clojure.java.io/resource "schema.gql")) resolvers)
      schema/compile))




(comment

  (execute schema "{currentUser {username} }" nil nil)
  (execute schema "{claims {body } }" nil nil)
  (execute schema "{claims {body, contributors {username} } }" nil nil)
  (execute schema "{claims {body, supportingEvidence {edges { node {body}}}, contributors {username} } }" nil nil)

  (get-all-claims db)

  )
