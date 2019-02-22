(ns truth.domain
  (:require [datomic.api :as d]
            [slugger.core :as slugger]))

(defn uuid [] (str (java.util.UUID/randomUUID)))
(def ->slug slugger/->slug)

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
   :claim/slug (->slug body)
   :claim/creator creator
   :claim/contributors contributors
   :claim/evidence  evidence
   :claim/votes votes
   })

(defn new-claim-vote [{db-id :db/id id :id
                       claim :claim voter :voter agreement :agreement}]
  {:db/id (or db-id (uuid))
   :claim-vote/id (or id (uuid))
   :claim-vote/voter voter
   :claim-vote/agreement agreement})

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
  '[[(zero-agreement ?agreement ?agreement-count)
     [(ground 0) ?agreement]
     [(ground 0) ?agreement-count]]
    [(zero-support-oppose ?support ?oppose)
     [(ground 0) ?support]
     [(ground 0) ?oppose]]
    [(zero-rating ?rating ?rating-count)
     [(ground 0) ?rating]
     [(ground 0) ?rating-count]]
    [(nil-my-rating ?my-rating)
     [(ground -1) ?my-rating]]
    [(zero-score ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
     [(ground 0) ?agree-disagree-score]
     [(ground 0) ?support-oppose-score]
     [(ground 0) ?support-oppose-score-component-count]]
    [(nil-my-agreement ?my-agreement)
     [(ground -101) ?my-agreement]]

    [(agree-disagree-score [?claim] ?uniqueness ?score ?score-component-count)
     (or-join [?claim ?uniqueness ?score ?score-component-count]
      (and
       [?claim :claim/votes ?vote]
       [(identity ?vote) ?uniqueness]
       [?vote :claim-vote/agreement ?agreement]
       [(* ?agreement 1) ?score]
       [(ground 1) ?score-component-count])
      (and
       [(identity ?claim) ?uniqueness]
       [(ground 0) ?score]
       [(ground 0) ?score-component-count]
       ))]
    [(evidence-score [?evidence] ?uniqueness ?score ?score-component-count)
     [?evidence :evidence/claim ?claim]
     (or-join [?claim ?agreement-uniqueness ?agreement ?agreement-count]
              (and
               [?claim :claim/votes ?vote]
               [?vote :claim-vote/agreement ?agreement]
               [(ground 1) ?agreement-count]
               [(identity ?vote) ?agreement-uniqueness])
              (and
               (zero-agreement ?agreement ?agreement-count)
               [(identity ?claim) ?agreement-uniqueness]))
     (or-join [?evidence ?rating ?rating-count ?rating-uniqueness]
              (and
               [?evidence :evidence/votes ?relevance-vote]
               [?relevance-vote :relevance-vote/rating ?rating]
               [(ground 1) ?rating-count]
               [(identity ?relevance-vote) ?rating-uniqueness])
              (and
               [(identity ?evidence) ?rating-uniqueness]
               (zero-rating ?rating ?rating-count)))
     (or-join [?evidence ?support-coeff]
      (and
       [?evidence :evidence/supports true]
       [(ground 1) ?support-coeff])
      (and
       [?evidence :evidence/supports false]
       [(ground -1) ?support-coeff]))
     [(list ?rating-uniqueness ?agreement-uniqueness)  ?uniqueness]
     [(* ?agreement ?rating ?support-coeff) ?score]
     [(* ?agreement-count ?rating-count) ?score-component-count]]
    [(support-oppose-score [?claim ?depth] ?uniqueness ?score ?score-component-count)
     (or-join [?claim ?uniqueness ?score ?score-component-count]
              (and
               [?claim :claim/evidence ?evidence]
               (evidence-score ?evidence ?uniqueness ?score ?score-component-count))
              (and
               [(identity ?claim) ?uniqueness]
               [(ground 0) ?score]
               [(ground 0) ?score-component-count]))]
    [(claim-score [?claim ?depth] ?uniqueness ?agree-disagree-score ?score ?support-oppose-component-count)
     [(/ 1 ?depth) ?depth-multiplier]
     [(* ?support-oppose-score ?depth-multiplier) ?score]
     (or [?claim ?depth ?uniqueness ?agree-disagree-score ?support-oppose-score ?support-oppose-component-count]
         (and
          [?depth]
          (agree-disagree-score ?claim ?uniqueness ?agree-disagree-score ?support-oppose-component-count)
          [(ground 0) ?support-oppose-score])
         (and
          (support-oppose-score ?claim ?depth ?uniqueness ?support-oppose-score ?support-oppose-component-count)
          [(ground 0) ?agree-disagree-score]))]

    [(agree-disagree ?claim ?uniqueness ?agreement ?agreement-count)
     (or-join [?claim ?uniqueness ?agreement ?agreement-count]
      (and
       [?claim :claim/votes ?vote]
       [(identity ?vote) ?uniqueness]
       [?vote :claim-vote/agreement ?agreement]
       [(ground 1) ?agreement-count])
      (and
       [(identity ?claim) ?uniqueness]
       (zero-agreement ?agreement ?agreement-count)))]
    [(agree-disagree-as ?claim ?user ?uniqueness ?my-agreement)
     (or-join
      [?claim ?user ?uniqueness ?my-agreement]
      (and
       [?claim :claim/votes ?vote]
       [(identity ?vote) ?uniqueness]
       [?vote :claim-vote/voter ?user]
       [?vote :claim-vote/agreement ?my-agreement])
      (and
       [(identity ?claim) ?uniqueness]
       (nil-my-agreement ?my-agreement)))]
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
       (zero-support-oppose ?support ?oppose)))]
    [(claim-stats [?claim] ?uniqueness ?agreement ?agreement-count ?support ?oppose ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
     (or-join [?claim ?uniqueness ?agreement ?agreement-count ?support ?oppose ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count]
              (and
               (zero-agreement ?agreement ?agreement-count)
               (zero-support-oppose ?support ?oppose)
               (claim-score ?claim 1 ?uniqueness ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count))
              (and
               [(ground 0) ?agree-disagree-score]
               [(ground 0) ?support-oppose-score]
               [(ground 0) ?support-oppose-score-component-count]
               (or-join [?claim ?uniqueness ?agreement ?agreement-count ?support ?oppose]
                        (and
                         (agree-disagree ?claim ?uniqueness ?agreement ?agreement-count)
                         (zero-support-oppose ?support ?oppose))
                        (and
                         (support-oppose ?claim ?uniqueness ?support ?oppose)
                         (zero-agreement ?agreement ?agreement-count)))))]
    [(claim-stats-as ?claim ?user ?uniqueness ?agreement ?agreement-count
                     ?support ?oppose ?my-agreement ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
     (or
      (and
       [?user]
       (claim-stats ?claim ?uniqueness ?agreement ?agreement-count ?support ?oppose ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
       (nil-my-agreement ?my-agreement))
      (and
       (agree-disagree-as ?claim ?user ?uniqueness ?my-agreement)
       (zero-score ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
       (zero-agreement ?agreement ?agreement-count)
       (zero-support-oppose ?support ?oppose)
       ))]
    [(my-rating [?relevance-vote ?user] ?my-rating)
     (or-join [?relevance-vote ?user ?my-rating]
              (and
               [?relevance-vote :relevance-vote/voter ?user]
               [?relevance-vote :relevance-vote/rating ?my-rating])
              (and
               (not [?relevance-vote :relevance-vote/voter ?user])
               (nil-my-rating ?my-rating)))
     ]
    [(evidence-rating [?evidence ?user] ?uniqueness ?rating ?rating-count ?my-rating)
     (or-join [?evidence ?user ?uniqueness ?rating ?rating-count ?my-rating]
              (and
               [?evidence :evidence/votes ?relevance-vote]
               [(identity ?relevance-vote) ?uniqueness]
               [?relevance-vote :relevance-vote/rating ?rating]
               [(ground 1) ?rating-count]
               (my-rating ?relevance-vote ?user ?my-rating)
               )
              (and
               [(identity ?evidence) ?uniqueness]
               (nil-my-rating ?my-rating)
               (zero-rating ?rating ?rating-count)))]
    [(evidence-stats-as [?evidence ?user] ?uniqueness ?agreement ?agreement-count
                        ?support ?oppose ?my-agreement
                        ?rating ?rating-count ?my-rating
                        ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
     (or-join [?evidence ?user ?uniqueness ?agreement ?agreement-count
               ?support ?oppose ?my-agreement
               ?rating ?rating-count ?my-rating
               ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count]
              (and
               [?evidence :evidence/claim ?claim]
               (claim-stats-as ?claim ?user ?uniqueness
                               ?agreement ?agreement-count ?support ?oppose
                               ?my-agreement ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
               (zero-rating ?rating ?rating-count)
               (nil-my-rating ?my-rating)
               )
              (and
               (evidence-rating ?evidence ?user ?uniqueness ?rating ?rating-count ?my-rating)
               (zero-agreement ?agreement ?agreement-count)
               (zero-support-oppose ?support ?oppose)
               (nil-my-agreement ?my-agreement)
               (zero-score ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
               )
              )


     ]
    ])

(defn assoc-claim-stats
  ([claim support-count oppose-count agreement agreement-count agree-disagree-score score score-component-count]
   (assoc-claim-stats claim support-count oppose-count agreement agreement-count -101 agree-disagree-score score score-component-count))
  ([claim support-count oppose-count agreement agreement-count my-agreement agree-disagree-score score score-component-count]
   (assoc claim
          :support-count support-count :oppose-count oppose-count
          :agreement agreement
          :agreement-count agreement-count
          :my-agreement (when (not (= my-agreement -101)) my-agreement)
          :score (+
                  agree-disagree-score
                  (if (= 0 score-component-count)
                    0
                    (/ score
                       (* 100 score-component-count)))))))

(def default-claim-spec '[:db/id
                          :claim/id
                          :claim/body
                          :claim/slug
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
            '(sum ?agreement) '(sum ?agreement-count)
            '(max ?my-agreement)
            '(sum ?agree-disagree-score) '(sum ?support-oppose-score) '(sum ?support-oppose-score-component-count)
            ]
           '[:in $ % ?claim ?user
             :with ?uniqueness
             :where
             (claim-stats-as ?claim ?user ?uniqueness ?agreement ?agreement-count
                             ?support ?oppose ?my-agreement
                             ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)])
          db rules claim-ref (or user-ref anon-user-ref))]
     (apply assoc-claim-stats result))))

(defn get-parent-claim-as
  ([db evidence-ref user-ref]
   (get-parent-claim-as db evidence-ref user-ref default-claim-spec))
  ([db evidence-ref user-ref claim-spec]
   (let [result
         (d/q
          (apply
           conj
           '[:find]
           [(list 'pull '?claim claim-spec)
            '(sum ?support) '(sum ?oppose)
            '(sum ?agreement) '(sum ?agreement-count)
            '(max ?my-agreement)
            '(sum ?agree-disagree-score)
            '(sum ?support-oppose-score) '(sum ?support-oppose-score-component-count)
            ]
           '[:in $ % ?evidence ?user
             :with ?uniqueness
             :where
             [?claim :claim/evidence ?evidence]
             (claim-stats-as ?claim ?user ?uniqueness ?agreement ?agreement-count
                             ?support ?oppose ?my-agreement
                             ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)])
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
             (sum ?agreement) (sum ?agreement-count)
             (max ?my-agreement)
             (sum ?agree-disagree-score)
             (sum ?support-oppose-score) (sum ?support-oppose-score-component-count)
             :in $ % ?user
             :with ?uniqueness
             :where
             [?claim :claim/id _]
             (claim-stats-as ?claim ?user ?uniqueness ?agreement ?agreement-count
                             ?support ?oppose ?my-agreement
                             ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)])
          db rules (or user-ref anon-user-ref))]
     (map (fn [result] (apply assoc-claim-stats result)) results))))

(defn search-claims-as
  ([db user-ref term]
   (search-claims-as db user-ref term default-claim-spec))
  ([db user-ref term claim-spec]
   (let [results
         (d/q
          (apply
           conj
           '[:find]
           '?search-score
           (list 'pull '?claim claim-spec)
           '[(sum ?support) (sum ?oppose)
             (sum ?agreement) (sum ?agreement-count)
             (max ?my-agreement)
             (sum ?agree-disagree-score )
             (sum ?support-oppose-score) (sum ?support-oppose-score-component-count)
             :in $ % ?user ?term
             :with ?uniqueness
             :where
             [(fulltext $ :claim/body ?term) [[?claim _ _ ?search-score]]]
             (claim-stats-as ?claim ?user ?uniqueness ?agreement ?agreement-count
                             ?support ?oppose ?my-agreement
                             ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)])
          db rules (or user-ref anon-user-ref) term)]
     (map (fn [[search-score & claim-result]]
            {:search/score search-score
             :search/result (apply assoc-claim-stats claim-result)}) results))))

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
             (sum ?agreement) (sum ?agreement-count)
             (max ?my-agreement)
             (sum ?agree-disagree-score)
             (sum ?support-oppose-score) (sum ?support-oppose-score-component-count)
             :in $ % ?claim ?user
             :with ?uniqueness
             :where
             [?claim :claim/evidence ?evidence]
             (evidence-stats-as
              ?evidence ?user ?uniqueness
              ?agreement ?agreement-count ?support ?oppose
              ?my-agreement
              ?rating ?rating-count ?my-rating
              ?agree-disagree-score
              ?support-oppose-score ?support-oppose-score-component-count)])
          db rules claim-ref (or user-ref anon-user-ref))]
     (for [[evidence relevance-rating-sum relevance-rating-count my-relevance-rating
            support-count oppose-count agreement agreement-count my-agreement
            agree-disagree-score score score-component-count]
           results]
       (assoc (assoc-evidence-stats evidence relevance-rating-sum relevance-rating-count my-relevance-rating)
              :evidence/claim (assoc-claim-stats
                               (:evidence/claim evidence)
                               support-count oppose-count
                               agreement agreement-count my-agreement
                               agree-disagree-score
                               score score-component-count)
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
              (sum ?agreement) (sum ?agreement-count)
              (max ?my-agreement)
              (sum ?agree-disagree-score)
              (sum ?support-oppose-score) (sum ?support-oppose-score-component-count)
              :in $ % ?evidence ?user
              :with ?uniqueness
              :where
              (evidence-stats-as
               ?evidence ?user ?uniqueness
               ?agreement ?agreement-count ?support ?oppose
               ?my-agreement
               ?rating ?rating-count ?my-rating
               ?agree-disagree-score
               ?support-oppose-score ?support-oppose-score-component-count)])
           db rules evidence-ref (or user-ref anon-user-ref))]
      (for [[evidence relevance-rating-sum relevance-rating-count my-relevance-rating
             support-count oppose-count agreement agreement-count my-agreement
             score score-component-count]
            results]
        (assoc (assoc-evidence-stats evidence relevance-rating-sum relevance-rating-count my-relevance-rating)
               :evidence/claim (assoc-claim-stats
                                (:evidence/claim evidence)
                                support-count oppose-count
                                agreement agreement-count my-agreement
                                score score-component-count)
               ))
      ))))
