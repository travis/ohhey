(ns truth.domain-test
  (:require [datomic.api :as d]
            [clojure.test :refer :all]
            [truth.schema :as schema]
            [truth.data :as data]
            [truth.domain :refer
             [uuid get-user-by-email get-all-claims get-claim get-claim-evidence]]
            ))

(use-fixtures
  :once (fn [run-tests]
          (let [uri "datomic:mem://domain-test"]
            (d/delete-database uri)
            (d/create-database uri)
            (let [conn (d/connect uri)]
              (schema/load conn)
              (data/load conn)
              (def fresh-db (d/db conn))))
          (run-tests)
          ))

(defn dissoc-ids [map]
  (dissoc map :db/id :user/id :claim/id :claim-vote/id :evidence/id :relevance-vote/id))

(deftest test-get-user-by-email
  (testing "it returns travis"
    (is (= {:user/username "travis", :user/email "travis@truth.com"}
           (dissoc-ids (get-user-by-email fresh-db "travis@truth.com"))))))


(deftest test-get-all-claims
  (testing "it returns all claims"
    (is (= [#:claim{:body "They don't like people",
                    :creator #:user{:username "travis"}
                    :evidence [#:evidence{:supports true,
                                          :claim #:claim{:body "A cat was mean to me"}}]}
            #:claim{:body "A cat was mean to me",
                    :creator #:user{:username "travis"}}
            #:claim{:body "Dogs are great",
                    :creator #:user{:username "travis"},
                    :votes
                    [#:claim-vote{:agree true,
                                  :voter #:user{:username "travis"}}
                     #:claim-vote{:agree true,
                                  :voter #:user{:username "toby"}}]
                    :evidence
                    [#:evidence{:supports true,
                                :claim #:claim{:body "They have cute paws"}}]}
            #:claim{:body "They have cute paws",
                    :creator #:user{:username "travis"}
                    :votes
                    [#:claim-vote{:agree true, :voter #:user{:username "chuchu"}}
                     #:claim-vote{:agree true, :voter #:user{:username "toby"}}]}
            #:claim{:body "Cats are great",
                    :creator #:user{:username "james"},
                    :contributors [#:user{:username "travis"}],
                    :votes
                    [#:claim-vote{:agree false,
                                  :voter #:user{:username "toby"}}
                     #:claim-vote{:agree true,
                                  :voter #:user{:username "james"}}
                     #:claim-vote{:agree true,
                                  :voter #:user{:username "chuchu"}}]
                    :evidence
                    [#:evidence{:supports true,
                                :claim #:claim{:body "They have cute paws"}}
                     #:evidence{:supports false,
                                :claim
                                #:claim{:body "They don't like people"}}
                     #:evidence{:supports true,
                                :claim
                                #:claim{:body "They don't like people"}}]}]
           (map dissoc-ids (get-all-claims fresh-db))))))

(def claim-spec
  '[:claim/body
    {(:claim/contributors :default []) [:user/username]}
    {:claim/creator [:user/username]}])

(deftest test-get-claim
  (testing "Dogs are great"
    (is (= {:claim/body "Dogs are great",
            :claim/contributors [],
            :claim/creator #:user{:username "travis"},
            :support-count 1,
            :oppose-count 0
            :agree-count 2
            :disagree-count 0}
           (dissoc-ids
            (get-claim fresh-db [:claim/body "Dogs are great"] claim-spec)))))
  (testing "Cats are great"
    (is (= {:claim/body "Cats are great",
            :claim/contributors [#:user{:username "travis"}],
            :claim/creator #:user{:username "james"},
            :support-count 2,
            :oppose-count 1
            :agree-count 2
            :disagree-count 1}
           (dissoc-ids
            (get-claim fresh-db [:claim/body "Cats are great"] claim-spec))))))

(def evidence-spec
  '[:evidence/supports
    {:evidence/claim
     [:claim/body
      {(:claim/contributors :default []) [:user/username]}
      {:claim/creator [:user/username]}]}])

(deftest test-get-claim-evidence
  (testing "Cats are great"
    (is (= '({:evidence/supports true,
              :evidence/claim
              #:claim{:body "They have cute paws",
                      :contributors [],
                      :creator #:user{:username "travis"}},
              :relevance 83,
              :claim
              {:support-count 0,
               :oppose-count 0,
               :agree-count 2,
               :disagree-count 0}}
             {:evidence/supports false,
              :evidence/claim
              #:claim{:body "They don't like people",
                      :contributors [],
                      :creator #:user{:username "travis"}},
              :relevance 100,
              :claim
              {:support-count 1,
               :oppose-count 0,
               :agree-count 0,
               :disagree-count 0}}
             {:evidence/supports true,
              :evidence/claim
              #:claim{:body "They don't like people",
                      :contributors [],
                      :creator #:user{:username "travis"}},
              :relevance 100,
              :claim
              {:support-count 1,
               :oppose-count 0,
               :agree-count 0,
               :disagree-count 0}})
           (get-claim-evidence fresh-db [:claim/body "Cats are great"] evidence-spec))))
  (testing "Dogs are great"
    (is (= '({:evidence/supports true,
              :evidence/claim
              #:claim{:body "They have cute paws",
                      :contributors [],
                      :creator #:user{:username "travis"}},
              :relevance 133/2,
              :claim
              {:support-count 0,
               :oppose-count 0,
               :agree-count 2,
               :disagree-count 0}})
           (get-claim-evidence fresh-db [:claim/body "Dogs are great"] evidence-spec))))
  (testing "They don't like people"
    (is (= '({:evidence/supports true,
              :evidence/claim #:claim{:body "A cat was mean to me",
                                      :contributors [],
                                      :creator #:user{:username "travis"}},
              :relevance 100, :claim {:support-count 0, :oppose-count 0, :agree-count 0, :disagree-count 0}})
           (get-claim-evidence fresh-db [:claim/body "They don't like people"] evidence-spec)))))

(comment
  (d/pull fresh-db
          [:claim/body {:claim/evidence [{:evidence/claim [:claim/body]}]}]
          [:claim/body "They don't like people"])
  (get-claim fresh-db [:claim/body "They don't like people"])
  (get-claim-evidence fresh-db [:claim/body "They don't like people"])
  (get-claim-evidence fresh-db [:claim/body "Dogs are great"])
  (get-claim-evidence fresh-db [:claim/body "Cats are great"])


  )
