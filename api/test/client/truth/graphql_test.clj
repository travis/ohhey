(ns truth.graphql-test
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
  :each (fn [run-tests]
          (let [client (d/client cloud/cfg)
                db-spec {:db-name (str "graphql-test-" (t/uuid))}]

            (d/create-database client db-spec)
            (let [schema (load-schema)
                  conn (d/connect client db-spec)
                  search-client (search/mock-search-domain-client)]
              (schema/client-load conn)
              (data/client-load conn)
              (defn execute
                ([query variables] (execute query variables "travis"))
                ([query variables current-username]
                 (gql/execute schema query variables
                              (do
                                (def db (d/db conn))
                                (let [current-user (t/get-user-by-username db current-username)]
                                  {:db db
                                   :conn conn
                                   :current-user current-user
                                   :search-client search-client})))))
              (run-tests)
              (d/delete-database client db-spec)))
          ))

(deftest test-currentUser
  (testing "currentUser"
    (is (= {:data {:currentUser {:username "travis"}}}
           (execute "{currentUser {username} }" nil)))))

(deftest test-claims
  (testing "claims"
    (is (= {:data
            {:claims
             [{:body "Dogs are great"}
              {:body "They don't like people"}
              {:body "Animals are awesome"}
              {:body "A cat was mean to me"}
              {:body "Cats are great"}]}}
           (execute "{claims {body } }" nil)))
    (is (= {:data
            {:claims
             [{:body "Dogs are great", :contributors []}
              {:body "They don't like people", :contributors []}
              {:body "Animals are awesome", :contributors []}
              {:body "A cat was mean to me", :contributors []}
              {:body "Cats are great", :contributors [{:username "travis"}]}]}}
           (execute "{claims {body, contributors {username} } }" nil)))
    (is (= {:data
            {:claims
             [{:body "Dogs are great",
               :evidence
               {:edges '({:claim {:body "Animals are awesome"} :supports true})},
               :contributors []}
              {:body "They don't like people",
               :evidence {:edges [{:claim {:body "A cat was mean to me"} :supports true}]},
               :contributors []}
              {:body "Animals are awesome",
               :evidence {:edges []},
               :contributors []}
              {:body "A cat was mean to me",
               :evidence {:edges []},
               :contributors []}
              {:body "Cats are great",
               :evidence
               {:edges '({:claim {:body "Animals are awesome"} :supports true}
                         {:claim {:body "They don't like people"} :supports false}
                         {:claim {:body "They don't like people"} :supports true}
                         )},
               :contributors '({:username "travis"})}
              ]}}
           (execute "{claims {body, contributors {username}, evidence {edges {supports, claim {body}}} } }" nil)))))

(deftest test-evidenceForClaims
  (testing "evidenceForClaims {supports, claim {body}}"
    (is (= {:data {:evidenceForClaim [{:supports true :claim {:body "Animals are awesome" :supportCount 0}}]}}
           (execute "query EvidenceForClaim($claimID: ID) {evidenceForClaim(claimID: $claimID) {supports, claim {body, supportCount}}}" {:claimID "dogs-are-great"})))))

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
    (is (= {:data
            {:addEvidence
             {:supports true, :claim {:body "SO FRIENDLY!!", :supportCount 0, :creator {:username "travis"}}}}}
           (execute add-evidence-mutation
                    {:claimID "dogs-are-great", :supports true, :claim {:body "SO FRIENDLY!!"}}))))
  (testing "adding an existing claim"
    (is (= {:data
            {:addEvidence
             {:supports false, :claim {:body "They don't like people", :supportCount 1, :creator {:username "travis"}}}}}
           (execute add-evidence-mutation
                    {:claimID "dogs-are-great", :supports false, :claim {:id "dont-like-people"}})))))

(def vote-query "
mutation VoteOnClaim($claimID: ID!, $agreement: Int!) {
  voteOnClaim(claimID: $claimID, agreement: $agreement) {
    myAgreement
  }
}
")
(deftest test-voteOnClaim
  (testing "voteOnClaim"
    (is (= {:data
            {:voteOnClaim
             {:myAgreement 100}}}
           (execute vote-query {:claimID "dogs-are-great", :agreement 100})))
    (is (= {:data
            {:voteOnClaim
             {:myAgreement -100}}}
           (execute vote-query {:claimID "dogs-are-great", :agreement -100})))
    (is (= {:data
            {:voteOnClaim
             {:myAgreement 100}}}
           (execute vote-query {:claimID "animals-are-awesome", :agreement 100})))))

(def evidence-vote-query "
  mutation VoteOnEvidence($evidenceID: ID!, $rating: Int!) {
    voteOnEvidence(evidenceID: $evidenceID, rating: $rating) {
      relevance, myRelevanceRating
    }
  }
  ")

(deftest test-voteOnEvidence
  (testing "voteOnEvidence"
    (is (= {:data
            {:voteOnEvidence
             {:myRelevanceRating 66, :relevance 49.5}}}
           (execute evidence-vote-query { :rating 66, :evidenceID "ara-supports-dag"})))
    (is (= {:data
            {:voteOnEvidence
             {:myRelevanceRating 33, :relevance 33.0}}}
           (execute evidence-vote-query {:rating 33, :evidenceID "ara-supports-dag"})))
    (is (= {:data
            {:voteOnEvidence
             {:myRelevanceRating 66, :relevance 44.0}}}
           (execute evidence-vote-query {:rating 66, :evidenceID "ara-supports-dag"}
                    "toby")))))

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
    (is (= {:data {:addClaim {:body "this test will pass!"
                              :slug "this-test-will-pass"
                              :creator {:username "travis"}}}}
           (execute add-claim-mutation {:claim {:body "this test will pass!"}}))))
  (testing "slug uniqueness"
    (is (= {:truth.error/type :truth.error/unique-conflict}
           (->
            (do
              (execute add-claim-mutation {:claim {:body "this mutation should return errors"}})
              (execute add-claim-mutation {:claim {:body "this mutation should return errors"}}))
            :errors first :extensions :data
            )))))

(def search-query "
query SearchClaims($term: String!) {
  searchClaims(term: $term) {
    totalCount
    results {
      score
      result {
      __typename
        ... on Claim {
          body
        }
      }
    }
  }
}")

;; TODO: revive once search is using cloudsearch
#_(deftest searchClaims
  (is (= {:data {:searchClaims
                 {:totalCount 2
                  :results
                  [{:score 1.0
                    :result {:__typename :Claim
                             :body "Dogs are great"}}
                   {:score 1.0
                    :result {:__typename :Claim
                             :body "Cats are great"}}
                   ]}}}
         (execute search-query {:term "are great"}))))
