(ns truth.graphql-test
  (:require [datomic.api :as d]
            [com.walmartlabs.lacinia :as gql]
            [clojure.test :refer :all]
            [truth.schema :as schema]
            [truth.data :as data]
            [truth.graphql :refer [load-schema]]
            [truth.domain :as t]))

(def uri "datomic:mem://graphql-test-interactive")
(use-fixtures
  :each (fn [run-tests]
          (let [uri (str "datomic:mem://graphql-test-" (t/uuid))]
            (d/create-database uri)
            (let [schema (load-schema)
                  conn (d/connect uri)]
              (schema/load conn)
              (data/load conn)
              (defn execute [query variables]
                (gql/execute schema query variables
                             (let [db (d/db conn)
                                   current-user (t/get-user-by-email db "travis@truth.com")]
                               {:db db
                                :conn conn
                                :current-user current-user})))
              )
            (run-tests)
            (d/delete-database uri)
            )))

(deftest test-currentUser
  (testing "currentUser"
    (is (= {:data {:currentUser {:username "travis"}}}
           (execute "{currentUser {username} }" nil)))))

(deftest test-claims
  (testing "claims"
    (is (= {:data
            {:claims
             [{:body "Dogs are great"}
              {:body "Animals are awesome"}
              {:body "Cats are great"}
              {:body "They don't like people"}
              {:body "A cat was mean to me"}]}}
           (execute "{claims {body } }" nil)))
    (is (= {:data
            {:claims
             [{:body "Dogs are great", :contributors []}
              {:body "Animals are awesome", :contributors []}
              {:body "Cats are great", :contributors [{:username "travis"}]}
              {:body "They don't like people", :contributors []}
              {:body "A cat was mean to me", :contributors []}
              ]}}
           (execute "{claims {body, contributors {username} } }" nil)))
    (is (= {:data
            {:claims
             [{:body "Dogs are great",
               :evidence
               {:edges '({:claim {:body "Animals are awesome"} :supports true})},
               :contributors []}
              {:body "Animals are awesome",
               :evidence {:edges []},
               :contributors []}
              {:body "Cats are great",
               :evidence
               {:edges '({:claim {:body "Animals are awesome"} :supports true}
                         {:claim {:body "They don't like people"} :supports false}
                         {:claim {:body "They don't like people"} :supports true}
                         )},
               :contributors '({:username "travis"})}
              {:body "They don't like people",
               :evidence {:edges [{:claim {:body "A cat was mean to me"} :supports true}]},
               :contributors []}
              {:body "A cat was mean to me",
               :evidence {:edges []},
               :contributors []}
              ]}}
           (execute "{claims {body, contributors {username}, evidence {edges {supports, claim {body}}} } }" nil)))))

(deftest test-evidenceForClaims
  (testing "evidenceForClaims {supports, claim {body}}"
    (is (= {:data {:evidenceForClaim [{:supports true :claim {:body "Animals are awesome" :supportCount 0}}]}}
           (execute "query EvidenceForClaim($claimID: ID) {evidenceForClaim(claimID: $claimID) {supports, claim {body, supportCount}}}" {:claimID "dogs-are-great"})))))

(deftest test-addEvidence
  (testing "addEvidence"
    (is (= {:data
            {:addEvidence
             {:supports true, :claim {:body "SO FRIENDLY!!", :supportCount 0, :creator {:username "travis"}}}}}
           (execute "
mutation($claimID: ID!, $supports: Boolean!, $claim: ClaimInput!) {
  addEvidence(claimID: $claimID, supports: $supports, claim: $claim) {
    supports, claim {
      body, supportCount
      creator { username }
    }
  }
}"
                    {:claimID "dogs-are-great", :supports true, :claim {:body "SO FRIENDLY!!"}})))))

(def vote-query "
mutation VoteOnClaim($claimID: ID!, $agree: Boolean!) {
  voteOnClaim(claimID: $claimID, agree: $agree) {
    agree, disagree
  }
}
")
(deftest test-voteOnClaim
  (testing "voteOnClaim"
    (is (= {:data
            {:voteOnClaim
             {:agree true, :disagree false}}}
           (execute vote-query {:claimID "dogs-are-great", :agree true})))
    (is (= {:data
            {:voteOnClaim
             {:agree false, :disagree true}}}
           (execute vote-query {:claimID "dogs-are-great", :agree false})))
    (is (= {:data
            {:voteOnClaim
             {:agree true, :disagree false}}}
           (execute vote-query {:claimID "animals-are-awesome", :agree true})))))
