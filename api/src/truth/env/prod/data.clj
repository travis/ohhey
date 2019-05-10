(ns truth.env.prod.data
  (:require
   [truth.domain
    :refer [new-user new-claim new-claim-vote
            new-source new-evidence new-relevance-vote]
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
   [{:body "Dogs are great."
     :standalone true
     :created-at #inst "2019-05-01T12:00:00Z"
     :creator toby
     :votes (map
             new-claim-vote
             [{:voter travis :agree true :agreement 100}
              {:voter toby :agree true :agreement 100}])
     :evidence (map
                new-evidence
                [{:creator travis
                  :claim "animals-are-awesome"
                  :supports true
                  :votes (map
                          new-relevance-vote
                          [{:voter travis :rating 100}])}])}
    {:db/id "animals-are-awesome"
     :body "Animals are awesome."
     :standalone true
     :created-at #inst "2019-05-01T12:01:00Z"
     :creator toby
     :votes (map
             new-claim-vote
             [{:voter toby :agree true :agreement 100}])}
    {:body "Cats are great."
     :standalone true
     :created-at #inst "2019-05-01T12:00:00Z"
     :creator toby
     :votes (map
             new-claim-vote
             [{:voter travis :agree true :agreement 100}
              {:voter toby :agree true :agreement 100}])
     :evidence (map
                new-evidence
                [{:creator toby
                  :claim "animals-are-awesome"
                  :supports true
                  :votes (map
                          new-relevance-vote
                          [{:voter toby :rating 100}])}
                 {:creator toby
                  :supports true
                  :claim (new-claim
                          {:body "It feels nice to pet a cat."
                           :created-at #inst "2019-05-01T12:02:00Z"
                           :creator toby})}
                 {:creator toby
                  :supports true
                  :claim (new-claim
                          {:body "Cats are very cute."
                           :created-at #inst "2019-05-01T12:03:00Z"
                           :creator toby})}
                 {:creator toby
                  :supports true
                  :claim (new-claim
                          {:body "Cats are very interesting."
                           :created-at #inst "2019-05-01T12:04:00Z"
                           :creator toby})}
                 {:creator toby
                  :supports false
                  :claim (new-claim
                          {:body "Cats can be jerks."
                           :created-at #inst "2019-05-01T12:05:00Z"
                           :creator toby})}
                 {:creator toby
                  :supports false
                  :claim (new-claim
                          {:body "Cats sometimes destroy furniture."
                           :created-at #inst "2019-05-01T12:06:00Z"
                           :creator toby})}
                 ])}
    {:body "The only thing I know for certain is that I exist."
     :standalone true
     :created-at #inst "2019-05-04T12:00:00Z"
     :creator toby
     :sources (map new-source [{:url "https://en.wikipedia.org/wiki/Cogito,_ergo_sum"}])
     :evidence (map
                new-evidence
                [{:creator toby
                  :claim (new-claim
                          {:body "It is possible that everything I experience is a simulation."
                           :created-at #inst "2019-05-04T12:01:00Z"
                           :creator toby
                           :sources (map new-source [{:title "The Matrix"
                                                      :url "https://www.imdb.com/title/tt0133093/"}])})
                  :supports true}
                 {:creator toby
                  :claim (new-claim
                          {:body "Even if everything I experience is a simulation, I am still experiencing it, therefore I must exist."
                           :created-at #inst "2019-05-04T12:02:00Z"
                           :creator toby
                           :sources (map new-source [{:url "https://en.wikipedia.org/wiki/Cogito,_ergo_sum"}])})
                  :supports true
                  }])
     }
    {:body "God exists."
     :standalone true
     :created-at #inst "2019-05-09T17:25:00Z"
     :creator toby
     :sources (map new-source [{:url "https://en.wikipedia.org/wiki/Existence_of_God"}])
     :evidence (map
                new-evidence
                [{:creator toby
                  :supports true
                  :claim (new-claim
                          {:body "It is impossible for an entity to cause itself to be created, and it is impossible for there to be an infinite chain of causes. Therefore, there must be a first cause, itself uncaused."
                           :creator toby
                           :sources (map new-source [{:url "https://en.wikipedia.org/wiki/Unmoved_mover#First_cause"}])
                           :created-at #inst "2019-05-09T17:30:00Z"})}
                 {:creator toby
                  :supports false
                  :claim (new-claim
                          {:body "It is possible to explain the creation of the universe purely within the realm of science, so the idea of a divine being is unnecessary. "
                           :creator toby
                           :sources (map new-source [{:url "https://en.wikipedia.org/wiki/The_Grand_Design_(book)"}])
                           :created-at #inst "2019-05-09T17:31:00Z"})}])}
    ]))
