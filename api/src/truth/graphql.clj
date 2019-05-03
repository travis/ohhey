(ns truth.graphql
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [com.walmartlabs.lacinia :refer [execute]]
            [com.walmartlabs.lacinia.util :refer [attach-resolvers]]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.parser.schema :refer [parse-schema]]
            [com.walmartlabs.lacinia.resolve :refer [resolve-as]]
            [com.walmartlabs.lacinia.executor :refer [selects-field? selections-seq selections-tree]]
            [truth.domain :as t
             :refer [get-user-by-email get-all-claims get-contributors get-claim-evidence]]
            [truth.search :as search]
            [datomic.client.api :as d]))

(defn dkey
  [key]
  (fn [context args value]
    (get value key)))

(defn handle-errors [resolver resolver-path]
  (fn [context variables parent]
    (try
      (resolver context variables parent)
      (catch clojure.lang.ExceptionInfo e
        (let [exception-map  (Throwable->map e)]
          (if (= (:db/error (:data exception-map)) :db.error/unique-conflict)
            (do
              (log/debug e (str "caught :db.error/unique-conflict while resolving "resolver-path))
              (resolve-as nil {:message "this mutation attempted to assert a duplicate value"
                               :data {:truth.error/type :truth.error/unique-conflict}}))
            (do
              (println e (str "caught clojure.lang.ExceptionInfo while resolving "resolver-path))
              (log/error e (str "caught clojure.lang.ExceptionInfo while resolving "resolver-path))
              (resolve-as nil {:message "unknown clojure.lang.ExceptionInfo - please contact the administrator"
                               :data {:truth.error/type :truth.error/unknown-exception-info
                                      :truth.error/error e}})))))
      (catch java.util.concurrent.ExecutionException e
        (println e (str "caught execution exception while resolving "resolver-path))
        (log/error e (str "caught execution exception while resolving "resolver-path))
        (resolve-as nil {:message "unknown execution exception - please contact the administrator"
                         :data {:truth.error/type :truth.error/unknown-execution-exception
                                :truth.error/error e}}))
      (catch Throwable t
        (println t (class t) (str "caught error while resolving "resolver-path))
        (log/error t (class t) (str "caught error while resolving "resolver-path))
        (resolve-as nil {:message "unknown error - please contact the administrator"
                         :data {:truth.error/type :truth.error/unknown-error
                                :truth.error/error t}})))))

(defn apply-middleware [resolvers middlewares]
 (update-in resolvers [:resolvers]
            (fn [resolvers]
              (reduce
               (fn [m [datatype property-resolvers]]
                 (assoc m datatype
                        (reduce
                         (fn [n [property resolver]]
                           (assoc n property (reduce (fn [r middleware] (middleware r [datatype property])) resolver middlewares)))
                         {}
                         property-resolvers)))
               {}
               resolvers)
              )))
(defn search-results-of-type [type results]
  {:search/count (count results)
   :search/results
   (map (fn [search-result]
          (assoc search-result
                 :search/result
                 (schema/tag-with-type (:search/result search-result) type)))
        results)})

(def char-limit 255)
(defn reject-long-bodies! [{body :body}]
  (when (< char-limit (count body))
    (throw (ex-info "claim body must be at most 255 characters long" {:body body :count (count body)}))))

(def resolvers
  (->
   {:scalars
    {:Instant
     {:parse (fn [epoch]
               (java.util.Date. epoch))
      :serialize (fn [instant]
                   (try
                     (.getTime instant)
                     (catch Throwable _
                       (log/warn "Error serializing instant " instant)
                       nil)))}}
    :resolvers
    {:Query
     {:healthy
      (fn [context arguments parent] true)
      :currentUser
      (fn [{db :db current-user :current-user} arguments parent]
        current-user)

      :claims
      (fn [{db :db current-user :current-user} arguments parent]
        (t/get-all-claims-as db (:db/id current-user)))

      :searchClaims
      (fn [{db :db search-client :search-client current-user :current-user} {term :term} parent]
        (if term
          (->> (t/search-claims-as db (search-client) (:db/id current-user) term)
               (search-results-of-type :Claim))
          []))

      :suggestClaims
      (fn [{db :db search-client :search-client current-user :current-user} {term :term} parent]
        (if term
          (->> (t/suggest-claims-as db (search-client) (:db/id current-user) term)
               (search-results-of-type :Claim))
          []))

      :claim
      (fn [{db :db current-user :current-user :as context} {slug :slug} parent]
        (if (selects-field? context :Claim/userMeta)
          (t/get-claim-for db
                           [:claim/slug slug]
                           [:user/username (-> (selections-tree context) :Claim/userMeta :args :username)]
                           (:db/id current-user))
          (t/get-claim-as db [:claim/slug slug] (:db/id current-user))))

      :evidenceForClaim
      (fn [{db :db current-user :current-user} {claim-id :claimID username :username} parent]
        (->>
         (if username
           (t/get-claim-evidence-for db [:claim/id claim-id] [:user/username username])
           (t/get-claim-evidence-as db [:claim/id claim-id] (:db/id current-user)))
         (sort-by #(get-in % [:evidence/claim :claim/created-at]))
         reverse))
      }
     :Mutation
     {:logIn
      (fn [{session :session db :db current-user :current-user}
           {username :username password :password} parent]
        (let [user (t/get-user-by-username db username)]
          (if (= (:user/password user) password)
            (do
              (var-set session (assoc @session :identity username))
              user)
            nil)))

      :logOut
      (fn [{session :session}
           vars parent]
        (var-set session (assoc @session :identity nil))
        true)
      :addClaim
      (fn addClaim [{current-user :current-user
                     search-client :search-client transact :transact}
                    {claim-input :claim} parent]
        (let [result
              (transact {:tx-data [`(truth.domain/create-claim!
                                     ~(assoc claim-input :db/id "new-claim" :standalone true)
                                     [:user/username ~(:user/username current-user)])]})
              new-claim (t/get-claim-as (:db-after result)
                                        (-> result :tempids (get "new-claim"))
                                        (:db/id current-user))]
          (search/upload-claims (search-client) [new-claim])
          new-claim))

      :addEvidence
      (fn [{current-user :current-user search-client :search-client transact :transact}
           {claim-id :claimID {id :id :as claim} :claim supports :supports} parent]
        (let [result
              (transact
               {:tx-data
                [`(truth.domain/add-evidence!
                   ~claim-id ~{:claim claim :supports supports :db/id "new-evidence"}
                   ~[:user/username (:user/username current-user)])]})
              new-evidence (t/get-evidence-as (:db-after result)
                                              (-> result :tempids (get "new-evidence"))
                                              (:db/id current-user))]
          (when (not id) (search/upload-claims (search-client) [(:evidence/claim new-evidence)]))
          new-evidence))

      :voteOnClaim
      (fn [{current-user :current-user transact :transact}
           {claim-id :claimID agreement :agreement} parent]
        (let [result
              (transact
               {:tx-data
                [`(truth.domain/vote-on-claim! ~claim-id ~current-user ~agreement)]})]
          (t/get-claim-as (:db-after result) [:claim/id claim-id] (:db/id current-user))))

      :voteOnEvidence
      (fn [{current-user :current-user transact :transact}
           {evidence-id :evidenceID rating :rating} parent]
        (let [result
              (transact
               {:tx-data
                [`(truth.domain/vote-on-evidence! ~evidence-id ~current-user ~rating)]})
              db (:db-after result)
              evidence [:evidence/id evidence-id]
              user (:db/id current-user)]
          (assoc (t/get-evidence-as db evidence user)
                 :parentClaim (t/get-parent-claim-as db evidence user))))
      }

     :User
     {:username (dkey :user/username)
      :email (dkey :user/email)
      }
     :SearchResult
     {:score (dkey :search/score)
      :result (dkey :search/result)
      }
     :SearchResults
     {:totalCount (dkey :search/count)
      :results (dkey :search/results)
      }
     :Claim
     {:id (dkey :claim/id)
      :slug (dkey :claim/slug)
      :body (dkey :claim/body)
      :createdAt (dkey :claim/created-at)
      :supportCount (dkey :support-count)
      :opposeCount (dkey :oppose-count)
      :agreement (dkey :agreement)
      :myAgreement (dkey :my-agreement)
      :agreementCount (dkey :agreement-count)
      :creator (dkey :claim/creator)
      :contributors
      (fn [{db :db} arguments {id :db/id contributors :claim/contributors}]
        (or contributors (get-contributors db id)))
      :evidence
      (fn [{db :db current-user :current-user} a {id :db/id evidence :evidence}]
        (or evidence {:edges (t/get-claim-evidence-as db id (:db/id current-user))}))
      :userMeta
      (fn [{db :db} {username :username} {claim-id :claim/id agreement :user-agreement :as claim}]
        {:id (str claim-id ":" username) :agreement agreement :user {:user/username username}})
      }
     :Evidence
     {:id (dkey :evidence/id)
      :supports (dkey :evidence/supports)
      :claim (dkey :evidence/claim)
      :myRelevanceRating (dkey :my-relevance-rating)
      :parentClaim
      (fn [{conn :conn db :db current-user :current-user}
           {} {evidence-id :evidence/id}]
        (t/get-parent-claim-as (d/db conn) [:evidence/id evidence-id] (:db/id current-user)))

      :userMeta
      (fn [{db :db} {username :username} {evidence-id :evidence/id relevance :user-relevance}]
        {:id (str evidence-id ":" username) :relevance relevance :user {:user/username username}})
      }
     }}
   (apply-middleware [handle-errors])))

(defn load-schema []
  (-> (parse-schema (slurp (clojure.java.io/resource "schema.gql")) resolvers)
      schema/compile
      ))
