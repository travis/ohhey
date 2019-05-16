(ns truth.data
  (:require [datomic.api :as d]
            [datomic.client.api :as cd]
            [truth.search :as search]
            [truth.features :as features]
            [truth.domain
             :refer [new-user new-claim new-claim-vote
                     new-evidence new-relevance-vote new-source uuid]
             :as t]))

(def users
  (map
   new-user
   [{:username "anon"
     :email "anon@truth.com"
     :password ""}
    {:username "travis"
     :email "travis@truth.com"
     :password "cats"}
    {:username "james"
     :email "james@truth.com"
     :password "aptp"}
    {:username "toby"
     :email "toby@truth.com"
     :password "tobes"}
    {:username "chuchu"
     :email "chuchu@truth.com"
     :password "keai"}
    {:username "tani"
     :email "tani@truth.com"
     :password "flow"}
    {:username "ian"
     :email "ian@truth.com"
     :password "lisp"}
    {:username "danny"
     :email "danny@truth.com"
     :password "twc"}
    {:username "nikkia"
     :email "nikkia@truth.com"
     :password "nickles"}]))

(def james [:user/username "james"])
(def travis [:user/username "travis"])
(def toby [:user/username "toby"])
(def chuchu [:user/username "chuchu"])
(def tani [:user/username "tani"])

(def ids
  {:dogs-are-great #uuid "0489a7e5-9fc3-46ec-bdbf-ff4c3794e90e"
   :ara-supports-dag #uuid "31e3b92e-4cb1-450e-be69-ab3326a3ea23"
   :animals-are-awesome #uuid "2fdddcd7-bf80-45b2-b695-cc2a369736a5"
   :dlp-opposes-cag #uuid "7e8907be-b366-4f1c-9440-ca87b4e58744"
   :dont-like-people #uuid "4d574b3e-794b-41b9-b997-2d9fc8909316"
   :cwm-supports-dlp #uuid "fa65cec0-7d5b-48d3-b8f4-08f2f018ec8f"})

(def pet-claims
  (concat
   (map
    new-claim
    [{:id (ids :dogs-are-great)
      :body "Dogs are great"
      :standalone true
      :featured true
      :created-at #inst "2018-02-02T12:00:00Z"
      :creator travis
      :votes (map
              new-claim-vote
              [{:voter travis :agree true :agreement 100}
               {:voter toby :agree true :agreement 100}])
      :evidence (map
                 new-evidence
                 [{:id (ids :ara-supports-dag)
                   :creator travis
                   :claim (new-claim
                           {:id (ids :animals-are-awesome)
                            :db/id "animals-are-awesome"
                            :body "Animals are awesome"
                            :standalone true
                            :created-at #inst "2018-02-02T12:01:00Z"
                            :creator travis
                            :votes (map
                                    new-claim-vote
                                    [{:voter chuchu :agree true :agreement 100}
                                     {:voter toby :agree true :agreement 100}])})
                   :supports true
                   :votes (map
                           new-relevance-vote
                           [{:voter james :rating 33}
                            {:voter travis :rating 100}])}])}

     {:body "Cats are great"
      :standalone true
      :created-at #inst "2018-02-02T12:02:00Z"
      :creator james
      :contributors [travis]
      :sources (map new-source [{:title "Wikipedia Cat"
                                 :url "https://en.wikipedia.org/wiki/Cat"}
                                {:title "Catster Magazine"
                                 :url "http://subscribe.catster.com/Catster/Magazine"}])
      :votes (map
              new-claim-vote
              [{:voter toby :agree false :agreement -100}
               {:voter james :agree true :agreement 100}
               {:voter chuchu :agree true :agreement 100}])
      :evidence (map
                 new-evidence
                 [{:creator james
                   :claim "animals-are-awesome"
                   :supports true
                   :votes (map
                           new-relevance-vote
                           [{:voter james :rating 100}
                            {:voter travis :rating 66}])}
                  {:id (ids :dlp-opposes-cag)
                   :creator travis
                   :supports false
                   :claim (new-claim
                           {:db/id "dont-like-people"
                            :id (ids :dont-like-people)
                            :body "They don't like people"
                            :created-at #inst "2018-02-02T12:03:00Z"
                            :creator travis
                            :evidence (map
                                       new-evidence
                                       [{:id (ids :cwm-supports-dlp)
                                         :creator travis
                                         :supports true
                                         :claim (new-claim
                                                 {:body "A cat was mean to me"
                                                  :created-at #inst "2018-02-02T12:04:00Z"

                                                  :creator travis})}])})}
                  {:creator tani
                   :claim "dont-like-people"
                   :supports true}])}])))

(def claims pet-claims)

(defn load [conn]
  @(d/transact conn users)
  @(d/transact conn pet-claims))

(defn client-load [conn]
  (cd/transact conn {:tx-data users})
  (cd/transact conn {:tx-data pet-claims}))

(defn add-all-claims-to-search-index [conn search-doc-creds]
  (search/upload-claims search-doc-creds (t/get-all-claims (cd/db conn))))

(defn load-and-index-default-dataset [conn search-doc-creds]
  {:db (client-load conn)
   :search (when (features/search-enabled?)
               (add-all-claims-to-search-index conn search-doc-creds))})

(defn delete-claims-from-search-index [conn search-doc-creds]
  (search/delete-claims search-doc-creds (t/get-all-claims (cd/db conn))))

(defn clear-and-delete-database [conn search-client client db-spec]
  {:search (when (features/search-enabled?)
             (delete-claims-from-search-index conn search-client))
   :db (cd/delete-database client db-spec)})
