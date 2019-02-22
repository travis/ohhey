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
           (get-claim fresh-db [:claim/slug "dogs-are-great"] claim-spec))))
  (testing "Cats are great"
    (is (= cats-are-great
           (get-claim fresh-db [:claim/slug "cats-are-great"] claim-spec)))))

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
           (get-claim-evidence fresh-db [:claim/slug "cats-are-great"] evidence-spec))))
  (testing "Dogs are great"
    (is (= [{:evidence/supports true,
             :evidence/claim animals-are-awesome
             :relevance 133/2}]
           (get-claim-evidence fresh-db [:claim/slug "dogs-are-great"] evidence-spec))))
  (testing "They don't like people"
    (is (= [{:evidence/supports true,
             :evidence/claim mean-cat
             :relevance 100}]
           (get-claim-evidence fresh-db [:claim/slug "they-dont-like-people"] evidence-spec)))))

(deftest test-search-claims-as
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

(deftest agree-disagree-score
  (let [agree-disagree-score
        (fn [slug]
          (d/q '[:find (sum ?score)
                 :in $ % ?claim
                 :with ?uniqueness
                 :where
                 (agree-disagree-score ?claim ?uniqueness ?score)]
               fresh-db t/rules [:claim/slug slug]))]
    (is (= [[200]]
           (agree-disagree-score "dogs-are-great")))
    (is (= [[100]]
           (agree-disagree-score "cats-are-great")))
    (is (= [[200]]
           (agree-disagree-score "animals-are-awesome")))
    (is (= [[0]]
           (agree-disagree-score "a-cat-was-mean-to-me")))
    (is (= [[0]]
           (agree-disagree-score "they-dont-like-people")))))

(deftest evidence-score
  (let [evidence-score
        (fn [id]
          (d/q '[:find
                 (sum ?score) (sum ?score-component-count)
                 :in $ % ?evidence
                 :with ?uniqueness
                 :where
                 (evidence-score ?evidence ?uniqueness ?score ?score-component-count)]
               fresh-db t/rules [:evidence/id id]))]
    (is (= [[26600 4]]
           (evidence-score "ara-supports-dag")))
    (is (= [[0 0]]
           (evidence-score "cwm-supports-dlp")))
    (is (= [[0 0]]
           (evidence-score "dlp-opposes-cag")))))

(deftest test-support-oppose-score
  (testing "support-oppose-score"
    (let [support-oppose-score
          (fn [slug]
            (d/q '[:find (sum ?score) (sum ?score-component-count)
                   :in $ % ?claim
                   :with ?uniqueness
                   :where
                   (support-oppose-score ?claim 1 ?uniqueness ?score ?score-component-count)
                   ]
                 fresh-db t/rules [:claim/slug slug]))]
      (is (= [[26600 4]]
             (support-oppose-score "dogs-are-great")))
      (is (= [[33200 4]]
             (support-oppose-score "cats-are-great")))
      (is (= [[0 0]]
             (support-oppose-score "animals-are-awesome")))
      (is (= [[0 0]]
             (support-oppose-score "a-cat-was-mean-to-me")))
      (is (= [[0 0]]
             (support-oppose-score "they-dont-like-people")))
      )))

(deftest test-claim-score
  (let [claim-score
        (fn [slug]
          (d/q '[:find (sum ?agree-disagree-score) (sum ?score) (sum ?score-component-count)
                 :in $ % ?claim
                 :with ?uniqueness
                 :where
                 (claim-score ?claim 1 ?uniqueness ?agree-disagree-score ?score ?score-component-count)
                 ]
               fresh-db t/rules [:claim/slug slug]))]
    (is (= [[200 26600 4]]
           (claim-score "dogs-are-great")))
    (is (= [[100 33200 4]]
           (claim-score "cats-are-great")))
    (is (= [[200 0 0]]
           (claim-score "animals-are-awesome")))
    (is (= [[0 0 0]]
           (claim-score "a-cat-was-mean-to-me")))
    (is (= [[0 0 0]]
           (claim-score "they-dont-like-people")))
    ))

(deftest test-claim-stats
  (let [claim-stats
        (fn [slug]
          (d/q '[:find (sum ?agreement) (sum ?agreement-count) (sum ?support) (sum ?oppose) (sum ?agree-disagree-score) (sum ?score) (sum ?score-component-count)
                 :in $ % ?claim
                 :with ?uniqueness
                 :where
                 (claim-stats ?claim ?uniqueness ?agreement ?agreement-count ?support ?oppose ?agree-disagree-score ?score ?score-component-count)
                 ]
               fresh-db t/rules [:claim/slug slug]))]
    (is (= [[200 2 1 0 200 26600 4]]
           (claim-stats "dogs-are-great")))
    (is (= [[100 3 2 1 100 33200 4]]
           (claim-stats "cats-are-great")))
    (is (= [[200 2 0 0 200 0 0]]
           (claim-stats "animals-are-awesome")))
    (is (= [[0 0 0 0 0 0 0]]
           (claim-stats "a-cat-was-mean-to-me")))
    (is (= [[0 0 1 0 0 0 0]]
           (claim-stats "they-dont-like-people")))
    ))

(comment
  (d/pull fresh-dbp
          [:claim/body {:claim/evidence [{:evidence/claim [:claim/body]}]}]
          [:claim/body "they-dont-like-people"])
  (get-all-claims fresh-db)
  (t/get-claim fresh-db [:claim/slug "cats-are-great"])
  (t/get-claim-as fresh-db [:claim/slug "cats-are-great"] [:user/username "james"])
  (t/get-claim-as fresh-db [:claim/slug "cats-are-great"] [:user/username "anon"])
  (t/get-claim-evidence-as fresh-db [:claim/slug "dogs-are-great"] [:user/username "james"])
  (t/get-claim-evidence-as fresh-db [:claim/slug "dogs-are-great"] [:user/username "toby"])
  (get-claim-evidence fresh-db [:claim/slug "dogs-are-great"])
  (get-claim-evidence fresh-db [:claim/slug "cats-are-great"])
  (t/get-vote-for-user-and-claim fresh-db [:user/username "travis"] [:claim/slug "dogs-are-great"])

  )
