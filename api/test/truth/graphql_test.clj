(ns truth.graphql-test
  (:require [datomic.api :as d]
            [com.walmartlabs.lacinia :as gql]
            [clojure.test :refer :all]
            [truth.schema :as schema]
            [truth.data :as data]
            [truth.graphql :refer [load-schema]]))

(def schema (load-schema))

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
               :evidence {:edges [{:claim {:body "A cat was mean to me"} :supports true}]},
               :contributors []}
              {:body "A cat was mean to me",
               :evidence {:edges []},
               :contributors []}
              {:body "Dogs are great",
               :evidence
               {:edges '({:claim {:body "They have cute paws"} :supports true})},
               :contributors []}
              {:body "They have cute paws",
               :evidence {:edges []},
               :contributors []}
              {:body "Cats are great",
               :evidence
               {:edges '({:claim {:body "They have cute paws"} :supports true}
                         {:claim {:body "They don't like people"} :supports false}
                         {:claim {:body "They don't like people"} :supports true}
                         )},
               :contributors '({:username "travis"})}
              ]}}
           (execute "{claims {body, contributors {username}, evidence {edges {supports, claim {body}}} } }" nil)))
    ))
