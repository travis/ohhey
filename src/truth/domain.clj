(ns truth.domain
  (:require [datomic.api :as d]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn new-user [{db-id :db/id username :username email :email}]
  {:db/id (or db-id (uuid))
   :user/id (uuid)
   :user/username username
   :user/email email})

(defn new-claim [{db-id :db/id body :body creator :creator
                  contributors :contributors evidence :evidence
                  votes :votes
                  :or {contributors [] evidence [] votes []}}]
  {:db/id (or db-id (uuid))
   :claim/id (uuid)
   :claim/body body
   :claim/creator creator
   :claim/contributors contributors
   :claim/evidence  evidence
   :claim/votes votes
   })

(defn new-claim-vote [{db-id :db/id claim :claim voter :voter agree :agree}]
  {:db/id (or db-id (uuid))
   :claim-vote/id (uuid)
   :claim-vote/voter voter
   :claim-vote/agree agree})

(defn new-evidence [{db-id :db/id creator :creator claim :claim supports :supports
                     votes :votes
                     :or {votes []}}]
  {:db/id (or db-id (uuid))
   :evidence/id (uuid)
   :evidence/creator creator
   :evidence/claim claim
   :evidence/supports supports
   :evidence/votes votes})

(defn new-relevance-vote [{db-id :db/id evidence :evidence voter :voter rating :rating}]
  {:db/id (or db-id (uuid))
   :relevance-vote/id (uuid)
   :relevance-vote/voter voter
   :relevance-vote/rating rating})


(defn get-user-by-email [db email]
  (first (first
          (d/q '[:find (pull ?user [*])
                 :in $ ?email
                 :where
                 [?user :user/email ?email]]
               db email))))

(defn get-all-claims [db]
  (map first
   (d/q
    '[:find (pull ?claim  [*
                           {:claim/contributors [:user/username]}
                           {:claim/creator [:user/username]}
                           {:claim/evidence [:evidence/supports {:evidence/claim [:claim/body]}]}
                           {:claim/votes [:claim-vote/agree {:claim-vote/voter [:user/username]}]}
                           ])
      :where [?claim :claim/id _]]
    db)))

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
      (d/q '[:find (pull ?evidence-claim [* {:claim/creator [:user/username]}])
             :in $ ?claim [?supports ...]
             :where
             [?claim :claim/evidence ?evidence]
             [?evidence :evidence/supports ?supports]
             [?evidence :evidence/claim ?evidence-claim]]
           db claim supports)))

(defn get-claim [db claim-ref]
  (let [[claim support-count oppose-count agree-count disagree-count]
        (d/q
         '[:find
           [(pull ?claim  [:claim/body
                           {(:claim/contributors :default []) [:user/username]}
                           {:claim/creator [:user/username]}

                           ])
            (sum ?support) (sum ?oppose)
            (sum ?agree) (sum ?disagree)]
           :in $ ?claim
           :with ?uniqueness
           :where
           (or-join [?claim ?uniqueness ?agree ?disagree ?support ?oppose]
                    (and
                     [?claim :claim/votes ?agree-vote]
                     [(identity ?agree-vote) ?uniqueness]
                     [?agree-vote :claim-vote/agree true]
                     [(ground 1) ?agree]
                     [(ground 0) ?disagree]
                     [(ground 0) ?support]
                     [(ground 0) ?oppose])
                    (and
                     [?claim :claim/votes ?disagree-vote]
                     [(identity ?disagree-vote) ?uniqueness]
                     [?disagree-vote :claim-vote/agree false]
                     [(ground 0) ?agree]
                     [(ground 1) ?disagree]
                     [(ground 0) ?support]
                     [(ground 0) ?oppose])
                    (and
                     [?claim :claim/evidence ?supporting-evidence]
                     [(identity ?supporting-evidence) ?uniqueness]
                     [?supporting-evidence :evidence/supports true]
                     [(ground 1) ?support]
                     [(ground 0) ?oppose]
                     [(ground 0) ?agree]
                     [(ground 0) ?disagree])
                    (and
                     [?claim :claim/evidence ?opposing-evidence]
                     [(identity ?opposing-evidence) ?uniqueness]
                     [?opposing-evidence :evidence/supports false]
                     [(ground 0) ?support]
                     [(ground 1) ?oppose]
                     [(ground 0) ?agree]
                     [(ground 0) ?disagree]))]
         db claim-ref)]

    (assoc claim
           :support-count support-count :oppose-count oppose-count
           :agree-count agree-count :disagree-count disagree-count)))
