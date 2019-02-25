(ns truth.domain.rules-test
  (:require [datomic.api :as d]
            [clojure.test :refer :all]
            [truth.schema :as schema]
            [truth.data :as data]
            [truth.domain.rules :refer [rules]]))

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


(deftest agree-disagree
  (let [agree-disagree
        (fn [slug]
          (d/q '[:find (sum ?agreement) (sum ?agreement-count)
                 :in $ % ?claim
                 :with ?uniqueness
                 :where
                 (agree-disagree ?claim ?uniqueness ?agreement ?agreement-count)]
               fresh-db rules [:claim/slug slug]))]
    (is (= [[200 2]]
           (agree-disagree "dogs-are-great")))
    (is (= [[100 3]]
           (agree-disagree "cats-are-great")))
    (is (= [[200 2]]
           (agree-disagree "animals-are-awesome")))
    (is (= [[0 0]]
           (agree-disagree "a-cat-was-mean-to-me")))
    (is (= [[0 0]]
           (agree-disagree "they-dont-like-people")))))

(deftest claim-for
  (let [claim-for
        (fn [slug username]
          (d/q '[:find (sum ?agreement)
                 :in $ % ?claim ?user
                 :with ?uniqueness
                 :where
                 (claim-for ?claim ?user ?uniqueness ?agreement)]
               fresh-db rules [:claim/slug slug] [:user/username username]))]
    (is (= [[100]]
           (claim-for "dogs-are-great" "travis")))
    (is (= [[0]]
           (claim-for "cats-are-great" "travis")))
    (is (= [[100]]
           (claim-for "cats-are-great" "chuchu")))
    (is (= [[-100]]
           (claim-for "cats-are-great" "toby")))
    (is (= [[0]]
           (claim-for "animals-are-awesome" "travis")))
    (is (= [[0]]
           (claim-for "a-cat-was-mean-to-me" "travis")))
    (is (= [[0]]
           (claim-for "they-dont-like-people" "travis")))))

(deftest agreement-for
  (let [agreement-for
        (fn [slug username]
          (d/q '[:find (sum ?agreement)
                 :in $ % ?claim ?user
                 :with ?uniqueness
                 :where
                 (agreement-for ?claim ?user ?uniqueness ?agreement)]
               fresh-db rules [:claim/slug slug] [:user/username username]))]
    (is (= [[100]]
           (agreement-for "dogs-are-great" "travis")))
    (is (= [[0]]
           (agreement-for "cats-are-great" "travis")))
    (is (= [[100]]
           (agreement-for "cats-are-great" "chuchu")))
    (is (= [[-100]]
           (agreement-for "cats-are-great" "toby")))
    (is (= [[0]]
           (agreement-for "animals-are-awesome" "travis")))
    (is (= [[0]]
           (agreement-for "a-cat-was-mean-to-me" "travis")))
    (is (= [[0]]
           (agreement-for "they-dont-like-people" "travis")))))

(deftest evidence-for
  (let [evidence-for
        (fn [slug username]
          (d/q '[:find (pull ?evidence [{:evidence/claim
                                         [:claim/body]}])
                 ?rating ?agreement

                 :in $ % ?claim ?user
                 :with ?uniqueness
                 :where
                 (evidence-for ?claim ?user ?evidence ?uniqueness ?rating ?agreement)]
               fresh-db rules [:claim/slug slug] [:user/username username]))]
    (testing "dogs are great"
      (is (= [[{:evidence/claim {:claim/body "Animals are awesome"}} 100 -101]
              [{:evidence/claim {:claim/body "Animals are awesome"}} -1 -101]]
             (evidence-for "dogs-are-great" "travis")))
      (is (= [[{:evidence/claim {:claim/body "Animals are awesome"}} 33 -101]]
             (evidence-for "dogs-are-great" "james")))
      (is (= [[{:evidence/claim {:claim/body "Animals are awesome"}} -1 100]]
             (evidence-for "dogs-are-great" "chuchu")))
      (is (= [[{:evidence/claim {:claim/body "Animals are awesome"}} -1 100]]
             (evidence-for "dogs-are-great" "toby"))))
    (testing "cats are great"
      (is (= [[{:evidence/claim {:claim/body "Animals are awesome"}} 66 -101]
              [{:evidence/claim {:claim/body "They don't like people"}} -1 -101]]
             (evidence-for "cats-are-great" "travis")))
      (is (= [[{:evidence/claim {:claim/body "Animals are awesome"}} -1 100]]
             (evidence-for "cats-are-great" "chuchu")))
      (is (= [[{:evidence/claim {:claim/body "Animals are awesome"}} 100 -101]
              [{:evidence/claim {:claim/body "Animals are awesome"}} -1 -101]]
             (evidence-for "cats-are-great" "james")))
      (is (= [[{:evidence/claim {:claim/body "Animals are awesome"}} -1 100]]
             (evidence-for "cats-are-great" "toby")))
      (is (= [[{:evidence/claim {:claim/body "They don't like people"}} -1 -101]]
             (evidence-for "cats-are-great" "tani"))))))

(deftest agree-disagree-score
  (let [agree-disagree-score
        (fn [slug]
          (d/q '[:find (sum ?score)
                 :in $ % ?claim
                 :with ?uniqueness
                 :where
                 (agree-disagree-score ?claim ?uniqueness ?score)]
               fresh-db rules [:claim/slug slug]))]
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
               fresh-db rules [:evidence/id id]))]
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
                 fresh-db rules [:claim/slug slug]))]
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
               fresh-db rules [:claim/slug slug]))]
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
               fresh-db rules [:claim/slug slug]))]
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
