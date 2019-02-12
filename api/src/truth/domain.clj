(ns truth.domain
  (:require [datomic.api :as d]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn new-user [{db-id :db/id id :id
                 username :username email :email}]
  {:db/id (or db-id (uuid))
   :user/id (or id (uuid))
   :user/username username
   :user/email email})

(defn new-claim [{db-id :db/id id :id
                  body :body creator :creator
                  contributors :contributors evidence :evidence
                  votes :votes
                  :or {contributors [] evidence [] votes []}}]
  {:db/id (or db-id (uuid))
   :claim/id (or id (uuid))
   :claim/body body
   :claim/creator creator
   :claim/contributors contributors
   :claim/evidence  evidence
   :claim/votes votes
   })

(defn new-claim-vote [{db-id :db/id id :id
                       claim :claim voter :voter agree :agree}]
  {:db/id (or db-id (uuid))
   :claim-vote/id (or id (uuid))
   :claim-vote/voter voter
   :claim-vote/agree agree})

(defn new-evidence [{db-id :db/id id :id
                     creator :creator claim :claim supports :supports
                     votes :votes
                     :or {votes []}}]
  {:db/id (or db-id (uuid))
   :evidence/id (or id (uuid))
   :evidence/creator creator
   :evidence/claim claim
   :evidence/supports supports
   :evidence/votes votes})

(defn new-relevance-vote [{db-id :db/id id :id
                           evidence :evidence voter :voter rating :rating}]
  {:db/id (or db-id (uuid))
   :relevance-vote/id (or id (uuid))
   :relevance-vote/voter voter
   :relevance-vote/rating rating})


(defn get-user-by-email [db email]
  (first (first
          (d/q '[:find (pull ?user [*])
                 :in $ ?email
                 :where
                 [?user :user/email ?email]]
               db email))))

(defn get-claim-by-body [db]
  (map first
       (d/q
        '[:find (pull ?claim  [* {:claim/contributors [:user/username]} {:claim/creator [:user/username]}])
          :where [?claim :claim/id _]]
        db)))

(defn get-contributors [db claim]
  (map first
   (d/q '[:find (pull ?user [:user/username])
          :in $ ?claim-id
          :where
          [?claim-id :claim/contributors ?user]]
        db claim)))

(defn get-evidence-for-claim [db claim supports]
  (map first
      (d/q '[:find (pull ?evidence-claim [:claim/body {:claim/creator [:user/username]}])
             :in $ ?claim [?supports ...]
             :where
             [?claim :claim/evidence ?evidence]
             [?evidence :evidence/supports ?supports]
             [?evidence :evidence/claim ?evidence-claim]]
           db claim supports)))

(def rules
  '[[(agree-disagree ?claim ?uniqueness ?agree ?disagree)
     (or-join
      [?claim ?uniqueness ?agree ?disagree]
      (and
       [?claim :claim/votes ?vote]
       [(identity ?vote) ?uniqueness]
       (or-join
        [?vote ?agree ?disagree]
        (and
         [?vote :claim-vote/agree true]
         [(ground 1) ?agree]
         [(ground 0) ?disagree])
        (and
         [?vote :claim-vote/agree false]
         [(ground 0) ?agree]
         [(ground 1) ?disagree])))
      (and
       [(identity ?claim) ?uniqueness]
       [(ground 0) ?agree]
       [(ground 0) ?disagree]))]
    [(agree-disagree-as ?claim ?user ?uniqueness ?i-agree ?i-disagree)
     (or-join
      [?claim ?user ?uniqueness ?i-agree ?i-disagree]
      (and
       [?claim :claim/votes ?vote]
       [(identity ?vote) ?uniqueness]
       (or-join
        [?user ?vote ?i-agree ?i-disagree]
        (and
         [?vote :claim-vote/agree true]
         [?vote :claim-vote/voter ?user]
         [(ground 1) ?i-agree]
         [(ground 0) ?i-disagree])
        (and
         [?vote :claim-vote/agree true]
         (not [?vote :claim-vote/voter ?user])
         [(ground 0) ?i-agree]
         [(ground 0) ?i-disagree])
        (and
         [?vote :claim-vote/agree false]
         [?vote :claim-vote/voter ?user]
         [(ground 0) ?i-agree]
         [(ground 1) ?i-disagree])
        (and
         [?vote :claim-vote/agree false]
         (not [?vote :claim-vote/voter ?user])
         [(ground 0) ?i-agree]
         [(ground 0) ?i-disagree])))
      (and
       [(identity ?claim) ?uniqueness]
       [(ground 0) ?i-agree]
       [(ground 0) ?i-disagree]
       ))]
    [(support-oppose ?claim ?uniqueness ?support ?oppose)
     (or-join
      [?claim ?uniqueness ?support ?oppose]
      (and
       [?claim :claim/evidence ?evidence]
       [(identity ?evidence) ?uniqueness]
       (or-join
        [?evidence ?support ?oppose]
        (and
         [?evidence :evidence/supports true]
         [(ground 1) ?support]
         [(ground 0) ?oppose])
        (and
         [?evidence :evidence/supports false]
         [(ground 0) ?support]
         [(ground 1) ?oppose])))
      (and
       [(identity ?claim) ?uniqueness]
       [(ground 0) ?support]
       [(ground 0) ?oppose]))]
    [(claim-stats ?claim ?uniqueness ?agree ?disagree ?support ?oppose)
     (or-join
      [?claim ?uniqueness ?agree ?disagree ?support ?oppose]
      (and
       [(ground 0) ?support]
       [(ground 0) ?oppose]
       (agree-disagree ?claim ?uniqueness ?agree ?disagree))
      (and
       [(ground 0) ?agree]
       [(ground 0) ?disagree]
       (support-oppose ?claim ?uniqueness ?support ?oppose)))]
    [(evidence-stats ?evidence ?uniqueness ?agree ?disagree ?support ?oppose ?rating ?rating-count)
     (or-join
      [?evidence ?uniqueness ?agree ?disagree ?support ?oppose ?rating ?rating-count]
      (and
       [?evidence :evidence/votes ?relevance-vote]
       [(identity ?relevance-vote) ?uniqueness]
       [?relevance-vote :relevance-vote/rating ?rating]
       [(ground 1) ?rating-count]
       [(ground 0) ?agree]
       [(ground 0) ?disagree]
       [(ground 0) ?support]
       [(ground 0) ?oppose])
      (and
       [?evidence :evidence/claim ?claim]
       (claim-stats ?claim ?uniqueness ?agree ?disagree ?support ?oppose)
       [(ground 0) ?rating-count]
       [(ground 0) ?rating]))]])

(defn assoc-claim-stats
  ([claim support-count oppose-count agree-count disagree-count]
   (assoc-claim-stats claim support-count oppose-count agree-count disagree-count 0 0)
   )
  ([claim support-count oppose-count agree-count disagree-count i-agree-count i-disagree-count]
   (assoc claim
          :support-count support-count :oppose-count oppose-count
          :agree-count agree-count :disagree-count disagree-count
          :agree (< 0 i-agree-count) :disagree (< 0 i-disagree-count))))

(def default-claim-spec '[:db/id
                          :claim/id
                          :claim/body
                          {(:claim/contributors :default []) [:user/username]}
                          {:claim/creator [:user/username]}])

(defn get-claim-as
  ([db claim-ref user-ref]
   (get-claim-as db claim-ref user-ref default-claim-spec))
  ([db claim-ref user-ref claim-spec]
   (let [result
         (d/q
          (apply
           conj
           '[:find]
           [(list 'pull '?claim claim-spec)
            '(sum ?support) '(sum ?oppose)
            '(sum ?agree) '(sum ?disagree)
            '(sum ?i-agree) '(sum ?i-disagree)
            ]
           '[:in $ % ?claim ?user
             :with ?uniqueness
             :where
             (or
              (and
               [?user]
               (claim-stats ?claim ?uniqueness ?agree ?disagree ?support ?oppose)
               [(ground 0) ?i-agree]
               [(ground 0) ?i-disagree]
               )
              (and
               (agree-disagree-as ?claim ?user ?uniqueness ?i-agree ?i-disagree)
               [(ground 0) ?agree]
               [(ground 0) ?disagree]
               [(ground 0) ?support]
               [(ground 0) ?oppose]
               ))
             ])
          db rules claim-ref (or user-ref [:user/username "anon"]))]
     (apply assoc-claim-stats result))))

(defn get-claim
  ([db claim-ref]
   (get-claim db claim-ref default-claim-spec))
  ([db claim-ref claim-spec]
   (get-claim-as db claim-ref nil claim-spec)))

(defn get-all-claims
  ([db]
   (get-all-claims
    db
    default-claim-spec))
  ([db claim-spec]
   (let [results
         (d/q
          (apply
           conj
           '[:find]
           (list 'pull '?claim claim-spec)
           '[(sum ?support) (sum ?oppose)
             (sum ?agree) (sum ?disagree)
             :in $ %
             :with ?uniqueness
             :where
             [?claim :claim/id _]
             (claim-stats ?claim ?uniqueness ?agree ?disagree ?support ?oppose)])
          db rules)]
     (map (fn [result] (apply assoc-claim-stats result)) results))))

(defn assoc-evidence-stats [evidence relevance-rating-sum relevance-rating-count]
  (assoc evidence
         :relevance
         (if (= relevance-rating-count 0)
           100
           (/ relevance-rating-sum relevance-rating-count))))

(def default-evidence-spec
  [:evidence/id
   :evidence/supports
    {:evidence/claim
     default-claim-spec}])

(defn get-claim-evidence
  ([db claim-ref]
   (get-claim-evidence
    db claim-ref
    default-evidence-spec))
  ([db claim-ref evidence-spec]
   (let [results
         (d/q
          (apply
           conj
           '[:find]
           (list 'pull '?evidence evidence-spec)
           '[(sum ?rating) (sum ?rating-count)
             (sum ?support) (sum ?oppose)
             (sum ?agree) (sum ?disagree)
             :in $ % ?claim
             :with ?uniqueness
             :where
             [?claim :claim/evidence ?evidence]
             (evidence-stats ?evidence ?uniqueness ?agree ?disagree ?support ?oppose ?rating ?rating-count)])
          db rules claim-ref)]
     (for [[evidence relevance-rating-sum relevance-rating-count support-count oppose-count agree-count disagree-count] results]
       (assoc (assoc-evidence-stats evidence relevance-rating-sum relevance-rating-count)
              :evidence/claim (assoc-claim-stats (:evidence/claim evidence) support-count oppose-count agree-count disagree-count)
              ))
     )))
