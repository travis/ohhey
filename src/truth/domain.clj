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

(defn new-evidence [{db-id :db/id creator :creator claim :claim supports :supports}]
  {:db/id (or db-id (uuid))
   :evidence/id (uuid)
   :evidence/creator creator
   :evidence/claim claim
   :evidence/supports supports})

(defn new-relevance-vote [{db-id :db/id evidence :evidence voter :voter rating :rating}]
  {:db/id (or db-id (uuid))
   :relevance-vote/id (uuid)
   :relevance-vote/evidence evidence
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
