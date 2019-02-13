(ns truth.domain-test
  (:require [datomic.api :as d]
            [clojure.test :refer :all]
            [truth.schema :as schema]
            [truth.data :as data]
            [truth.domain :as t
             :refer [uuid get-user-by-email get-all-claims get-claim get-claim-evidence]]
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

(def dogs-are-great
  {:claim/body "Dogs are great",
   :claim/contributors [],
   :claim/creator #:user{:username "travis"},
   :support-count 1 :oppose-count 0 :agree-count 2 :disagree-count 0
   :agree false :disagree false})

(def cats-are-great
  {:claim/body "Cats are great",
   :claim/contributors [#:user{:username "travis"}],
   :claim/creator #:user{:username "james"},
   :support-count 2 :oppose-count 1 :agree-count 2 :disagree-count 1
   :agree false :disagree false})

(deftest test-get-claim
  (testing "Dogs are great"
    (is (= dogs-are-great
           (get-claim fresh-db [:claim/body "Dogs are great"] claim-spec))))
  (testing "Cats are great"
    (is (= cats-are-great
           (get-claim fresh-db [:claim/body "Cats are great"] claim-spec)))))

(deftest test-get-claim-as
  (testing "Dogs are great"
    (is (= dogs-are-great
           (t/get-claim-as fresh-db [:claim/body "Dogs are great"] [:user/username "anon"] claim-spec)))
    (is (= dogs-are-great
           (t/get-claim-as fresh-db [:claim/body "Dogs are great"] [:user/username "james"] claim-spec)))
    (is (= (assoc dogs-are-great :agree true)
           (t/get-claim-as fresh-db [:claim/body "Dogs are great"] [:user/username "travis"] claim-spec))))
  (testing "Cats are great"
    (is (= cats-are-great
           (t/get-claim-as fresh-db [:claim/body "Cats are great"] [:user/username "anon"] claim-spec)))
    (is (= cats-are-great
           (t/get-claim-as fresh-db [:claim/body "Cats are great"] [:user/username "travis"] claim-spec)))
    (is (= (assoc cats-are-great :disagree true)
           (t/get-claim-as fresh-db [:claim/body "Cats are great"] [:user/username "toby"] claim-spec)))
    (is (= (assoc cats-are-great :agree true)
           (t/get-claim-as fresh-db [:claim/body "Cats are great"] [:user/username "chuchu"] claim-spec)))))

(def evidence-spec
  '[:evidence/supports
    {:evidence/claim
     [:claim/body
      {(:claim/contributors :default []) [:user/username]}
      {:claim/creator [:user/username]}]}])

(def animals-are-awesome
  {:claim/body "Animals are awesome",
   :claim/contributors [],
   :claim/creator #:user{:username "travis"}
   :support-count 0,
   :oppose-count 0,
   :agree-count 2,
   :disagree-count 0,
   :agree false :disagree false})

(def dont-like-people
  {:claim/body "They don't like people",
   :claim/contributors [],
   :claim/creator #:user{:username "travis"}
   :support-count 1,
   :oppose-count 0,
   :agree-count 0,
   :disagree-count 0,
   :agree false :disagree false})

(def mean-cat
  {:claim/body "A cat was mean to me",
   :claim/contributors [],
   :claim/creator #:user{:username "travis"}
   :support-count 0, :oppose-count 0, :agree-count 0, :disagree-count 0
   :agree false :disagree false})

(deftest test-get-claim-evidence
  (testing "Cats are great"
    (is (= [{:evidence/supports true,
             :evidence/claim animals-are-awesome,
             :relevance 83}
            {:evidence/supports false,
             :evidence/claim dont-like-people
             :relevance 100}
            {:evidence/supports true,
             :evidence/claim dont-like-people
             :relevance 100}]
           (get-claim-evidence fresh-db [:claim/body "Cats are great"] evidence-spec))))
  (testing "Dogs are great"
    (is (= [{:evidence/supports true,
             :evidence/claim animals-are-awesome
             :relevance 133/2}]
           (get-claim-evidence fresh-db [:claim/body "Dogs are great"] evidence-spec))))
  (testing "They don't like people"
    (is (= [{:evidence/supports true,
             :evidence/claim mean-cat
             :relevance 100}]
           (get-claim-evidence fresh-db [:claim/body "They don't like people"] evidence-spec)))))

(deftest test-get-claim-evidence-as
  (testing "Cats are great"
    (is (= [{:evidence/supports true,
             :evidence/claim animals-are-awesome,
             :relevance 83}
            {:evidence/supports false,
             :evidence/claim dont-like-people
             :relevance 100}
            {:evidence/supports true,
             :evidence/claim dont-like-people
             :relevance 100}]
           (t/get-claim-evidence-as fresh-db [:claim/body "Cats are great"] [:user/username "anon"] evidence-spec)))
    (is (= [{:evidence/supports true,
             :evidence/claim (assoc animals-are-awesome :agree true),
             :relevance 83}
            {:evidence/supports false,
             :evidence/claim dont-like-people
             :relevance 100}
            {:evidence/supports true,
             :evidence/claim dont-like-people
             :relevance 100}]
           (t/get-claim-evidence-as fresh-db [:claim/body "Cats are great"] [:user/username "toby"] evidence-spec))))
  (testing "Dogs are great"
    (is (= [{:evidence/supports true,
             :evidence/claim animals-are-awesome
             :relevance 133/2}]
           (t/get-claim-evidence-as fresh-db [:claim/body "Dogs are great"] [:user/username "anon"] evidence-spec)))
    (is (= [{:evidence/supports true,
             :evidence/claim (assoc animals-are-awesome :agree true)
             :relevance 133/2}]
           (t/get-claim-evidence-as fresh-db [:claim/body "Dogs are great"] [:user/username "toby"] evidence-spec)))
    (is (= [{:evidence/supports true,
             :evidence/claim animals-are-awesome
             :my-relevance-rating 33
             :relevance 133/2}]
           (t/get-claim-evidence-as fresh-db [:claim/body "Dogs are great"] [:user/username "james"] evidence-spec)))))


(comment
  (d/pull fresh-dbp
          [:claim/body {:claim/evidence [{:evidence/claim [:claim/body]}]}]
          [:claim/body "They don't like people"])
  (get-all-claims fresh-db)
  (t/get-claim fresh-db [:claim/body "Cats are great"])
  (t/get-claim-as fresh-db [:claim/body "Cats are great"] [:user/username "james"])
  (t/get-claim-as fresh-db [:claim/body "Cats are great"] [:user/username "anon"])
  (t/get-claim-evidence-as fresh-db [:claim/body "Dogs are great"] [:user/username "james"])
  (t/get-claim-evidence-as fresh-db [:claim/body "Dogs are great"] [:user/username "toby"])
  (get-claim-evidence fresh-db [:claim/body "They don't like people"])
  (get-claim-evidence fresh-db [:claim/body "Dogs are great"])
  (get-claim-evidence fresh-db [:claim/body "Cats are great"])


  )
