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
    ]))

(def james [:user/username "james"])
(def travis [:user/username "travis"])
(def toby [:user/username "toby"])

(def pet-claims

  (concat
   (map
    new-claim
    [{:body "Dogs are great"
      :creator travis
      :evidence (map
                 new-evidence
                 [{:db/id "cute-paws-supports-dogs-are-great"
                   :creator travis
                   :claim (new-claim
                           {:db/id "cute-paws"
                            :body "They have cute paws"
                            :creator travis})
                   :supports true}])}

     {:body "Cats are great"
      :creator james
      :contributors [travis]
      :evidence (map
                 new-evidence
                 [{:db/id "cute-paws-supports-cats-are-great"
                   :creator james
                   :claim "cute-paws"
                   :supports true}
                  {:creator travis
                   :claim (new-claim
                           {:body "They don't like people"
                            :creator travis})
                   :supports false}])}])


   (map
    new-relevance-vote
    [{:evidence "cute-paws-supports-dogs-are-great"
      :voter james
      :rating 33}
     {:evidence "cute-paws-supports-dogs-are-great"
      :voter travis
      :rating 100}
     {:evidence "cute-paws-supports-cats-are-great"
      :voter james
      :rating 100}
     {:evidence "cute-paws-supports-cats-are-great"
      :voter travis
      :rating 66}])))

(def dogs-are-great [:claim/body "Dogs are great"])
(def cats-are-great [:claim/body "Cats are great"])
(def cute-paws [:claim/body "They have cute paws"])
(def dont-like-people [:claim/body "They don't like people"])

(def votes
  (map
   new-claim-vote
   [{:claim dogs-are-great
     :voter travis
     :agree true}
    {:claim dogs-are-great
     :voter james
     :agree false}
    {:claim cats-are-great
     :voter travis
     :agree false}
    {:claim cats-are-great
     :voter james
     :agree true}
    ]))

(defn load [conn]
  @(d/transact conn users)
  @(d/transact conn pet-claims)
  @(d/transact conn votes))
