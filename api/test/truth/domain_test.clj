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
              {:claim/body "They have cute paws",
               :claim/contributors [],
               :claim/creator #:user{:username "travis"}
               :support-count 0,
               :oppose-count 0,
               :agree-count 2,
               :disagree-count 0},
              :relevance 83}
             {:evidence/supports false,
              :evidence/claim
              {:claim/body "They don't like people",
               :claim/contributors [],
               :claim/creator #:user{:username "travis"}
               :support-count 1,
               :oppose-count 0,
               :agree-count 0,
               :disagree-count 0},
              :relevance 100}
             {:evidence/supports true,
              :evidence/claim
              {:claim/body "They don't like people",
               :claim/contributors [],
               :claim/creator #:user{:username "travis"}
               :support-count 1,
               :oppose-count 0,
               :agree-count 0,
               :disagree-count 0},
              :relevance 100})
           (get-claim-evidence fresh-db [:claim/body "Cats are great"] evidence-spec))))
  (testing "Dogs are great"
    (is (= '({:evidence/supports true,
              :evidence/claim
              {:claim/body "They have cute paws",
               :claim/contributors [],
               :claim/creator #:user{:username "travis"}
               :support-count 0,
               :oppose-count 0,
               :agree-count 2,
               :disagree-count 0},
              :relevance 133/2})
           (get-claim-evidence fresh-db [:claim/body "Dogs are great"] evidence-spec))))
  (testing "They don't like people"
    (is (= '({:evidence/supports true,
              :evidence/claim {:claim/body "A cat was mean to me",
                               :claim/contributors [],
                               :claim/creator #:user{:username "travis"}
                               :support-count 0, :oppose-count 0, :agree-count 0, :disagree-count 0},
              :relevance 100})
           (get-claim-evidence fresh-db [:claim/body "They don't like people"] evidence-spec)))))

(comment
  (d/pull fresh-db
          [:claim/body {:claim/evidence [{:evidence/claim [:claim/body]}]}]
          [:claim/body "They don't like people"])
  (get-all-claims fresh-db)
  (get-claim-evidence fresh-db [:claim/body "They don't like people"])
  (get-claim-evidence fresh-db [:claim/body "Dogs are great"])
  (get-claim-evidence fresh-db [:claim/body "Cats are great"])


  )
