(ns truth.domain-test
  (:require [datomic.api :as d]
            [clojure.test :refer :all]
            [truth.schema :as schema]
            [truth.data :as data]
            [truth.domain :refer [uuid get-user-by-email get-all-claims get-evidence-for-claim]]
            ))

(use-fixtures
  :once (fn [run-tests]
          (let [uri "datomic:mem://domain-test"]
            (d/create-database uri)
            (let [conn (d/connect uri)]
              (schema/load conn)
             (data/load conn)
             (def fresh-db (d/db conn))

             (run-tests)))
          ))

(defn dissoc-ids [map]
  (dissoc map :db/id :user/id :claim/id :claim-vote/id :evidence/id :relevance-vote/id))

(deftest test-get-user-by-email
  (testing "it returns travis"
    (is (= {:user/username "travis", :user/email "travis@truth.com"}
           (dissoc-ids (get-user-by-email fresh-db "travis@truth.com"))))))


(deftest test-get-all-claims
  (testing "it returns all claims"
    (is (= [#:claim{:body "Dogs are great",
                    :creator #:user{:username "travis"},
                    :votes
                    [#:claim-vote{:agree true,
                                  :voter #:user{:username "travis"}}
                     #:claim-vote{:agree false,
                                  :voter #:user{:username "james"}}]
                    :evidence
                    [#:evidence{:supports true,
                                :claim #:claim{:body "They have cute paws"}}]}
            #:claim{:body "They have cute paws",
                    :creator #:user{:username "travis"}}
            #:claim{:body "Cats are great",
                    :creator #:user{:username "james"},
                    :contributors [#:user{:username "travis"}],
                    :votes
                    [#:claim-vote{:agree false,
                                  :voter #:user{:username "travis"}}
                     #:claim-vote{:agree true,
                                  :voter #:user{:username "james"}}]
                    :evidence
                    [#:evidence{:supports true,
                                :claim #:claim{:body "They have cute paws"}}
                     #:evidence{:supports false,
                                :claim
                                #:claim{:body "They don't like people"}}]}
            #:claim{:body "They don't like people",
                    :creator #:user{:username "travis"}}]
           (map dissoc-ids (get-all-claims fresh-db))))))

(deftest test-get-evidence-for-claims
  (testing "about dogs"
   (is (= [#:claim{:body "They have cute paws", :creator #:user{:username "travis"}}]
          (map dissoc-ids
               (get-evidence-for-claim fresh-db [:claim/body "Dogs are great"] [true]))))
   (is (= []
          (map dissoc-ids
               (get-evidence-for-claim fresh-db [:claim/body "Dogs are great"] [false]))))
   (is (= [#:claim{:body "They have cute paws",
                   :creator #:user{:username "travis"}}]
          (map dissoc-ids
               (get-evidence-for-claim fresh-db [:claim/body "Dogs are great"] [true false])))))
  (testing "about cats"
    (is (= [#:claim{:body "They have cute paws",
                    :creator #:user{:username "travis"}}]
           (map dissoc-ids
                (get-evidence-for-claim fresh-db [:claim/body "Cats are great"] [true]))))
    (is (= [#:claim{:body "They don't like people",
                    :creator #:user{:username "travis"}}]
           (map dissoc-ids
                (get-evidence-for-claim fresh-db [:claim/body "Cats are great"] [false]))))
    (is (= [#:claim{:body "They have cute paws",
                    :creator #:user{:username "travis"}}
            #:claim{:body "They don't like people",
                    :creator #:user{:username "travis"}}]
           (map dissoc-ids
                (get-evidence-for-claim fresh-db [:claim/body "Cats are great"] [false true]))))))
