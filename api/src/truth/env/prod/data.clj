(ns truth.env.prod.data
  (:require
   [clojure.walk :refer [postwalk]]
   [truth.domain
    :refer [new-user new-claim new-claim-vote new-book
            new-source new-evidence new-relevance-vote]
    :as t]))

(def users
  (map
   new-user
   [{:username "anon"
     :email "anon@ohhey.fyi"
     :password ""}
    {:db/id "toby"
     :username "toby"
     :email "toby@ohhey.fyi"
     :password "tobes"}
    {:db/id "travis"
     :username "travis"
     :email "travis@ohhey.fyi"
     :password "cats"}
    {:username "rylan"
     :email "rylan@colab.coop"
     :password "ithaca"}
    {:username "mckenzie"
     :email "mckenzie@colab.coop"
     :password "cayuga"}
    {:username "ethan"
     :email "ethan@colab.coop"
     :password "chester"}]))

(def travis "travis")
(def toby "toby")

(defprotocol Shorthand
  (->claim [data])
  (->evidence [data])
  (->evidence-list [data]))

(defn toby-relevance-vote [vote]
  (new-relevance-vote (merge {:voter toby} vote)))

(defn toby-evidence [{claim :claim votes :votes supports :supports
                      :as evidence
                      :or {votes [] supports true}}]
  (new-evidence (-> evidence
                    (assoc :supports supports)
                    (assoc :creator toby)
                    (assoc :claim (->claim claim))
                    (assoc :votes (map toby-relevance-vote votes)))))

(defn toby-claim-vote [vote]
  (new-claim-vote (merge {:voter toby} vote)))

(defn toby-claim [{sources :sources votes :votes creator :creator
                   evidence :evidence supported-by :supported-by countered-by :countered-by
                   agree :agree
                   :as claim
                   :or {sources [] evidence [] votes [] creator toby}}]
  (new-claim (-> claim
                 (assoc :creator creator)
                 (assoc :sources (map new-source sources))
                 (assoc :evidence (->evidence-list evidence))
                 (assoc :votes (map toby-claim-vote
                                    (if (nil? agree)
                                      votes
                                      (cons {:agreement (if agree 100 -100)} votes)))))))

(defn map->evidence-list [{supporting-evidence :support opposing-evidence :oppose}]
  (concat (map (fn [evidence] (assoc (->evidence evidence) :evidence/supports true))
               supporting-evidence)
          (map (fn [evidence] (assoc (->evidence evidence) :evidence/supports false))
               opposing-evidence)))

(extend-protocol Shorthand
  clojure.lang.PersistentArrayMap
  (->claim [claim] (toby-claim claim))
  (->evidence [evidence] (toby-evidence evidence))
  (->evidence-list [evidence-map] (map->evidence-list evidence-map))
  clojure.lang.PersistentHashMap
  (->claim [claim] (toby-claim claim))
  (->evidence [evidence] (toby-evidence evidence))
  (->evidence-list [evidence-map] (map->evidence-list evidence-map))

  clojure.lang.PersistentVector
  (->claim [[claim-body & [sources evidence] :as claim]]
    (->claim (merge {:body claim-body :evidence evidence :sources sources}
                    (meta claim))))
  (->evidence [[claim]]
    (->evidence {:claim claim}))
  (->evidence-list [evidence-list]
    (map ->evidence evidence-list))
  clojure.lang.LazySeq
  (->evidence-list [evidence-list]
    (map ->evidence-list evidence-list))
  java.lang.String
  (->claim [tmpid]
    tmpid)
  nil
  (->evidence-list [n]
    []))

(defn print-claim [claim]
  [(:claim/body claim)
   (for [evidence (:claim/evidence claim)]
     (print-claim (:evidence/claim evidence)))])

(defn ->claim-print [shorthand-claim]
  (print-claim (->claim shorthand-claim)))

(def books
  (map
   new-book
   [{:db/id "new-jim-crow"
     :title "The New Jim Crow: Mass Incarceration in the Age of Colorblindness"
     :author "Michelle Alexander"
     :url "http://newjimcrow.com/"}]))

(def claims
  (map ->claim
       [{:db/id "animals-are-awesome"
         :body "Animals are awesome."
         :standalone true
         :created-at #inst "2019-05-01T12:01:00Z"
         :votes [{:voter toby :agreement 100}]}

        {:body "Dogs are great."
         :standalone true
         :created-at #inst "2019-05-01T12:00:00Z"
         :votes [{:voter travis :agreement 100}
                 {:voter toby :agreement 100}]
         :evidence [{:creator travis
                     :claim "animals-are-awesome"
                     :supports true
                     :votes [{:voter travis :rating 100}]}]}
        {:body "Cats are great."
         :standalone true
         :featured true
         :created-at #inst "2019-05-01T12:00:00Z"
         :creator toby
         :votes [{:voter travis :agreement 100}
                 {:voter toby :agreement 100}]
         :evidence [{:creator toby
                     :claim "animals-are-awesome"
                     :supports true
                     :votes [{:voter toby :rating 100}]}
                    {:creator toby
                     :supports true
                     :claim {:body "It feels nice to pet a cat."
                             :created-at #inst "2019-05-01T12:02:00Z"
                             :creator toby}}
                    {:creator toby
                     :supports true
                     :claim {:body "Cats are very cute."
                             :created-at #inst "2019-05-01T12:03:00Z"
                             :creator toby}}
                    {:creator toby
                     :supports true
                     :claim {:body "Cats are very interesting."
                             :created-at #inst "2019-05-01T12:04:00Z"
                             :creator toby}}
                    {:creator toby
                     :supports false
                     :claim {:body "Cats can be jerks."
                             :created-at #inst "2019-05-01T12:05:00Z"
                             :creator toby}}
                    {:creator toby
                     :supports false
                     :claim {:body "Cats sometimes destroy furniture."
                             :created-at #inst "2019-05-01T12:06:00Z"
                             :creator toby}}
                    ]}
        {:body "The only thing I know for certain is that I exist."
         :standalone true
         :featured true
         :created-at #inst "2019-05-04T12:00:00Z"
         :creator toby
         :sources [{:title "Cogito, ergo sum"
                    :url "https://en.wikipedia.org/wiki/Cogito,_ergo_sum"}]
         :evidence [{:creator toby
                     :claim {:body "The real world seems very real."
                             :created-at #inst "2019-05-10T12:01:00Z"
                             :creator toby}
                     :supports false}
                    {:creator toby
                     :claim {:body "It is possible that everything I experience is a simulation."
                             :created-at #inst "2019-05-04T12:01:00Z"
                             :creator toby
                             :sources [{:title "The Matrix"
                                        :url "https://www.imdb.com/title/tt0133093/"}]}
                     :supports true}
                    {:creator toby
                     :claim {:body "Even if everything I experience is a simulation, I am still experiencing it, therefore I must exist."
                             :created-at #inst "2019-05-04T12:02:00Z"
                             :creator toby
                             :sources [{:title "Cogito, ergo sum"
                                         :url "https://en.wikipedia.org/wiki/Cogito,_ergo_sum"}]}
                     :supports true
                     }]}
        {:body "God exists."
         :standalone true
         :featured true
         :created-at #inst "2019-05-09T17:25:00Z"
         :creator toby
         :sources [{:title "Existence of God"
                    :url "https://en.wikipedia.org/wiki/Existence_of_God"}]
         :evidence {:support
                    [[{:body "It is impossible for an entity to cause itself to be created, and it is impossible for there to be an infinite chain of causes. Therefore, there must be a first cause, itself uncaused."
                       :creator toby
                       :sources [{:title "First cause"
                                  :url "https://en.wikipedia.org/wiki/Unmoved_mover#First_cause"}]
                       :created-at #inst "2019-05-09T17:30:00Z"}]]
                    :oppose
                    [[{:body "It is possible to explain the creation of the universe purely within the realm of science, so the idea of a divine being is unnecessary. "
                       :creator toby
                       :sources [{:title "The Grand Design"
                                  :url "https://en.wikipedia.org/wiki/The_Grand_Design_(book)"}]
                       :created-at #inst "2019-05-09T17:31:00Z"}]]}}

        {:body "Illegal immigration does not increase crime."
         :standalone true
         :featured true
         :agree true
         :created-at #inst "2019-05-14T13:09:00Z"
         :creator toby
         :sources [{:title "Is There a Connection Between Undocumented Immigrants and Crime?"
                    :url "https://www.nytimes.com/2019/05/13/upshot/illegal-immigration-crime-rates-research.html"}]
         :evidence [{:creator toby
                     :supports true
                     :claim {:body "There is no sign of a strong link between undocumented immigrants and crime."
                             :creator toby
                             :agree true
                             :created-at #inst "2019-05-14T13:10:00Z"
                             :sources  [{:title "Is There a Connection Between Undocumented Immigrants and Crime?"
                                         :url "https://www.nytimes.com/2019/05/13/upshot/illegal-immigration-crime-rates-research.html"}]
                             }}
                    {:creator toby
                     :supports true
                     :agree true
                     :claim {:body "A 2018 study published in the journal \"Criminology\" found that \"undocumented immigration does not increase violence. Rather, the relationship between undocumented immigration and violent crime is generally negative.\""
                             :creator toby
                             :created-at #inst "2019-05-14T13:11:00Z"
                             :sources  [{:title "Does Undocumented Immigration Increase Violent Crime?"
                                         :url "https://onlinelibrary.wiley.com/doi/full/10.1111/1745-9125.12175?fbclid=IwAR05w2ks-G8TSlg6F1tKJghcogd-jkweNHgSFtDLE_oZMzceveUyr2NBCCo"}]}}
                    {:creator toby
                     :supports true
                     :claim {:body "Undocumented immigrants in Texas are convicted of homicide, larceny, and sex crimes at lower rates than native-born Americans."
                             :creator toby
                             :created-at #inst "2019-05-14T13:12:00Z"
                             :sources  [{:title "Illegal Immigrants and Crime – Assessing the Evidence"
                                         :url "https://www.cato.org/blog/illegal-immigrants-crime-assessing-evidence"}]}}
                    {:creator toby
                     :agree true
                     :supports true
                     :claim {:body "Undocumented immigrants have far more to lose from being arrested than citizens or legal immigrants, and are therefore less likely to break the law once they are in the country."
                             :creator toby
                             :created-at #inst "2019-05-14T13:12:00Z"}}
                    {:creator toby
                     :agree false
                     :supports false
                     :claim {:body "The US federal government's State Criminal Alien Assistence Program data suggest undocumented immigrants commit crime at a higher rate than citizens and documented immigrants."
                             :creator toby
                             :created-at #inst "2019-05-14T13:13:00Z"
                             :sources [{:title "SCAAP Data Suggest Illegal Aliens Commit Crime at a Much Higher Rate Than Citizens and Lawful Immigrants"
                                        :url "https://www.fairus.org/issue/illegal-immigration/scaap-data-suggest-illegal-aliens-commit-crime-much-higher-rate-citizens"}]
                             :evidence [{:creator toby
                                         :supports false
                                         :agree true
                                         :claim {:body "The Federation for American Immigration Reform report on undocumented immigrant incarceration rates is poorly contrived and terribly executed."
                                                 :creator toby
                                                 :created-at #inst "2019-05-14T13:14:00Z"
                                                 :sources [{:title "FAIR SCAAP Crime Report Has Many Serious Problems"
                                                            :url "https://www.cato.org/blog/fair-scaap-crime-report-has-many-serious-problems"}]
                                                 :evidence [{:creator toby
                                                             :supports true
                                                             :claim {:body "The Federation for American Immigration Reform report on undocumented immigrant incarceration rates does not state the year or period of years it is analyzing."
                                                                     :creator toby
                                                                     :created-at #inst "2019-05-14T13:14:00Z"
                                                                     :sources [{:title "FAIR SCAAP Crime Report Has Many Serious Problems"
                                                                                :url "https://www.cato.org/blog/fair-scaap-crime-report-has-many-serious-problems"}]}}
                                                            {:creator toby
                                                             :supports true
                                                             :claim {:body "The Federation for American Immigration Reform report on undocumented immigrant incarceration rates uses SCAAP data incorrectly when calculating incarceration rates."
                                                                     :creator toby
                                                                     :created-at #inst "2019-05-14T13:14:00Z"
                                                                     :sources [{:title "FAIR SCAAP Crime Report Has Many Serious Problems"
                                                                                :url "https://www.cato.org/blog/fair-scaap-crime-report-has-many-serious-problems"}]}}]}}]}
                     }]}
        {:body "Objective reality does not exist."
         :created-at #inst "2019-05-14T15:33:00Z"
         :sources [{:title "A quantum experiment suggests there’s no such thing as objective reality"
                    :url "https://www.technologyreview.com/s/613092/a-quantum-experiment-suggests-theres-no-such-thing-as-objective-reality/"}]
         :votes [{:agreement 100}]
         :evidence [{:supports true
                 :claim {:body "Two observers of a quantum physics experiment experienced different, conflicting realities."
                         :created-at #inst "2019-05-14T15:36:00Z"
                         :sources [{:title "Experimental rejection of observer-independence in the quantum world"
                                    :url "https://arxiv.org/abs/1902.05080"}]}
                     :votes {:rating 100}}]}
        ["In 2019, the \"War on Drugs\" is one pillar of a racial caste system similar to Jim Crow."
         [{:book "new-jim-crow"}]
         {:support
          [[["In 2019, the mass incarceration of African Americans has created a racial caste system similar to Jim Crow."
             [{:book "new-jim-crow"}]
             {:support
              [[["In 2019, a person who has been convicted of a felony is considered to be part of a social undercaste by the laws of the United States of America."]]
               [["In 2019, the social undercaste created by laws disempowering felons shares many characteristics with the social undercaste created by Jim Crow laws."
                 [{:book "new-jim-crow"}]]]
               [["Jim Crow era segregation laws and modern mass incarceration have similar political origins."
                 [{:book "new-jim-crow" :page 190}]
                 {:support
                  [[["Jim Crow era segregation laws were proposed as part of a deliberate effort to deflect hostility that had been brewing against the white elite away from them and toward African Americans"
                     [{:book "new-jim-crow" :page 190}]]]
                   [["American conservatives in the 1970s and 1980s deliberately used racially coded rhetoric on crime and welfare to appeal to racial biases and the economic vulnerabilities of poor and working-class white people."
                     [{:book "new-jim-crow" :page 190}]]]]}]]]}]]
           [["In 2019, the \"War on Drugs\" drives the mass incarceration of African Americans."
             [{:book "new-jim-crow"}]]]]}]
        ^{:featured true}
        ["Earth's climate is changing rapidly as a result of human activity."
         [{:url "https://climate.nasa.gov/evidence/" :title "NASA Climate Change Facts"}]
         {:support
          [[["More than 97 percent of actively publishing climate scientists agree that climate warming trends over the last century are a result of human activity."
             [{:url "https://climate.nasa.gov/scientific-consensus/" :title "NASA Climate Change Scientific Consensus"}]]]
           [["An increase in carbon dioxide in the earth's atmosphere is driving a rapid increase in global temperatures."
             [{:url "https://climate.nasa.gov/evidence/" :title "NASA Climate Change Facts"}]
             {:support
              [[["Carbon dioxide traps infrared radiation in the atmosphere."]]
               [["Measured increases in atmospheric concentrations of carbon dioxide correlate with measured increases in global temperatures."
                 [{:url "https://skepticalscience.com/The-correlation-between-CO2-and-temperature.html" :title "The correlation between CO2 and temperature"}]]]]}]]
           [["Human activity has driven a rapid and historically unprecedented rise in the amount of carbin dioxide in the atmosphere."
             [{:url "https://skepticalscience.com/co2-increase-is-natural-not-human-caused.htm" :title "What is causing the increase in atmospheric CO2?"}]]]
           [["Global annually averaged surface air temperature increased by about 1.8°F (1.0°C) from 1901-2016"
             [{:url "https://science2017.globalchange.gov/chapter/executive-summary/"
               :title "U.S. Global Change Research Program Climate Science Special Report"}
              {:url "https://www.ncdc.noaa.gov/cag/global/time-series/globe/land_ocean/12/5/1901-2019"
               :title "Global Land and Temperature Anomalies"}]]]
           [["Glacial melting in the Himalayas is accelerating."
             [{:url "https://www.theguardian.com/environment/2019/jun/19/himalayan-glacier-melting-doubled-since-2000-scientists-reveal"
               :title "Himalayan glacier melting doubled since 2000, spy satellites show"}]
             {:support
              [[["Between 1975 and 2000, Himalayan glacier surfaces sank by an average of 22cm per year."]]
               [["Between 2000 and 2016, Himalayan glacier surfaces sank by an average of 43cm per year."]]]}]]
           [["The number of wildfires is increasing and wildfire season is getting longer in the Western United States."
             [{:url "https://www.ucsusa.org/global-warming/science-and-impacts/impacts/infographic-wildfires-climate-change.html"
               :title "Union of Concerned Scientists Western Wildfires and Climate Change"}]]]
           ]
          :oppose
          [[^{:agree false}
            ["Over the past few hundred years, there has been a steady increase in the numbers of sunspots, at the time when the Earth has been getting warmer. The data suggests solar activity is influencing the global climate causing the world to get warmer."
             [{:url "http://news.bbc.co.uk/2/hi/science/nature/3869753.stm"
               :title "Sunspots reaching 1,000-year high"}]
             {:oppose
              [[["The sun's energy output has been decreasing since the 1980s, but global temperatures have continued to increase."
                 [{:url "https://skepticalscience.com/solar-activity-sunspots-global-warming-basic.htm"
                   :title "Sun & climate: moving in opposite directions"}]]]]}]]]}]
        ["The death penalty should be abolished."
         [{:url "https://www.amnesty.org.au/5-reasons-abolish-death-penalty/" :title "Five Reasons to Abolish the Death Penalty"}
          {:url "https://www.nytimes.com/2019/06/14/opinion/sunday/death-penalty.html" :title "When We Kill"}]
         {:support
          [[["The death penalty is irreversible."]]
           [["The death penalty is frequently overturned in the United States."
             [{:url "https://www.innocenceproject.org/all-cases/" :title "Innocence Project - All Cases"}
              {:url "https://en.wikipedia.org/wiki/List_of_exonerated_death_row_inmates#United_States" :title "Wikipedia - List of exonerated death row inmates in the United States"}
              ]]]
           [["There is no credible scientific evidence that the death penalty deters crime."
             [{:url "https://www.abc.net.au/news/2015-02-26/fact-check3a-does-the-death-penalty-deter3f/6116030" :title "Fact check: No proof the death penalty prevents crime"}]]]
           [["A 2009 study found that 88% of surveyed members of the American Criminology Society did not believe the death penalty deterred murderers more than long term imprisonment."]]]
          :oppose
          [[["Long term imprisonment of people who commit heinous crimes is very costly."]]
           [["People who commit murder deserve to be killed."
             []
             {:oppose
              [[["Most killers are untreated traumatized children who are controlling the actions of the scary adults they have become."
                 [{:url "https://www.apa.org/monitor/2016/02/killers" :title "Listening to killers"}]]]
               [["The goal of the United States of America's criminal justice system should be to rehabilitate offenders."]]]}]]]}]
        ["Prisons should not be operated by private companies."]
        ^{:agree true}
        ["Prisons should be abolished."
         []
         {:support
          [[^{:agree false}
            ["We could safely eliminate all prisons tomorrow."]]
           [^{:agree true}
            ["Prisons isolate people from communities that support them."]]
           [^{:agree true}
            ["Prisons exacerbate many of the underlying problems that lead people to cause harm."
             []
             {:support
              [[["People who are imprisoned for committing crimes motivated by need, such as minor theft or prostitution, find it much harder to obtain legal employment once convicted of a crime and are therefore even more likely to cause harm again."
                 [{:url "https://en.wikipedia.org/wiki/Prison_abolition_movement#Arguments_made_for_prison_abolition"
                   :title "Wikipedia: Arguments made for prison abolition"}]]]
               [["Prisoners are exposed to more violence than non-prisoners."]]
               [["Prisoners frequently accrue debts in prison that leave them more impoverished once they leave than when they went to prison."]]
               [["Most people with substance abuse issues who are released from prison relapse once they are out."
                 [{:url "https://www.prisonerhealth.org/educational-resources/factsheets-2/incarceration-substance-abuse-and-addiction/"
                   :title "Incarceration, Substance Abuse, and Addiction" :author "The Center for Prisoner Health and Human Rights"}]]]]}]]
           [^{:agree true}
            ["Putting people in prisons does not solve any of the underlying problems that lead people to cause harm."
             [{:url "http://criticalresistance.org/wp-content/uploads/2012/06/What-is-Abolition.pdf"
               :title "What is Abolition?" :author "Critical Resistance"}]
             {:support
              [[["Putting people in prisons does not improve their mental health."]]
               [["Putting people in prisons does not lift them out of poverty."]]
               [["Putting people in prisons does not cure addiction."]]
               [["Putting people in prisons does not reduce their exposure to violence."]]]}]]]
          :oppose
          [[^{:agree true}
            ["Prisons are necessary in 2019 to deal with very dangerous individuals."]]
           [^{:agree false}
            ["Prisons keep criminals from causing harm to people outside prisons."
             []
             {:oppose
              [[["Prisons traumatize people, making it even more likely they will cause harm to people outside prisons once they leave."]]
               [["Prisons acclimate people to violence, making it even more likely they will act violently to people outside prisons once they leave."]]]}]]]}]
        ["Abortion should be legal."
         []
         {:support
          [[["A woman's right to bodily autonomy outweighs any moral harm caused by terminating a fetus."]]]
          :oppose
          [[["Abortion ends a life."]]]}]
        ["Police officers should not be first responders to mental health crises."
         []
         {:support
          [[["Many police officers don't have the appropriate training to be first responders to mental health crises."]]
           [["Policing culture in the United States of America does not provide a good framework for responding to mental health crises."]]
           [["The threat of state sanctioned violence escalates mental health crises."]]
           [["All police action carries an implicit threat of state sanctioned violence."]]
           [["The CAHOOTS program in Eugene, OR is a better model for responding to mental health crises than police response."
             [{:url "https://whitebirdclinic.org/cahoots/" :title "CAHOOTS Information Page"}]]]]
          :oppose
          [[["Police officers are the only resource many communities have to respond to mental health crises."]]
           [["Many police officers genuinely want to help people who are experiencing a mental health crisis."]]]}]
        ["Having aspirations helps us navigate life in a meaningful and fulfilling way."
         []
         {:oppose
          [[["Failing to make progress toward our aspirations causes suffering."
             []
             {:oppose
              [[["Failing to make progress toward our aspirations doesn't cause suffering, a negative fixation on that lack of progress causes suffering."
                 {:url "https://www.ecu.edu.au/news/latest-news/2019/06/not-always-reaching-your-potential-is-okay-but-overthinking-it-is-a-problem"
                  :title "Not always reaching your potential is okay, but overthinking it is a problem"}]]]}]]
           ]}]
        ["In 2019, in the United States we should make laws that give advantages to some people on the basis of race."
         []
         {:support
          [[["There are profound racial inequalities in housing, employment and health in the United States in 2019."
             [{:url "https://news.stanford.edu/2017/06/16/report-finds-significant-racial-ethnic-disparities/"
               :title "Significant racial and ethnic disparities still exist, according to Stanford report"}]]
            ["We should aspire to live in a society that does not disadvantage people on the basis of race."]
            ["The cultural and financial structures of the United States in 2019 give advantages to people who present as white."]
            ["We should use law to effect change in our cultural and financial institutions."]
            ["There is no biological basis for the idea of race."
             [{:url "https://rationalwiki.org/wiki/Racialism#Scientific_consensus:_Races_aren.27t_useful"
               :title "RationalWiki: Racialism: Scientific consensus: Races aren't useful"}]
             {:support
              [[["DNA studies do not indicate that separate classifiable subspecies (races) exist within modern humans."
                 [{:url "https://archive.fo/oQ5ut"
                   :title "Human Genome Project Information Archive: Minorities, Race and Genomics"}]]]

               [["A 2012 survey of 3,286 American Anthropological Association members found very strong agreement that the conventional concept of race is not scientifically useful."
                 [{:url "https://onlinelibrary.wiley.com/doi/full/10.1002/ajpa.23120"
                   :title "Anthropologists' views on race, ancestry, and genetics"}]]]]
              }]
            ["There is a large racial gap in the transmission of wealth across generations."
             [{:url "https://news.umich.edu/three-generations-of-data-show-how-wealthy-white-families-stay-wealthy/"
               :title "Three generations of data show how wealthy (white) families stay wealthy"}]]

            ]]}]
        ["English speakers should use 'they' and 'them' pronouns when referring to a person whose gender identity they do not know."
         []
         {:support [[["It is grammatically correct to refer to a single person as \"they\" in english if I do not know their gender."
                      []
                      {:support [[["If someone knocked on my door and my housemate said \"there's someone here for you\" I would reply \"what do they want\" if I did not know their gender."]]]}]]]}]
        ["People should be able to choose the pronouns people use to refer to them in English."
         []
         {:support
          [["Many people with male genitalia do not identify with masculine gender norms."]
           ["Many people with female genitalia do not identify with feminine gender norms."]
           [["The experience of being misgendered can be hurtful."
             [{:url "https://www.hrc.org/resources/talking-about-pronouns-in-the-workplace"
               :title "Talking About Pronouns in the Workplace"}]]]
           [["Many people do not identify with the gender they were assigned at birth and experience distress when people use the pronouns associated with that gender to refer to them."]]
           [["Gender expression through preferred pronouns is an important part of identity for many people, and people's decisions about their own identities should be respected."
             [{:url "https://www.theguardian.com/commentisfree/2018/jun/04/gender-neutral-pronouns-they-he-she-why-deny"
               :title "If someone wants to be called 'they' and not 'he' or 'she', why say no?"}]
             {:support
              [[["Gender expression through preferred pronouns is an important part of identity for many people."]
                ["People's decisions about their own identities should be respected."]]]}]]
           [["People should not have the right to deny other people's identities."
             [{:url "https://www.theguardian.com/commentisfree/2018/jun/04/gender-neutral-pronouns-they-he-she-why-deny"
               :title "If someone wants to be called 'they' and not 'he' or 'she', why say no?"}]]]]}]

        ])
  )

(def data (concat users books claims))
