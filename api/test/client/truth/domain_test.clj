(ns truth.domain-test
  (:require [datomic.client.api :as d]
            [clojure.test :refer :all]
            [truth.schema :as schema]
            [truth.data :as data]
            [truth.domain :as t]
            [truth.cloud :as cloud]
            ))

(use-fixtures
  :once (fn [run-tests]
          (def client (d/client cloud/cfg))
          (def db-spec {:db-name "test"})
          (def conn (d/connect client db-spec))
          (def fresh-db (d/db conn))
          (run-tests)))

(def claim-spec
  '[:claim/body
    {(:claim/contributors :default []) [:user/username]}
    {:claim/creator [:user/username]}])

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
   :agreement 200 :agreement-count 2 :my-agreement nil
   :score 200})

(def dont-like-people
  {:claim/body "They don't like people",
   :claim/contributors [],
   :claim/creator #:user{:username "travis"}
   :support-count 1,
   :oppose-count 0,
   :agreement 0 :agreement-count 0 :my-agreement nil
   :score 0})

(def mean-cat
  {:claim/body "A cat was mean to me",
   :claim/contributors [],
   :claim/creator #:user{:username "travis"}
   :support-count 0, :oppose-count 0
   :agreement 0 :agreement-count 0 :my-agreement nil
   :score 0})

(def dogs-are-great
  {:claim/body "Dogs are great",
   :claim/contributors [],
   :claim/creator #:user{:username "travis"},
   :support-count 1 :oppose-count 0
   :agreement 200 :agreement-count 2 :my-agreement nil
   :score 533/2})

(def cats-are-great
  {:claim/body "Cats are great",
   :claim/contributors [#:user{:username "travis"}],
   :claim/creator #:user{:username "james"},
   :support-count 2 :oppose-count 1
   :agreement 100 :agreement-count 3 :my-agreement nil
   :score 183})

(deftest test-get-claim
  (testing "Dogs are great"
    (is (= dogs-are-great
           (t/get-claim fresh-db [:claim/slug "dogs-are-great"] claim-spec))))
  (testing "Cats are great"
    (is (= cats-are-great
           (t/get-claim fresh-db [:claim/slug "cats-are-great"] claim-spec)))))

(deftest test-get-claim-as
  (testing "Dogs are great"
    (is (= dogs-are-great
           (t/get-claim-as fresh-db [:claim/slug "dogs-are-great"] [:user/username "anon"] claim-spec)))
    (is (= dogs-are-great
           (t/get-claim-as fresh-db [:claim/slug "dogs-are-great"] [:user/username "james"] claim-spec)))
    (is (= (assoc dogs-are-great :my-agreement 100)
           (t/get-claim-as fresh-db [:claim/slug "dogs-are-great"] [:user/username "travis"] claim-spec))))
  (testing "Cats are great"
    (is (= cats-are-great
           (t/get-claim-as fresh-db [:claim/slug "cats-are-great"] [:user/username "anon"] claim-spec)))
    (is (= cats-are-great
           (t/get-claim-as fresh-db [:claim/slug "cats-are-great"] [:user/username "travis"] claim-spec)))
    (is (= (assoc cats-are-great :my-agreement -100)
           (t/get-claim-as fresh-db [:claim/slug "cats-are-great"] [:user/username "toby"] claim-spec)))
    (is (= (assoc cats-are-great :my-agreement 100)
           (t/get-claim-as fresh-db [:claim/slug "cats-are-great"] [:user/username "chuchu"] claim-spec)))))

(deftest test-get-claim-for
  (testing "Dogs are great"
    (is (= {:user-agreement 0
            :my-agreement 100
            :claim/body "Dogs are great",
            :claim/contributors [],
            :claim/creator {:user/username "travis"}}
           (t/get-claim-for fresh-db
                            [:claim/slug "dogs-are-great"] [:user/username "anon"]
                            [:user/username "travis"] claim-spec)))
    (is (= {:user-agreement 0
            :my-agreement -101
            :claim/body "Dogs are great",
            :claim/contributors [],
            :claim/creator {:user/username "travis"}}
           (t/get-claim-for fresh-db
                            [:claim/slug "dogs-are-great"] [:user/username "james"]
                            [:user/username "chuchu"] claim-spec)))
    (is (= {:user-agreement 100
            :my-agreement 100
            :claim/body "Dogs are great",
            :claim/contributors [],
            :claim/creator {:user/username "travis"}}
           (t/get-claim-for fresh-db
                            [:claim/slug "dogs-are-great"] [:user/username "travis"]
                            [:user/username "toby"] claim-spec)))))

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
           (t/get-claim-evidence fresh-db [:claim/slug "cats-are-great"] evidence-spec))))
  (testing "Dogs are great"
    (is (= [{:evidence/supports true,
             :evidence/claim animals-are-awesome
             :relevance 133/2}]
           (t/get-claim-evidence fresh-db [:claim/slug "dogs-are-great"] evidence-spec))))
  (testing "They don't like people"
    (is (= [{:evidence/supports true,
             :evidence/claim mean-cat
             :relevance 100}]
           (t/get-claim-evidence fresh-db [:claim/slug "they-dont-like-people"] evidence-spec)))))

;; TODO: turn this back on once we get cloudsearch working
#_(deftest test-search-claims-as
  (testing "Cats are great"
    (is (= [{:search/score 1.0 :search/result dogs-are-great}
            {:search/score 1.0 :search/result cats-are-great}]
           (t/search-claims-as fresh-db [:user/username "anon"] "are great" claim-spec)))))

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
           (t/get-claim-evidence-as fresh-db [:claim/slug "cats-are-great"] [:user/username "anon"] evidence-spec)))
    (is (= [{:evidence/supports true,
             :evidence/claim (assoc animals-are-awesome :my-agreement 100),
             :relevance 83}
            {:evidence/supports false,
             :evidence/claim dont-like-people
             :relevance 100}
            {:evidence/supports true,
             :evidence/claim dont-like-people
             :relevance 100}]
           (t/get-claim-evidence-as fresh-db [:claim/slug "cats-are-great"] [:user/username "toby"] evidence-spec))))
  (testing "Dogs are great"
    (is (= [{:evidence/supports true,
             :evidence/claim animals-are-awesome
             :relevance 133/2}]
           (t/get-claim-evidence-as fresh-db [:claim/slug "dogs-are-great"] [:user/username "anon"] evidence-spec)))
    (is (= [{:evidence/supports true,
             :evidence/claim (assoc animals-are-awesome :my-agreement 100)
             :relevance 133/2}]
           (t/get-claim-evidence-as fresh-db [:claim/slug "dogs-are-great"] [:user/username "toby"] evidence-spec)))
    (is (= [{:evidence/supports true,
             :evidence/claim animals-are-awesome
             :my-relevance-rating 33
             :relevance 133/2}]
           (t/get-claim-evidence-as fresh-db [:claim/slug "dogs-are-great"] [:user/username "james"] evidence-spec)))))

(def evidence-for-spec
  '[:evidence/supports
    {:evidence/claim
     [:claim/body]}])

(deftest test-get-claim-evidence-for
  (testing "Cats are great"
    (is (= [{:evidence/supports true,
             :evidence/claim {:user-agreement nil
                              :claim/body "Animals are awesome"}
             :user-relevance 66}
            {:evidence/supports false,
             :evidence/claim {:user-agreement nil
                              :claim/body "They don't like people"}
             :user-relevance nil}
            ]
           (t/get-claim-evidence-for fresh-db [:claim/slug "cats-are-great"] [:user/username "travis"] evidence-for-spec)))
    (is (= [{:evidence/supports true,
             :evidence/claim {:user-agreement 100
                              :claim/body "Animals are awesome"},
             :user-relevance nil}]
           (t/get-claim-evidence-for fresh-db [:claim/slug "cats-are-great"] [:user/username "toby"] evidence-for-spec))))
  (testing "Dogs are great"
    (is (= [{:evidence/supports true,
             :evidence/claim {:user-agreement nil
                              :claim/body "Animals are awesome"}
             :user-relevance 100}]
           (t/get-claim-evidence-for fresh-db [:claim/slug "dogs-are-great"] [:user/username "travis"] evidence-for-spec)))
    (is (= [{:evidence/supports true,
             :evidence/claim {:user-agreement 100
                              :claim/body "Animals are awesome"}
             :user-relevance nil}]
           (t/get-claim-evidence-for fresh-db [:claim/slug "dogs-are-great"] [:user/username "toby"] evidence-for-spec)))
    (is (= [{:evidence/supports true,
             :evidence/claim {:user-agreement nil
                              :claim/body "Animals are awesome"}
             :user-relevance 33}]
           (t/get-claim-evidence-for fresh-db [:claim/slug "dogs-are-great"] [:user/username "james"] evidence-for-spec)))))

(comment
  (d/pull fresh-dbp
          [:claim/body {:claim/evidence [{:evidence/claim [:claim/body]}]}]
          [:claim/body "they-dont-like-people"])
  (t/get-all-claims fresh-db)
  (t/get-claim fresh-db [:claim/slug "cats-are-great"])
  (t/get-claim-as fresh-db [:claim/slug "cats-are-great"] [:user/username "james"])
  (t/get-claim-as fresh-db [:claim/slug "cats-are-great"] [:user/username "anon"])
  (t/get-claim-evidence-as fresh-db [:claim/slug "dogs-are-great"] [:user/username "james"])
  (t/get-claim-evidence-as fresh-db [:claim/slug "dogs-are-great"] [:user/username "toby"])
  (get-claim-evidence fresh-db [:claim/slug "dogs-are-great"])
  (get-claim-evidence fresh-db [:claim/slug "cats-are-great"])
  (t/get-vote-for-user-and-claim fresh-db [:user/username "travis"] [:claim/slug "dogs-are-great"])

  )
