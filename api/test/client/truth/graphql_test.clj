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
  :once (fn [run-tests]
          (def schema (load-schema))
          (def client (d/client cloud/cfg))
          (def db-spec {:db-name "test"})
          (def conn (d/connect client db-spec))
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
                               :search-client search/mock-search-domain-client})))))
          (run-tests)))

(deftest test-currentUser
  (testing "currentUser"
    (is (= {:data {:currentUser {:username "travis"}}}
           (execute "{currentUser {username} }" nil)))))

(deftest test-claim
  (testing "claim"
    (is (= {:data
            {:claim
             {:body "Dogs are great"}}}
           (execute "{claim(slug: \"dogs-are-great\") {body}}" nil))))
  (testing "claim with user metadata"
    (is (= {:data
            {:claim
             {:body "Dogs are great", :userMeta {:agreement 100}}}}
           (execute "{claim(slug: \"dogs-are-great\") { body, userMeta(username: \"travis\") { agreement }}}" nil)))))

(deftest test-claims
  (testing "claims"
    (is (= {:data
            {:claims
             [{:body "Dogs are great"}
              {:body "Animals are awesome"}
              {:body "Cats are great"}]}}
           (execute "{claims {body } }" nil)))
    (is (= {:data
            {:claims
             [{:body "Dogs are great", :contributors []}
              {:body "Animals are awesome", :contributors []}
              {:body "Cats are great", :contributors [{:username "travis"}]}]}}
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
               :contributors '({:username "travis"})}]}}
           (execute "{claims {body, contributors {username}, evidence {edges {supports, claim {body}}} } }" nil)))
    (is (= {:data
            {:claims
             [{:body "Dogs are great"}]}}
           (execute "{claims(featured: true) { body } }" nil)))))

(deftest test-evidenceForClaim
  (testing "evidenceForClaims {supports, claim {body}}"
    (is (= {:data {:evidenceForClaim [{:supports true :claim {:body "Animals are awesome" :supportCount 0}}]}}
           (execute "query EvidenceForClaim($claimID: ID!) {evidenceForClaim(claimID: $claimID) {supports, claim {body, supportCount}}}" {:claimID (data/ids :dogs-are-great)}))))
  (testing "evidenceForClaim(username: \"travis\")"
    (is (= {:data
            {:evidenceForClaim
             [{:supports true :claim {:body "Animals are awesome"}
               :userMeta {:relevance 100.0}}]}}
           (execute "
query UserEvidenceForClaim($username: String!, $claimID: ID!) {
  evidenceForClaim(username: $username, claimID: $claimID) {
    supports
    claim { body }
    userMeta(username: $username) { relevance }
  }
}"
                    {:username "travis" :claimID (data/ids :dogs-are-great)})))))

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
