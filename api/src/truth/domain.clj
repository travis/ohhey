(ns truth.domain
  (:require [datomic.client.api :as d]
            [truth.domain.rules :refer [rules]]
            [truth.search :as search]
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

(defn agreement-or-nil [agreement]
  (when (not (= agreement -101)) agreement))

(defn assoc-claim-stats
  ([claim support-count oppose-count agreement agreement-count agree-disagree-score score score-component-count]
   (assoc-claim-stats claim support-count oppose-count agreement agreement-count -101 agree-disagree-score score score-component-count))
  ([claim support-count oppose-count agreement agreement-count my-agreement agree-disagree-score score score-component-count]
   (assoc claim
          :support-count support-count :oppose-count oppose-count
          :agreement agreement
          :agreement-count agreement-count
          :my-agreement (agreement-or-nil my-agreement)
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
                          {:claim/creator [:user/username]}])

(def anon-user-ref [:user/username "anon"])


(defn get-claim-for
  ([db claim-ref user-ref]
   (get-claim-for db claim-ref user-ref default-claim-spec))
  ([db claim-ref user-ref claim-spec]
   (let [[[claim agreement]]
         (d/q
          (apply
           conj
           '[:find]
           (list 'pull '?claim claim-spec)
           '(sum ?agreement)
           '[:in $ % ?claim ?user
             :with ?uniqueness
             :where
             (claim-for ?claim ?user ?uniqueness ?agreement)])
          db rules claim-ref (or user-ref anon-user-ref))]
     (assoc claim :agreement agreement))))

(defn get-claim-as
  ([db claim-ref user-ref]
   (get-claim-as db claim-ref user-ref default-claim-spec))
  ([db claim-ref user-ref claim-spec]
   (let [[result]
         (d/q
          (apply
           conj
           '[:find]
           (list 'pull '?claim claim-spec)
           '(sum ?support) '(sum ?oppose)
           '(sum ?agreement) '(sum ?agreement-count)
           '(max ?my-agreement)
           '(sum ?agree-disagree-score) '(sum ?support-oppose-score) '(sum ?support-oppose-score-component-count)
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
   (let [[result]
         (d/q
          (apply
           conj
           '[:find]
           (list 'pull '?claim claim-spec)
           '(sum ?support) '(sum ?oppose)
           '(sum ?agreement) '(sum ?agreement-count)
           '(max ?my-agreement)
           '(sum ?agree-disagree-score)
           '(sum ?support-oppose-score) '(sum ?support-oppose-score-component-count)
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
  ([db search-creds user-ref term]
   (search-claims-as db search-creds user-ref term default-claim-spec))
  ([db search-creds user-ref term claim-spec]
   (let [search-results (:hit (:hits (search/search search-creds term)))
         results
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
             :in $ % ?user [?claim-id ...]
             :with ?uniqueness
             :where
             [?claim :claim/id ?claim-id]
             [(ground 0) ?search-score]
             (claim-stats-as ?claim ?user ?uniqueness ?agreement ?agreement-count
                             ?support ?oppose ?my-agreement
                             ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)])
          db rules (or user-ref anon-user-ref) (map :id search-results))]
     (map (fn [[search-score & claim-result]]
            {:search/score search-score
             :search/result (apply assoc-claim-stats claim-result)}) results))))

(defn suggest-claims-as
  ([db search-creds user-ref term]
   (search-claims-as db search-creds user-ref term default-claim-spec))
  ([db search-creds user-ref term claim-spec]
   (let [search-results (:suggestions (:suggest (search/suggest search-creds term)))
         results
         (d/q
          (apply
           conj
           '[:find]
           '?search-score
           (list 'pull '?claim claim-spec)
           '[(max ?my-agreement)
             :in $ % ?user [?claim-id ...]
             :with ?uniqueness
             :where
             [?claim :claim/id ?claim-id]
             [(ground 0) ?search-score]
             (my-agreement ?claim ?user ?uniqueness ?my-agreement)])
          db rules (or user-ref anon-user-ref) (map :id search-results))]
     (map (fn [[search-score claim my-agreement]]
            {:search/score search-score
             :search/result (assoc claim :my-agreement my-agreement)})
          results))))

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

(defn merge-gcef-results [results]
  (reduce (fn [m [evidence user rating agreement :as result]]
            (assoc m (:evidence/id evidence) result))
          results
          {}))

(defn get-claim-evidence-for
  ([db claim-ref user-ref] (get-claim-evidence-for db claim-ref user-ref default-evidence-spec))
  ([db claim-ref user-ref evidence-spec]
   (let [results
         (d/q
          (apply
           conj
           '[:find]
           (list 'pull '?evidence evidence-spec)
           (list 'pull '?user [:user/username])
           '[(max ?rating)
             (max ?agreement)
             :in $ % ?claim ?user
             :with ?uniqueness
             :where
             (evidence-for
              ?claim ?user
              ?evidence ?uniqueness
              ?rating ?agreement)])
          db rules claim-ref (or user-ref anon-user-ref))]
     (for [[evidence {username :user/username} rating agreement]
           results]
       (assoc (assoc evidence :relevance (when (not (= rating -1)) rating))
              :evidence/claim (assoc
                               (:evidence/claim evidence)
                               :agreement (agreement-or-nil agreement)))))))
