(ns truth.graphql-test
  (:require [datomic.api :as d]
            [com.walmartlabs.lacinia :as gql]
            [clojure.test :refer :all]
            [truth.schema :as schema]
            [truth.data :as data]
            [truth.graphql :refer [schema]]))

(use-fixtures
  :once (fn [run-tests]
          (let [uri "datomic:mem://graphql-test"]
            (d/create-database uri)
            (let [conn (d/connect uri)]
              (schema/load conn)
              (data/load conn)
              (defn execute [query variables]
                (gql/execute schema query variables
                             {:db (d/db conn)}))
              ))
          (run-tests)
          ))

(deftest test-schema
  (testing "currentUser"
    (is (= {:data {:currentUser {:username "travis"}}}
           (execute "{currentUser {username} }" nil))))
  (testing "claims"
    (is (= {:data
            {:claims
             [{:body "They don't like people"}
              {:body "A cat was mean to me"}
              {:body "Dogs are great"}
              {:body "They have cute paws"}
              {:body "Cats are great"}]}}
           (execute "{claims {body } }" nil)))
    (is (= {:data
            {:claims
             [{:body "They don't like people", :contributors []}
              {:body "A cat was mean to me", :contributors []}
              {:body "Dogs are great", :contributors []}
              {:body "They have cute paws", :contributors []}
              {:body "Cats are great", :contributors [{:username "travis"}]}
              ]}}
           (execute "{claims {body, contributors {username} } }" nil)))
    (is (= {:data
            {:claims
             [{:body "They don't like people",
               :supportingEvidence {:edges [{:node {:body "A cat was mean to me"}}]},
               :contributors []}
              {:body "A cat was mean to me",
               :supportingEvidence {:edges []},
               :contributors []}
              {:body "Dogs are great",
               :supportingEvidence
               {:edges '({:node {:body "They have cute paws"}})},
               :contributors []}
              {:body "They have cute paws",
               :supportingEvidence {:edges []},
               :contributors []}
              {:body "Cats are great",
               :supportingEvidence
               {:edges '({:node {:body "They don't like people"}}
                         {:node {:body "They have cute paws"}})},
               :contributors '({:username "travis"})}
              ]}}
           (execute "{claims {body, supportingEvidence {edges { node {body}}}, contributors {username} } }" nil)))
    (is (= {:data
            {:claims
             [{:body "They don't like people",
               :opposingEvidence {:edges []},
               :contributors []}
              {:body "A cat was mean to me",
               :opposingEvidence {:edges []},
               :contributors []}
              {:body "Dogs are great",
               :opposingEvidence {:edges []},
               :contributors []}
              {:body "They have cute paws",
               :opposingEvidence {:edges []},
               :contributors []}
              {:body "Cats are great",
               :opposingEvidence
               {:edges [{:node {:body "They don't like people"}}]},
               :contributors [{:username "travis"}]}
              ]}}
           (execute "{claims {body, opposingEvidence {edges { node {body}}}, contributors {username} } }" nil)))))
