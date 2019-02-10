(ns truth.data
  (:require [datomic.api :as d]
            [truth.domain :refer [new-user new-claim new-claim-vote
                                  new-evidence new-relevance-vote]]))


(def users
  (map
   new-user
   [{:username "travis"
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
    [{:body "Dogs are great"
      :creator travis
      :votes (map
              new-claim-vote
              [{:voter travis :agree true}
               {:voter toby :agree true}])
      :evidence (map
                 new-evidence
                 [{:creator travis
                   :claim (new-claim
                           {:db/id "cute-paws"
                            :body "They have cute paws"
                            :creator travis})
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
                   :claim "cute-paws"
                   :supports true
                   :votes (map
                           new-relevance-vote
                           [{:voter james :rating 100}
                            {:voter travis :rating 66}])}
                  {:creator travis
                   :claim (new-claim
                           {:db/id "dont-like-people"
                            :body "They don't like people"
                            :creator travis})
                   :supports false}
                  {:creator tani
                   :claim "dont-like-people"
                   :supports true}])}])))

(defn load [conn]
  @(d/transact conn users)
  @(d/transact conn pet-claims))
