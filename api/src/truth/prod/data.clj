(ns truth.prod.data
  (:require
   [truth.domain
    :refer [new-user new-claim new-claim-vote
            new-evidence new-relevance-vote]
    :as t]))

(def users
  (map
   new-user
   [{:username "anon"
     :email "anon@ohhey.fyi"
     :password ""}
    {:username "toby"
     :email "toby@ohhey.fyi"
     :password "tobes"}
    {:username "travis"
     :email "travis@ohhey.fyi"
     :password "cats"}]))

(def travis [:user/username "travis"])
(def toby [:user/username "toby"])

(def claims
  (map
   new-claim
   [{:body "Dogs are great"
     :standalone true
     :created-at #inst "2019-05-01T12:00:00Z"
     :creator toby
     :votes (map
             new-claim-vote
             [{:voter travis :agree true :agreement 100}
              {:voter toby :agree true :agreement 100}])
     :evidence (map
                new-evidence
                [{:id "ara-supports-dag"
                  :creator travis
                  :claim "animals-are-awesome"
                  :supports true
                  :votes (map
                          new-relevance-vote
                          [{:voter travis :rating 100}])}])
     }
    {:db/id "animals-are-awesome"
     :body "Animals are awesome"
     :standalone true
     :created-at #inst "2019-05-01T12:01:00Z"
     :creator toby
     :votes (map
             new-claim-vote
             [{:voter toby :agree true :agreement 100}])}
    {:body "Cats are great"
     :standalone true
     :created-at #inst "2019-05-01T12:00:00Z"
     :creator toby
     :votes (map
             new-claim-vote
             [{:voter travis :agree true :agreement 100}
              {:voter toby :agree true :agreement 100}])
     :evidence (map
                new-evidence
                [{:id "ara-supports-dag"
                  :creator travis
                  :claim "animals-are-awesome"
                  :supports true
                  :votes (map
                          new-relevance-vote
                          [{:voter toby :rating 100}])}])}]))
