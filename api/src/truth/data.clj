 (ns truth.data
  (:require [datomic.api :as d]
            [truth.domain :refer [new-user new-claim new-claim-vote
                                  new-evidence new-relevance-vote]]))


(def users
  (map
   new-user
   [{:username "anon"
     :email "anon@truth.com"}
    {:username "travis"
     :email "travis@truth.com"}
    {:username "james"
     :email "james@truth.com"}
    {:username "toby"
     :email "toby@truth.com"}
    {:username "chuchu"
     :email "chuchu@truth.com"}
    {:username "tani"
     :email "tani@truth.com"}
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
              [{:voter travis :agree true}
               {:voter toby :agree true}])
      :evidence (map
                 new-evidence
                 [{:creator travis
                   :claim (new-claim
                           {:id "animals-are-awesome"
                            :db/id "animals-are-awesome"
                            :body "Animals are awesome"
                            :creator travis
                            :votes (map
                                    new-claim-vote
                                    [{:voter chuchu :agree true}
                                     {:voter toby :agree true}])})
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
              [{:voter toby :agree false}
               {:voter james :agree true}
               {:voter chuchu :agree true}])
      :evidence (map
                 new-evidence
                 [{:creator james
                   :claim "animals-are-awesome"
                   :supports true
                   :votes (map
                           new-relevance-vote
                           [{:voter james :rating 100}
                            {:voter travis :rating 66}])}
                  {:creator travis
                   :supports false
                   :claim (new-claim
                           {:db/id "dont-like-people"
                            :body "They don't like people"
                            :creator travis
                            :evidence (map
                                       new-evidence
                                       [{:creator travis
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
