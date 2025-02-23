(ns truth.domain
  (:require [datomic.client.api :as d]
            [truth.domain.rules :refer [rules]]
            [truth.search :as search]
            [slugger.core :as slugger]))

(defn uuid [] (java.util.UUID/randomUUID))
(defn default-db-id [] (str (uuid)))
(def ->slug slugger/->slug)

(defn new-user [{db-id :db/id id :id
                 username :username email :email
                 password :password}]
  {:db/id (or db-id (default-db-id))
   :user/id (or id (uuid))
   :user/username username
   :user/email email
   :user/password password})

(def char-limit 255)


(defn new-source [{db-id :db/id id :id
                   url :url title :title
                   book :book publication :publication
                   page :page}]
  (-> {:db/id (or db-id (default-db-id))
       :source/id (or id (uuid))}
      (cond->
          book (assoc :source/book book)
          publication (assoc :source/publication publication)
          url (assoc :source/url url)
          title (assoc :source/title title)
          page (assoc :source/page page))))

(defn new-book [{db-id :db/id id :id
                 url :url title :title
                 author :author lccn :lccn}]
  (-> {:db/id (or db-id (default-db-id))
       :book/id (or id (uuid))}
      (cond->
          url (assoc :book/url url)
          title (assoc :book/title title)
          author (assoc :book/author author)
          lccn (assoc :book/lccn lccn))))

(defn new-publication [{db-id :db/id id :id
                        url :url name :name}]
  (-> {:db/id (or db-id (default-db-id))
       :publication/id (or id (uuid))}
      (cond->
          url (assoc :publication/url url)
          name(assoc :publication/name name))))

(defn new-claim [{db-id :db/id id :id
                  body :body creator :creator sources :sources
                  contributors :contributors evidence :evidence
                  votes :votes created-at :created-at featured :featured
                  standalone :standalone quoting :quoting
                  :or {contributors [] evidence [] votes [] sources []
                       created-at (java.util.Date.)
                       standalone false featured false}}]
  (cond->
   {:db/id (or db-id (default-db-id))
    :claim/id (or id (uuid))
    :claim/body body
    :claim/created-at created-at
    :claim/slug (->slug body)
    :claim/creator creator
    :claim/contributors contributors
    :claim/sources sources
    :claim/evidence evidence
    :claim/votes votes
    :claim/standalone standalone
    :claim/featured featured}
    quoting (assoc :claim/quoting quoting)))

(defn new-claim-vote [{db-id :db/id id :id
                       claim :claim voter :voter agreement :agreement}]
  {:db/id (or db-id (default-db-id))
   :claim-vote/id (or id (uuid))
   :claim-vote/voter voter
   :claim-vote/agreement agreement})

(defn new-evidence [{db-id :db/id id :id
                     creator :creator claim :claim supports :supports
                     created-at :created-at
                     votes :votes
                     :or {votes [] created-at (java.util.Date.)}}]
  {:db/id (or db-id (default-db-id))
   :evidence/id (or id (uuid))
   :evidence/creator creator
   :evidence/created-at created-at
   :evidence/claim claim
   :evidence/supports supports
   :evidence/votes votes})

(defn new-relevance-vote [{db-id :db/id id :id
                           evidence :evidence voter :voter rating :rating}]
  {:db/id (or db-id (default-db-id))
   :relevance-vote/id (or id (uuid))
   :relevance-vote/voter voter
   :relevance-vote/rating rating})

;; queries

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
                          :claim/created-at
                          :claim/slug
                          {:claim/sources [:source/url :source/title :source/lccn :source/page
                                           {:source/book [:book/url :book/title :book/author :book/lccn]}
                                           {:source/publication [:publication/url :publication/name]}]}
                          {:claim/quoting [:source/url :source/title :source/lccn :source/page
                                           {:source/book [:book/url :book/title :book/author :book/lccn]}
                                           {:source/publication [:publication/url :publication/name]}]}
                          {:claim/creator [:user/username]}])

(def anon-user-ref [:user/username "anon"])


(defn get-claim-for
  ([db claim-ref user-ref current-user-ref]
   (get-claim-for db claim-ref user-ref current-user-ref default-claim-spec))
  ([db claim-ref user-ref current-user-ref claim-spec]
   (let [[[claim agreement my-agreement]]
         (d/q
          (apply
           conj
           '[:find]
           (list 'pull '?claim claim-spec)
           '(sum ?agreement)
           '(max ?my-agreement)
           '[:in $ % ?claim ?user ?current-user
             :with ?uniqueness
             :where
             (claim-for ?claim ?user ?current-user ?uniqueness ?agreement ?my-agreement)])
          db rules claim-ref (or user-ref anon-user-ref) (or current-user-ref anon-user-ref))]
     (assoc claim :user-agreement agreement :my-agreement my-agreement))))

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

(defn get-featured-claims-as
  ([db user-ref]
   (get-featured-claims-as db user-ref default-claim-spec))
  ([db user-ref claim-spec]
   (->> (d/q
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
            [?claim :claim/featured true]
            (claim-stats-as ?claim ?user ?uniqueness ?agreement ?agreement-count
                            ?support ?oppose ?my-agreement
                            ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)])
         db rules (or user-ref anon-user-ref))
        (map (fn [result] (apply assoc-claim-stats result)))
        (sort-by :claim/created-at)
        reverse)))

(defn get-all-claims-as
  ([db user-ref]
   (get-all-claims-as db user-ref default-claim-spec))
  ([db user-ref claim-spec]
   (->> (d/q
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
            [?claim :claim/standalone true]
            (claim-stats-as ?claim ?user ?uniqueness ?agreement ?agreement-count
                            ?support ?oppose ?my-agreement
                            ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)])
         db rules (or user-ref anon-user-ref))
        (map (fn [result] (apply assoc-claim-stats result)))
        (sort-by :claim/created-at))))

(defn search-claims-as
  ([db search-client user-ref term]
   (search-claims-as db search-client user-ref term default-claim-spec))
  ([db search-client user-ref term claim-spec]
   (let [search-results (:hits (search/search search-client term))
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
  ([db search-client user-ref term]
   (search-claims-as db search-client user-ref term default-claim-spec))
  ([db search-client user-ref term claim-spec]
   (let [search-results (:suggestions (search/suggest search-client term))
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
       (assoc (assoc evidence :user-relevance (when (not (= rating -1)) rating))
              :evidence/claim (assoc
                               (:evidence/claim evidence)
                               :user-agreement (agreement-or-nil agreement)))))))

;; claim validation

(defn reject-long-bodies! [{body :body}]
  (when (< char-limit (count body))
    (throw (ex-info "claim body must be at most 255 characters long" {:body body :count (count body)}))))

(defn reject-invalid-chars! [{body :body}]
  (doall
   (for [c body]
     (when (not (= (java.lang.Character$UnicodeBlock/of c) java.lang.Character$UnicodeBlock/BASIC_LATIN))
       (throw (ex-info (str "claim body must not contain invalid character '"c"'") {:body body :char c}))))))

;; transaction functions

(defn create-claim! [db claim-input creator]
  (reject-long-bodies! claim-input)
  (reject-invalid-chars! claim-input)
  [(new-claim
    (assoc claim-input :creator creator))])

(defn add-evidence! [db claim-id
                     {{id :id :as evidence-claim-input} :claim :as evidence-input}
                     creator]
  [{:claim/id claim-id
    :claim/evidence
    (new-evidence (assoc
                   evidence-input
                   :creator creator
                   :claim (if id
                            [:claim/id id]
                            (first
                             (create-claim! db evidence-claim-input creator)))))}])

(defn vote-on-claim! [db claim-id voter agreement]
  [(if-let [vote-id (get-vote-for-user-and-claim db (:db/id voter) [:claim/id claim-id])]
     {:db/id vote-id :claim-vote/agreement agreement}
     {:claim/id claim-id
      :claim/votes (new-claim-vote
                    {:voter (:db/id voter)
                     :agreement agreement})})])

(defn vote-on-evidence! [db evidence-id voter rating]
  [(if-let [vote-id (get-vote-for-user-and-evidence db (:db/id voter) [:evidence/id evidence-id])]
     {:db/id vote-id :relevance-vote/rating rating}
     {:evidence/id evidence-id
      :evidence/votes (new-relevance-vote
                       {:voter (:db/id voter)
                        :rating rating})})])
