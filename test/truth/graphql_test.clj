(ns truth.graphql-test
  (:require [datomic.client.api :as d]
            [com.walmartlabs.lacinia :refer [execute]]
            [clojure.test :refer :all]
            [truth.schema :as schema]
            [truth.data :as data]
            [truth.graphql :refer [schema]]

            [truth.core :refer [conn]]))

(use-fixtures
  :once (fn [run-tests]
          (schema/load conn)
          (data/load conn)
          (def fresh-db (d/db conn))

          (run-tests)))

(deftest test-schema
  (testing "currentUser"
    (is (= {:data {:currentUser {:username "travis"}}}
           (execute schema "{currentUser {username} }" nil nil))))
  (testing "claims"
    (is (= {:data
            {:claims
             [{:body "Dogs are great"}
              {:body "They have cute paws"}
              {:body "Cats are great"}
              {:body "They don't like people"}]}}
           (execute schema "{claims {body } }" nil nil)))
    (is (= {:data
            {:claims
             [{:body "Dogs are great", :contributors []}
              {:body "They have cute paws", :contributors []}
              {:body "Cats are great", :contributors [{:username "travis"}]}
              {:body "They don't like people", :contributors []}]}}
           (execute schema "{claims {body, contributors {username} } }" nil nil)))
    (is (= {:data
            {:claims
             [{:body "Dogs are great",
               :supportingEvidence
               {:edges '({:node {:body "They have cute paws"}})},
               :contributors []}
              {:body "They have cute paws",
               :supportingEvidence {:edges []},
               :contributors []}
              {:body "Cats are great",
               :supportingEvidence
               {:edges '({:node {:body "They have cute paws"}})},
               :contributors '({:username "travis"})}
              {:body "They don't like people",
               :supportingEvidence {:edges []},
               :contributors []}]}}
           (execute schema "{claims {body, supportingEvidence {edges { node {body}}}, contributors {username} } }" nil nil)))
    (is (= {:data
            {:claims
             [{:body "Dogs are great",
               :opposingEvidence {:edges []},
               :contributors []}
              {:body "They have cute paws",
               :opposingEvidence {:edges []},
               :contributors []}
              {:body "Cats are great",
               :opposingEvidence
               {:edges [{:node {:body "They don't like people"}}]},
               :contributors [{:username "travis"}]}
              {:body "They don't like people",
               :opposingEvidence {:edges []},
               :contributors []}]}}
           (execute schema "{claims {body, opposingEvidence {edges { node {body}}}, contributors {username} } }" nil nil)))))
