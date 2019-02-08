(ns truth.data
  (:require [datomic.client.api :as d]
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
  (map
   new-claim
   [{:body "Dogs are great"
     :creator travis

     }
    {:body "They have cute paws"
     :creator travis

     }
    {:body "Cats are great"
     :creator james
     :contributors [travis]
     }
    {:body "They don't like people"
     :creator travis

     }
    ]))


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

(def evidence
  (concat
   (map
    new-evidence
    [{:db/id "cute-paws-supports-dogs-are-great"
      :creator travis
      :target dogs-are-great
      :claim cute-paws
      :supports true}
     {:db/id "cute-paws-supports-cats-are-great"
      :creator james
      :target cats-are-great
      :claim cute-paws
      :supports true}
     {:creator travis
      :target cats-are-great
      :claim dont-like-people
      :supports false}

     ])
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

(defn load [conn]
  (d/transact conn {:tx-data users})
  (d/transact conn {:tx-data pet-claims})
  (d/transact conn {:tx-data votes})
  (d/transact conn {:tx-data evidence}))
