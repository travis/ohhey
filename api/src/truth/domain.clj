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

(defn get-user-by-username [db username]
  (first (first
          (d/q '[:find (pull ?user [*])
                 :in $ ?username
                 :where
                 [?user :user/username ?username]]
               db username))))

(defn get-vote-for-user-and-claim [db user-ref claim-ref]
  (first
   (first
    (d/q '[:find ?vote
           :in $ ?user ?claim
           :where
           [?vote :claim-vote/voter ?user]
           [?claim :claim/votes ?vote]]
         db user-ref claim-ref))))

(defn get-vote-for-user-and-evidence [db user-ref evidence-ref]
  (first
   (first
    (d/q '[:find ?vote
           :in $ ?user ?evidence
           :where
           [?vote :relevance-vote/voter ?user]
           [?evidence :evidence/votes ?vote]]
         db user-ref evidence-ref))))

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
  '[[(agree-disagree ?claim ?uniqueness ?agree ?disagree ?score)
     (or-join
      [?claim ?uniqueness ?agree ?disagree ?score]
      (and
       [?claim :claim/votes ?vote]
       [(identity ?vote) ?uniqueness]
       (or-join
        [?vote ?agree ?disagree ?score]
        (and
         [?vote :claim-vote/agree true]
         [(ground 1) ?score]
         [(ground 1) ?agree]
         [(ground 0) ?disagree])
        (and
         [?vote :claim-vote/agree false]
         [(ground -1) ?score]
         [(ground 0) ?agree]
         [(ground 1) ?disagree])))
      (and
       [(identity ?claim) ?uniqueness]
       [(ground 0) ?score]
       [(ground 0) ?agree]
       [(ground 0) ?disagree]))]
    [(agree-disagree-as ?claim ?user ?uniqueness ?i-agree ?i-disagree ?score)
     [(ground 0) ?score]
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
    [(support-oppose ?claim ?uniqueness ?support ?oppose ?score)
     (or-join
      [?claim ?uniqueness ?support ?oppose ?score]
      (and
       [?claim :claim/evidence ?evidence]
       [(identity ?evidence) ?uniqueness]
       (or-join
        [?evidence ?support ?oppose ?score]
        (and
         [?evidence :evidence/supports true]
         [(ground 2) ?score]
         [(ground 1) ?support]
         [(ground 0) ?oppose])
        (and
         [?evidence :evidence/supports false]
         [(ground -2) ?score]
         [(ground 0) ?support]
         [(ground 1) ?oppose])))
      (and
       [(ground 0) ?score]
       [(identity ?claim) ?uniqueness]
       [(ground 0) ?support]
       [(ground 0) ?oppose]))]
    [(claim-stats ?claim ?uniqueness ?agree ?disagree ?support ?oppose ?score)
     (or-join
      [?claim ?uniqueness ?agree ?disagree ?support ?oppose ?score]
      (and
       [(ground 0) ?support]
       [(ground 0) ?oppose]
       (agree-disagree ?claim ?uniqueness ?agree ?disagree ?score))
      (and
       [(ground 0) ?agree]
       [(ground 0) ?disagree]
       (support-oppose ?claim ?uniqueness ?support ?oppose ?score)))]
    [(claim-stats-as ?claim ?user ?uniqueness ?agree ?disagree ?support ?oppose ?i-agree ?i-disagree ?score)
     (or
      (and
       [?user]
       (claim-stats ?claim ?uniqueness ?agree ?disagree ?support ?oppose ?score)
       [(ground 0) ?i-agree]
       [(ground 0) ?i-disagree]
       )
      (and
       (agree-disagree-as ?claim ?user ?uniqueness ?i-agree ?i-disagree ?score)
       [(ground 0) ?agree]
       [(ground 0) ?disagree]
       [(ground 0) ?support]
       [(ground 0) ?oppose]
       ))]
    [(my-rating [?relevance-vote ?user] ?my-rating)
     (or-join [?relevance-vote ?user ?my-rating]
              (and
               [?relevance-vote :relevance-vote/voter ?user]
               [?relevance-vote :relevance-vote/rating ?my-rating])
              (and
               (not [?relevance-vote :relevance-vote/voter ?user])
               [(ground -1) ?my-rating]))
     ]
    [(evidence-stats-as ?evidence ?user ?uniqueness ?agree ?disagree ?support ?oppose ?i-agree ?i-disagree ?rating ?rating-count ?my-rating ?score)
     (or-join
      [?evidence ?user ?uniqueness ?agree ?disagree ?support ?oppose ?i-agree ?i-disagree ?rating ?rating-count ?my-rating ?score]
      (and
       [?evidence :evidence/votes ?relevance-vote]
       [(identity ?relevance-vote) ?uniqueness]
       [?relevance-vote :relevance-vote/rating ?rating]
       [(ground 1) ?rating-count]
       (my-rating ?relevance-vote ?user ?my-rating)
       [(ground 0) ?score]
       [(ground 0) ?agree]
       [(ground 0) ?disagree]
       [(ground 0) ?support]
       [(ground 0) ?oppose]
       [(ground 0) ?i-agree]
       [(ground 0) ?i-disagree])
      (and
       [?evidence :evidence/claim ?claim]
       (claim-stats-as ?claim ?user ?uniqueness ?agree ?disagree ?support ?oppose ?i-agree ?i-disagree ?score)
       [(ground -1) ?my-rating]
       [(ground 0) ?rating-count]
       [(ground 0) ?rating]))]
    ])

(defn assoc-claim-stats
  ([claim support-count oppose-count agree-count disagree-count score]
   (assoc-claim-stats claim support-count oppose-count agree-count disagree-count 0 0 score)
   )
  ([claim support-count oppose-count agree-count disagree-count i-agree-count i-disagree-count score]
   (assoc claim
          :support-count support-count :oppose-count oppose-count
          :agree-count agree-count :disagree-count disagree-count
          :agree (< 0 i-agree-count) :disagree (< 0 i-disagree-count)
          :score score)))

(def default-claim-spec '[:db/id
                          :claim/id
                          :claim/body
                          {(:claim/contributors :default []) [:user/username]}
                          {:claim/creator [:user/username]}])

(def anon-user-ref [:user/username "anon"])


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
            '(sum ?score)
            ]
           '[:in $ % ?claim ?user
             :with ?uniqueness
             :where
             (claim-stats-as ?claim ?user ?uniqueness ?agree ?disagree ?support ?oppose ?i-agree ?i-disagree ?score)])
          db rules claim-ref (or user-ref anon-user-ref))]
     (apply assoc-claim-stats result))))

(defn get-parent-claim-as
  ([db claim-ref user-ref]
   (get-parent-claim-as db claim-ref user-ref default-claim-spec))
  ([db evidence-ref user-ref claim-spec]
   (let [result
         (d/q
          (apply
           conj
           '[:find]
           [(list 'pull '?claim claim-spec)
            '(sum ?support) '(sum ?oppose)
            '(sum ?agree) '(sum ?disagree)
            '(sum ?i-agree) '(sum ?i-disagree)
            '(sum ?score)
            ]
           '[:in $ % ?evidence ?user
             :with ?uniqueness
             :where
             [?claim :claim/evidence ?evidence]
             (claim-stats-as ?claim ?user ?uniqueness ?agree ?disagree ?support ?oppose ?i-agree ?i-disagree ?score)])
          db rules evidence-ref (or user-ref anon-user-ref))]
     (apply assoc-claim-stats result))))

(defn get-claim
  ([db claim-ref]
   (get-claim db claim-ref default-claim-spec))
  ([db claim-ref claim-spec]
   (get-claim-as db claim-ref nil claim-spec)))

(defn get-all-claims-as
  ([db user-ref]
   (get-all-claims-as db user-ref default-claim-spec))
  ([db user-ref claim-spec]
   (let [results
         (d/q
          (apply
           conj
           '[:find]
           (list 'pull '?claim claim-spec)
           '[(sum ?support) (sum ?oppose)
             (sum ?agree) (sum ?disagree)
             (sum ?i-agree) (sum ?i-disagree)
             (sum ?score)
             :in $ % ?user
             :with ?uniqueness
             :where
             [?claim :claim/id _]
             (claim-stats-as ?claim ?user ?uniqueness ?agree ?disagree ?support ?oppose ?i-agree ?i-disagree ?score)])
          db rules (or user-ref anon-user-ref))]
     (map (fn [result] (apply assoc-claim-stats result)) results))))

(defn get-all-claims
  ([db]
   (get-all-claims db default-claim-spec))
  ([db claim-spec]
   (get-all-claims-as db nil claim-spec)))


(defn assoc-evidence-stats [evidence relevance-rating-sum relevance-rating-count my-rating]
  (-> evidence
   (assoc :relevance
          (if (= relevance-rating-count 0)
            100
            (/ relevance-rating-sum relevance-rating-count)))
   (cond-> (>= my-rating 0) (assoc :my-relevance-rating my-rating))))

(def default-evidence-spec
  [:evidence/id
   :evidence/supports
    {:evidence/claim
     default-claim-spec}])

(defn get-claim-evidence-as
  ([db claim-ref user-ref] (get-claim-evidence-as db claim-ref user-ref default-evidence-spec))
  ([db claim-ref user-ref evidence-spec]
   (let [results
         (d/q
          (apply
           conj
           '[:find]
           (list 'pull '?evidence evidence-spec)
           '[(sum ?rating) (sum ?rating-count) (max ?my-rating)
             (sum ?support) (sum ?oppose)
             (sum ?agree) (sum ?disagree)
             (sum ?i-agree) (sum ?i-disagree)
             (sum ?score)
             :in $ % ?claim ?user
             :with ?uniqueness
             :where
             [?claim :claim/evidence ?evidence]
             (evidence-stats-as
              ?evidence ?user ?uniqueness
              ?agree ?disagree ?support ?oppose ?i-agree ?i-disagree
              ?rating ?rating-count ?my-rating ?score)])
          db rules claim-ref (or user-ref anon-user-ref))]
     (for [[evidence relevance-rating-sum relevance-rating-count my-relevance-rating
            support-count oppose-count agree-count disagree-count i-agree i-disagree
            score]
           results]
       (assoc (assoc-evidence-stats evidence relevance-rating-sum relevance-rating-count my-relevance-rating)
              :evidence/claim (assoc-claim-stats
                               (:evidence/claim evidence)
                               support-count oppose-count
                               agree-count disagree-count
                               i-agree i-disagree score)
              ))
     )))

(defn get-claim-evidence
  ([db claim-ref] (get-claim-evidence db claim-ref default-evidence-spec))
  ([db claim-ref evidence-spec]
   (get-claim-evidence-as db claim-ref nil evidence-spec)))

(defn get-evidence-as
  ([db evidence-ref user-ref] (get-evidence-as db evidence-ref user-ref default-evidence-spec))
  ([db evidence-ref user-ref evidence-spec]
   (first
    (let [results
          (d/q
           (apply
            conj
            '[:find]
            (list 'pull '?evidence evidence-spec)
            '[(sum ?rating) (sum ?rating-count) (max ?my-rating)
              (sum ?support) (sum ?oppose)
              (sum ?agree) (sum ?disagree)
              (sum ?i-agree) (sum ?i-disagree)
              (sum ?score)
              :in $ % ?evidence ?user
              :with ?uniqueness
              :where
              (evidence-stats-as
               ?evidence ?user ?uniqueness
               ?agree ?disagree ?support ?oppose ?i-agree ?i-disagree
               ?rating ?rating-count ?my-rating
               ?score)])
           db rules evidence-ref (or user-ref anon-user-ref))]
      (for [[evidence relevance-rating-sum relevance-rating-count my-relevance-rating
             support-count oppose-count agree-count disagree-count i-agree i-disagree ?score]
            results]
        (assoc (assoc-evidence-stats evidence relevance-rating-sum relevance-rating-count my-relevance-rating)
               :evidence/claim (assoc-claim-stats
                                (:evidence/claim evidence)
                                support-count oppose-count
                                agree-count disagree-count
                                i-agree i-disagree ?score)
               ))
      ))))
