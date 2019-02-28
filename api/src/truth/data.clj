(ns truth.data
  (:require [datomic.api :as d]
            [datomic.client.api :as cd]
            [truth.search :as search]
            [truth.domain
             :refer [new-user new-claim new-claim-vote
                     new-evidence new-relevance-vote]
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
    ]))

(def james [:user/username "james"])
(def travis [:user/username "travis"])
(def toby [:user/username "toby"])
(def chuchu [:user/username "chuchu"])
(def tani [:user/username "tani"])

(def pet-claims
  (concat
   (map
    new-claim
    [{:id "dogs-are-great"
      :body "Dogs are great"
      :creator travis
      :votes (map
              new-claim-vote
              [{:voter travis :agree true :agreement 100}
               {:voter toby :agree true :agreement 100}])
      :evidence (map
                 new-evidence
                 [{:id "ara-supports-dag"
                   :creator travis
                   :claim (new-claim
                           {:id "animals-are-awesome"
                            :db/id "animals-are-awesome"
                            :body "Animals are awesome"
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
      :creator james
      :contributors [travis]
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
                  {:id "dlp-opposes-cag"
                   :creator travis
                   :supports false
                   :claim (new-claim
                           {:db/id "dont-like-people"
                            :id "dont-like-people"
                            :body "They don't like people"
                            :creator travis
                            :evidence (map
                                       new-evidence
                                       [{:id "cwm-supports-dlp"
                                         :creator travis
                                         :supports true
                                         :claim (new-claim
                                                 {:body "A cat was mean to me"
                                                  :creator travis})}])})}
                  {:creator tani
                   :claim "dont-like-people"
                   :supports true}])}])))

(defn load [conn]
  @(d/transact conn users)
  @(d/transact conn pet-claims))

(defn client-load [conn]
  (cd/transact conn {:tx-data users})
  (cd/transact conn {:tx-data pet-claims}))

(defn add-all-claims-to-search-index [conn search-doc-creds]
  (search/upload-claims search-doc-creds (t/get-all-claims (cd/db conn))))

(defn load-and-index-default-dataset [conn search-doc-creds]
  (client-load conn)
  (add-all-claims-to-search-index conn search-doc-creds))

(defn delete-claims-from-search-index [conn search-doc-creds]
  (search/delete-claims search-doc-creds (t/get-all-claims (cd/db conn))))
