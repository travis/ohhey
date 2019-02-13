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
     (fn [{db :db current-user :current-user} arguments parent]
       current-user)

     :claims
     (fn [{db :db current-user :current-user} arguments parent]
       (println (t/get-all-claims-as db (:db/id current-user)))
       (t/get-all-claims-as db (:db/id current-user)))

     :evidenceForClaim
     (fn [{db :db current-user :current-user} {claim-id :claimID} parent]
       (t/get-claim-evidence-as db [:claim/id claim-id] (:db/id current-user)))
     }
    :Mutation
    {:addEvidence
     (fn [{conn :conn db :db current-user :current-user}
          {claim-id :claimID claim :claim supports :supports} parent]
       (let [creator [:user/email (:user/email current-user)]
             evidence (t/new-evidence {:supports supports
                                       :creator creator
                                       :claim (t/new-claim
                                               (assoc claim :creator creator))})]
         @(d/transact
           conn
           [{:claim/id claim-id
             :claim/evidence evidence}]))
       (-> (t/new-evidence {:supports supports
                            :claim (assoc (t/new-claim (assoc claim
                                                              :creator current-user))
                                          :support-count 0
                                          :oppose-count 0
                                          :agree-count 0
                                          :disagree-count 0)})
           (assoc :relevance 100))
       )
     :voteOnClaim
     (fn [{conn :conn db :db current-user :current-user}
          {claim-id :claimID agree :agree} parent]
       @(d/transact
         conn
         [(if-let [vote-id (t/get-vote-for-user-and-claim db (:db/id current-user) [:claim/id claim-id])]
            {:db/id vote-id :claim-vote/agree agree}
            {:claim/id claim-id
             :claim/votes (t/new-claim-vote
                           {:voter (:db/id current-user)
                            :agree agree})})])
       (t/get-claim-as (d/db conn) [:claim/id claim-id] (:db/id current-user)))
     }

    :User
    {:username (dkey :user/username)
     :email (dkey :user/email)
     }
    :Claim
    {:id (dkey :claim/id)
     :body (dkey :claim/body)
     :supportCount (dkey :support-count)
     :opposeCount (dkey :oppose-count)
     :agreeCount (dkey :agree-count)
     :disagreeCount (dkey :disagree-count)
     :creator (dkey :claim/creator)
     :contributors
     (fn [{db :db} arguments {id :db/id contributors :claim/contributors}]
       (or contributors (get-contributors db id)))
     :evidence
     (fn [{db :db current-user :current-user} a {id :db/id}]
       {:edges (t/get-claim-evidence-as db id (:db/id current-user))})
     }
    :Evidence
    {:id (dkey :evidence/id)
     :supports (dkey :evidence/supports)
     :claim (dkey :evidence/claim)
     :myRelevanceRating (dkey :my-relevance-rating)
     }}})

(defn load-schema []
  (-> (parse-schema (slurp (clojure.java.io/resource "schema.gql")) resolvers)
      schema/compile))
