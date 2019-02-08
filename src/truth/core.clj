(ns truth.core
  (:require [datomic.client.api :as d]))


(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(def cfg {:server-type :peer-server
          :access-key "myaccesskey"
          :secret "mysecret"
          :endpoint "localhost:8998"})

(def client (d/client cfg))

(def user-schema
  [{:db/ident :user/username
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The user's username"}
   {:db/ident :user/email
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The user's username"}
   ])

(def claim-schema
  [{:db/ident :claim/body
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The text of the claim"}
   {:db/ident :claim/creator
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "The user who created this claim"}
   {:db/ident :claim/contributors
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc "Users who have contributed this claim"}
   ])

(def claim-vote-schema
  [{:db/ident :claim-vote/claim
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "The claim being voted on"}
   {:db/ident :claim-vote/voter
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "The user who voted"}
   {:db/ident :claim-vote/agree
    :db/valueType :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db/doc "Whether the :claim-vote/user agrees with the claim or not"}
   ])

(def evidence-schema
  [{:db/ident :evidence/creator
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "The user who asserted this evidentiary relationship"}
   {:db/ident :evidence/target
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "The claim being supported"}
   {:db/ident :evidence/claim
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "The claim providing evidence"}
   {:db/ident :evidence/supports
    :db/valueType :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db/doc "Does this evidence support the target claim?"}
   ])

(def relevance-vote-schema
  [{:db/ident :relevance-vote/evidence
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "The evidence being voted on"}
   {:db/ident :relevance-vote/voter
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "The user who voted"}
   {:db/ident :relevance-vote/rating
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "How relevant the user thinks this evidence is"}
   ])

(def users
  [{:user/username "travis"
    :user/email "travis@truth.com"}
   {:user/username "james"
    :user/email "james@truth.com"}
   {:user/username "toby"
    :user/email "toby@truth.com"}
   ])

(def james [:user/username "james"])
(def travis [:user/username "travis"])
(def toby [:user/username "toby"])

(def pet-claims
  [{:claim/body "Dogs are great"
    :claim/creator travis
    :claim/contributors [travis]
    }
   {:claim/body "They have cute paws"
    :claim/creator travis
;;    :claim/contributors travis
}
   {:claim/body "Cats are great"
    :claim/creator james
;;    :claim/contributors travis
}
   {:claim/body "They don't like people"
    :claim/creator travis
    ;;    :claim/contributors travis
    }

   ])

(def more-pet-claims
  [{:db/id [:claim/body "Dogs are great"]
    :claim/contributors [james]
    :claim/body "Dogs are awesome"
    }
   ])

(def dogs-are-great [:claim/body "Dogs are great"])
(def cats-are-great [:claim/body "Cats are great"])
(def cute-paws [:claim/body "They have cute paws"])
(def dont-like-people [:claim/body "They don't like people"])

(def votes
  [{:claim-vote/claim dogs-are-great
    :claim-vote/voter travis
    :claim-vote/agree true}
   {:claim-vote/claim dogs-are-great
    :claim-vote/voter james
    :claim-vote/agree false}
   {:claim-vote/claim cats-are-great
    :claim-vote/voter travis
    :claim-vote/agree false}
   {:claim-vote/claim cats-are-great
    :claim-vote/voter james
    :claim-vote/agree true}
   ])

(def evidence
  [{:db/id "cute-paws-supports-dogs-are-great"
    :evidence/creator travis
    :evidence/target dogs-are-great
    :evidence/claim cute-paws
    :evidence/supports true}
   {:db/id "cute-paws-supports-cats-are-great"
    :evidence/creator james
    :evidence/target cats-are-great
    :evidence/claim cute-paws
    :evidence/supports true}
   {:evidence/creator travis
    :evidence/target cats-are-great
    :evidence/claim dont-like-people
    :evidence/supports false}
   {:relevance-vote/evidence "cute-paws-supports-dogs-are-great"
    :relevance-vote/voter james
    :relevance-vote/rating 33}
   {:relevance-vote/evidence "cute-paws-supports-dogs-are-great"
    :relevance-vote/voter travis
    :relevance-vote/rating 100}
   {:relevance-vote/evidence "cute-paws-supports-cats-are-great"
    :relevance-vote/voter james
    :relevance-vote/rating 100}
   {:relevance-vote/evidence "cute-paws-supports-cats-are-great"
    :relevance-vote/voter travis
    :relevance-vote/rating 66}
   ])

(def all-claims-q '[:find ?e ?claim-body
                    :where [?e :claim/body ?claim-body]])

(def all-evidence-claims-q
  '[:find ?body
    :where
    [_ :evidence/claim ?evidence-target]
    [?evidence-target :claim/body ?body]
    ])

(def cats-evidence '[:find ?body ?agree
                     :where
                     [?claim :claim/body "Cats are great"]
                     [?evidence :evidence/target ?claim]
                     [?evidence :evidence/claim ?evidence-claim]
                     [?evidence-claim :claim/body ?body]
                     [?vote :claim-vote/claim ?claim]
                     [?vote :claim-vote/agree ?agree]
                     ])

(def votes-q '[:find ?vote
               :where
               ;;[?vote :claim-vote/claim ?evidence-claim]
               [_ :claim-vote/agree ?vote]
               ])

(def relevance-votes-q
  '[:find ?agree ?target-body ?supports ?claim-body (avg ?rating)
    :in $ ?current-user-username
    :where
    [?vote :relevance-vote/rating ?rating]
    [?vote :relevance-vote/evidence ?evidence]
    [?evidence :evidence/supports ?supports]
    [?evidence :evidence/target ?target]
    [?target :claim/body ?target-body]
    [?target-vote :claim-vote/claim ?target]
    [?target-vote :claim-vote/agree ?agree]
    [?target-vote :claim-vote/voter ?current-user]
    [?current-user :user/username ?current-user-username]
    [?evidence :evidence/claim ?claim]
    [?claim :claim/body ?claim-body]
    ])

(def relevance-votes-q
  '[:find ?agree
    (pull ?target [:claim/body])
    (pull ?evidence [:evidence/supports])
    (pull ?claim [:claim/body])
    (avg ?rating)
    :in $ ?current-user-username
    :where
    [?vote :relevance-vote/rating ?rating]
    [?vote :relevance-vote/evidence ?evidence]
    [?evidence :evidence/target ?target]
    [?target-vote :claim-vote/claim ?target]
    [?target-vote :claim-vote/agree ?agree]
    [?target-vote :claim-vote/voter ?current-user]
    [?current-user :user/username ?current-user-username]
    [?evidence :evidence/claim ?claim]
    ])

(def claim-q
  '[:find (pull ?claim [* {:claim/contributors [:user/username]}])
    :in $ ?claim-body
    :where
    [?claim :claim/body ?claim-body]

    ])

(def conn (d/connect client {:db-name "truth"}))

(defn get-user-by-email [db email]
  (first (first
          (d/q '[:find (pull ?user [*])
                 :in $ ?email
                 :where
                 [?user :user/email ?email]]
               db email))))

(defn get-all-claims [db]
  (first
   (d/q
    '[:find (pull ?claim [* {:claim/contributors [:user/username]}])
      :where
      [?claim :claim/body _]]
    db)))

(defn get-contributors [db claim]
  (first
   (d/q '[:find (pull ?user [:user/username])
          :in $ ?claim-id
          :where
          [?claim-id :claim/contributors ?user]]
        db (:db/id claim))))

(comment
  (do
   (d/transact conn {:tx-data user-schema})
   (d/transact conn {:tx-data claim-schema})
   (d/transact conn {:tx-data claim-vote-schema})
   (d/transact conn {:tx-data evidence-schema})
   (d/transact conn {:tx-data relevance-vote-schema})

   (d/transact conn {:tx-data users})
   (d/transact conn {:tx-data pet-claims})
;;   (d/transact conn {:tx-data more-pet-claims})
   (d/transact conn {:tx-data votes})
   (d/transact conn {:tx-data evidence})
   (def db (d/db conn))

   )
  (let [[[id]]
        ]
    id
    )

  (d/q '[:find ?claim
         :in $ ?claim-body
         :where
         [?claim :claim/body ?claim-body]
         ]
       db "Dogs are great")

  (d/q '[:find ?eid
         :in $ ?eid
         :where
         [?eid]
         ]
       db 17592186045425)

  (d/transact conn {:tx-data [{:db/id 17592186045425
                               :claim/body "Dogs are fine"
                               :claim/contributors [toby]
                               }]})
  (def db (d/db conn))

  (d/q '[:find ?body (distinct ?contributor-usernames)
         :in $ ?eid
         :where
         [?eid]
         [?eid :claim/body ?body]
         [?eid :claim/contributors ?contributors]
         [?contributors :user/username ?contributor-usernames]
         ]
       db
       17592186045425)


  (d/q all-claims-q db)
  (d/q all-evidence-claims-q db)
  (d/q cats-evidence db)
  (d/q votes-q db)
  (d/q relevance-votes-q db "james")
  (d/q relevance-votes-q db "travis")
  (d/q claim-q db "Dogs are fine")
  )
