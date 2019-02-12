(ns truth.graphql
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.walmartlabs.lacinia :refer [execute]]
            [com.walmartlabs.lacinia.util :refer [attach-resolvers]]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.parser.schema :refer [parse-schema]]
            [truth.domain :as t
             :refer [get-user-by-email get-all-claims get-contributors get-claim-evidence]]
            [datomic.api :as d]))

(defn dkey
  [key]
  (fn [context args value]
    (get value key)))

(def resolvers
  {:resolvers
   {:Query
    {:currentUser
     (fn [{db :db} arguments parent]
       (get-user-by-email db "travis@truth.com"))

     :claims
     (fn [{db :db} arguments parent]
       (get-all-claims db))

     :evidenceForClaim
     (fn [{db :db} {claim-id :claimID} parent]
       (get-claim-evidence db [:claim/id claim-id]))
     }
    :Mutation
    {:addEvidence
     (fn [{db :db} {claim-id :claimID claim :claim supports :supports} parent]
       (-> (t/new-evidence {:supports supports
                            :claim (assoc (t/new-claim claim)
                                          :support-count 0
                                          :oppose-count 0
                                          :agree-count 0
                                          :disagree-count 0)})
           (assoc :relevance 100))
       )
     }

    :User
    {:username (dkey :user/username)}
    :Claim
    {:id (dkey :claim/id)
     :body (dkey :claim/body)
     :supportCount (dkey :support-count)
     :opposeCount (dkey :oppose-count)
     :agreeCount (dkey :agree-count)
     :disagreeCount (dkey :disagree-count)
     :contributors
     (fn [{db :db} arguments {id :db/id contributors :claim/contributors}]
       (or contributors (get-contributors db id)))
     :evidence
     (fn [{db :db} a {id :db/id}]
       {:edges (get-claim-evidence db id)})
     }
    :Evidence
    {:id (dkey :evidence/id)
     :supports (dkey :evidence/supports)
     :claim (dkey :evidence/claim)
     }

}})

(defn load-schema []
  (-> (parse-schema (slurp (clojure.java.io/resource "schema.gql")) resolvers)
      schema/compile))
