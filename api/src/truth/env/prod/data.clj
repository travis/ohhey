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

(def longform-claims
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
     :featured true
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
     :featured true
     :created-at #inst "2019-05-04T12:00:00Z"
     :creator toby
     :sources (map new-source [{:title "Cogito, ergo sum"
                                :url "https://en.wikipedia.org/wiki/Cogito,_ergo_sum"}])
     :evidence (map
                new-evidence
                [{:creator toby
                  :claim (new-claim
                          {:body "The real world seems very real."
                           :created-at #inst "2019-05-10T12:01:00Z"
                           :creator toby})
                  :supports false}
                 {:creator toby
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
                           :sources (map new-source [{:title "Cogito, ergo sum"
                                                      :url "https://en.wikipedia.org/wiki/Cogito,_ergo_sum"}])})
                  :supports true
                  }])
     }
    {:body "God exists."
     :standalone true
     :featured true
     :created-at #inst "2019-05-09T17:25:00Z"
     :creator toby
     :sources (map new-source [{:title "Existence of God"
                                :url "https://en.wikipedia.org/wiki/Existence_of_God"}])
     :evidence (map
                new-evidence
                [{:creator toby
                  :supports true
                  :claim (new-claim
                          {:body "It is impossible for an entity to cause itself to be created, and it is impossible for there to be an infinite chain of causes. Therefore, there must be a first cause, itself uncaused."
                           :creator toby
                           :sources (map new-source [{:title "First cause"
                                                      :url "https://en.wikipedia.org/wiki/Unmoved_mover#First_cause"}])
                           :created-at #inst "2019-05-09T17:30:00Z"})}
                 {:creator toby
                  :supports false
                  :claim (new-claim
                          {:body "It is possible to explain the creation of the universe purely within the realm of science, so the idea of a divine being is unnecessary. "
                           :creator toby
                           :sources (map new-source [{:title "The Grand Design"
                                                      :url "https://en.wikipedia.org/wiki/The_Grand_Design_(book)"}])
                           :created-at #inst "2019-05-09T17:31:00Z"})}])}
    {:body "Illegal immigration does not increase crime."
     :standalone true
     :created-at #inst "2019-05-14T13:09:00Z"
     :creator toby
     :sources (map new-source [{:title "Is There a Connection Between Undocumented Immigrants and Crime?"
                               :url "https://www.nytimes.com/2019/05/13/upshot/illegal-immigration-crime-rates-research.html"}])
     :evidence (map
                new-evidence
                [{:creator toby
                  :supports true
                  :claim (new-claim
                          {:body "There is no sign of a strong link between undocumented immigrants and crime."
                           :creator toby
                           :created-at #inst "2019-05-14T13:10:00Z"
                           :sources (map new-source [{:title "Is There a Connection Between Undocumented Immigrants and Crime?"
                                                      :url "https://www.nytimes.com/2019/05/13/upshot/illegal-immigration-crime-rates-research.html"}])
                           })}

                 {:creator toby
                  :supports true
                  :claim (new-claim
                          {:body "A 2018 study published in the journal \"Criminology\" found that \"undocumented immigration does not increase violence. Rather, the relationship between undocumented immigration and violent crime is generally negative.\""
                           :creator toby
                           :created-at #inst "2019-05-14T13:11:00Z"
                           :sources (map new-source [{:title "Does Undocumented Immigration Increase Violent Crime?"
                                                      :url "https://onlinelibrary.wiley.com/doi/full/10.1111/1745-9125.12175?fbclid=IwAR05w2ks-G8TSlg6F1tKJghcogd-jkweNHgSFtDLE_oZMzceveUyr2NBCCo"}])})}
                 {:creator toby
                  :supports true
                  :claim (new-claim
                          {:body "Undocumented immigrants in Texas are convicted of homicide, larceny, and sex crimes at lower rates than native-born Americans."
                           :creator toby
                           :created-at #inst "2019-05-14T13:12:00Z"
                           :sources (map new-source [{:title "Illegal Immigrants and Crime – Assessing the Evidence"
                                                      :url "https://www.cato.org/blog/illegal-immigrants-crime-assessing-evidence"}])})}
                 {:creator toby
                  :supports true
                  :claim (new-claim
                          {:body "Undocumented immigrants have far more to lose from being arrested than citizens or legal immigrants, and are therefore less likely to break the law once they are in the country."
                           :creator toby
                           :created-at #inst "2019-05-14T13:12:00Z"})}
                 {:creator toby
                  :supports false
                  :claim (new-claim
                          {:body "The US federal government's State Criminal Alien Assistence Program data suggest undocumented immigrants commit crime at a higher rate than citizens and documented immigrants."
                           :creator toby
                           :created-at #inst "2019-05-14T13:13:00Z"
                           :sources (map new-source [{:title "SCAAP Data Suggest Illegal Aliens Commit Crime at a Much Higher Rate Than Citizens and Lawful Immigrants"
                                                      :url "https://www.fairus.org/issue/illegal-immigration/scaap-data-suggest-illegal-aliens-commit-crime-much-higher-rate-citizens"}])
                           :evidence (map
                                      new-evidence
                                      [{:creator toby
                                        :supports false
                                        :claim (new-claim
                                                {:body "The Federation for American Immigration Reform report on undocumented immigrant incarceration rates is poorly contrived and terribly executed."
                                                 :creator toby
                                                 :created-at #inst "2019-05-14T13:14:00Z"
                                                 :sources (map new-source [{:title "FAIR SCAAP Crime Report Has Many Serious Problems"
                                                                            :url "https://www.cato.org/blog/fair-scaap-crime-report-has-many-serious-problems"}])
                                                 :evidence (map
                                                            new-evidence
                                                            [{:creator toby
                                                              :supports true
                                                              :claim (new-claim
                                                                      {:body "The Federation for American Immigration Reform report on undocumented immigrant incarceration rates does not state the year or period of years it is analyzing."
                                                                       :creator toby
                                                                       :created-at #inst "2019-05-14T13:14:00Z"
                                                                       :sources (map new-source [{:title "FAIR SCAAP Crime Report Has Many Serious Problems"
                                                                                                  :url "https://www.cato.org/blog/fair-scaap-crime-report-has-many-serious-problems"}])})}
                                                             {:creator toby
                                                              :supports true
                                                              :claim (new-claim
                                                                      {:body "The Federation for American Immigration Reform report on undocumented immigrant incarceration rates uses SCAAP data incorrectly when calculating incarceration rates."
                                                                       :creator toby
                                                                       :created-at #inst "2019-05-14T13:14:00Z"
                                                                       :sources (map new-source [{:title "FAIR SCAAP Crime Report Has Many Serious Problems"
                                                                                                  :url "https://www.cato.org/blog/fair-scaap-crime-report-has-many-serious-problems"}])})}])})}])})
                  }])}
    ]))

(def toby-claim)

(defn toby-relevance-vote [vote]
  (new-relevance-vote (merge {:voter toby} vote)))

(defn toby-evidence [{claim :claim votes :votes
                      :as evidence
                      :or {votes []}}]
  (new-evidence (-> evidence
                    (assoc :creator toby)
                    (assoc :claim (toby-claim claim))
                    (assoc :votes (map toby-relevance-vote votes)))))

(defn toby-claim-vote [vote]
  (new-claim-vote (merge {:voter toby} vote)))

(defn toby-claim [{sources :sources evidence :evidence votes :votes
                   :as claim
                   :or {sources [] evidence [] votes []}}]
  (new-claim (-> claim
                 (assoc :creator toby)
                 (assoc :sources (map new-source sources))
                 (assoc :evidence (map toby-evidence evidence))
                 (assoc :votes (map toby-claim-vote votes)))))

(def shorthand-claims
  (map
   toby-claim
   [{:body "Objective reality does not exist."
     :created-at #inst "2019-05-14T15:33:00Z"
     :sources [{:title "A quantum experiment suggests there’s no such thing as objective reality"
                :url "https://www.technologyreview.com/s/613092/a-quantum-experiment-suggests-theres-no-such-thing-as-objective-reality/"}]
     :votes [{:agreement 100}]
     :evidence [{:supports true
                 :claim {:body "Two observers of a quantum physics experiment experienced different, conflicting realities."
                         :created-at #inst "2019-05-14T15:36:00Z"
                         :sources [{:title "Experimental rejection of observer-independence in the quantum world"
                                    :url "https://arxiv.org/abs/1902.05080"}]}
                 :votes {:rating 100}}]}]))

(def claims (concat longform-claims shorthand-claims))
