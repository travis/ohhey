(ns truth.graphql-mutation-test
  (:require [datomic.client.api :as d]
            [com.walmartlabs.lacinia :as gql]
            [clojure.test :refer :all]
            [expectations.clojure.test :refer :all]
            [truth.schema :as schema]
            [truth.data :as data]
            [truth.graphql :refer [load-schema]]
            [truth.domain :as t]
            [truth.cloud :as cloud]
            [truth.search :as search]))

(use-fixtures
  :once (fn [run-tests]
          (def schema (load-schema))
          (def client (d/client cloud/cfg))
          (def db-spec {:db-name "test"})
          (def conn (d/connect client db-spec))
          (def fresh-db (d/with-db conn))
          (defn execute
            ([query variables] (execute query variables fresh-db "travis"))
            ([query variables db] (execute query variables db "travis"))
            ([query variables db current-username]
             (with-local-vars [latest-db db]
               (->
                (gql/execute schema query variables
                             (let [current-user (t/get-user-by-username @latest-db current-username)]
                               {:db @latest-db
                                :conn conn
                                :transact (fn [arg-map]
                                            (let [result (d/with @latest-db arg-map)]
                                              (var-set latest-db (:db-after result))
                                              result))
                                :current-user current-user
                                :search-client search/mock-search-domain-client}))
                (assoc :latest-db @latest-db)))))
          (run-tests)))

(def add-evidence-mutation "
mutation($claimID: ID!, $supports: Boolean!, $claim: ClaimInput!) {
  addEvidence(claimID: $claimID, supports: $supports, claim: $claim) {
    supports, claim {
      body, supportCount
      creator { username }
    }
  }
}")

(deftest test-addEvidence
  (testing "adding a new claim"
    (is (=
         {:addEvidence
          {:supports true, :claim {:body "SO FRIENDLY!!", :supportCount 0, :creator {:username "travis"}}}}
         (:data (execute add-evidence-mutation
                         {:claimID (str (data/ids :dogs-are-great)), :supports true, :claim {:body "SO FRIENDLY!!"}})))))
  (testing "adding an existing claim"
    (is (=
         {:addEvidence
          {:supports false, :claim {:body "They don't like people", :supportCount 1, :creator {:username "travis"}}}}
         (:data (execute add-evidence-mutation
                         {:claimID (str (data/ids :dogs-are-great)), :supports false,
                          :claim {:id (str (data/ids :dont-like-people))}}))))))

(def vote-query "
mutation VoteOnClaim($claimID: ID!, $agreement: Int!) {
  voteOnClaim(claimID: $claimID, agreement: $agreement) {
    myAgreement
  }
}
")
(deftest test-voteOnClaim
  (testing "voteOnClaim"
    (is (=
         {:voteOnClaim
          {:myAgreement 100}}
         (:data (execute vote-query {:claimID (str (data/ids :dogs-are-great)), :agreement 100}))))
    (is (=
         {:voteOnClaim
          {:myAgreement -100}}
         (:data (execute vote-query {:claimID (str (data/ids :dogs-are-great)), :agreement -100}))))
    (is (=
         {:voteOnClaim
          {:myAgreement 100}}
         (:data (execute vote-query {:claimID (str (data/ids :animals-are-awesome)), :agreement 100}))))))

(def evidence-vote-query "
  mutation VoteOnEvidence($evidenceID: ID!, $rating: Int!) {
    voteOnEvidence(evidenceID: $evidenceID, rating: $rating) {
      relevance, myRelevanceRating
    }
  }
  ")

(deftest test-voteOnEvidence
  (testing "voteOnEvidence"
    (is (=
         {:voteOnEvidence
          {:myRelevanceRating 66, :relevance 49.5}}
         (:data (execute evidence-vote-query { :rating 66, :evidenceID (str (data/ids :ara-supports-dag))}))))
    (is (=
         {:voteOnEvidence
          {:myRelevanceRating 33, :relevance 33.0}}
         (:data (execute evidence-vote-query {:rating 33, :evidenceID (str (data/ids :ara-supports-dag))}))))
    (is (=
         {:voteOnEvidence
          {:myRelevanceRating 66, :relevance 66.33333333333333}}
         (:data (execute evidence-vote-query {:rating 66, :evidenceID (str (data/ids :ara-supports-dag))}
                         fresh-db "toby"))))))

(def add-claim-mutation "
mutation AddClaim($claim: ClaimInput!) {
  addClaim(claim: $claim) {
    body
    slug
    creator {
      username
    }
  }
}")

(deftest addClaim
  (testing "happy path"
    (is (= {:addClaim {:body "this test will pass!"
                       :slug "this-test-will-pass"
                       :creator {:username "travis"}}}
           (:data (execute add-claim-mutation {:claim {:body "this test will pass!"}})))))
  (testing "slug uniqueness"
    (is (= {:truth.error/type :truth.error/unique-conflict}
           (->
            (let [new-db (:latest-db (execute add-claim-mutation {:claim {:body "this mutation should return errors"}}))]

              (execute add-claim-mutation {:claim {:body "this mutation should return errors"}} new-db))
            :errors first :extensions :data
            )))))
