(ns truth.schema
  (:require [datomic.client.api :as d]))

(def user
  [{:db/ident :user/username
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The user's username"}
   {:db/ident :user/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The user's id"}
   {:db/ident :user/email
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The user's email"}
   ])

(def claim
  [{:db/ident :claim/body
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The text of the claim"}
   {:db/ident :claim/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The claim's id"}
   {:db/ident :claim/creator
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "The user who created this claim"}
   {:db/ident :claim/contributors
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc "Users who have contributed this claim"}])

(def claim-vote
  [{:db/ident :claim-vote/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The claim vote's id"}
   {:db/ident :claim-vote/claim
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

(def evidence
  [{:db/ident :evidence/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The evidence's id"}
   {:db/ident :evidence/creator
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "The user who asserted this evidentiary relationship"}
   {:db/ident :evidence/claim
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "The claim providing evidence"}
   {:db/ident :evidence/target
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "The claim being supported or refuted"}
   {:db/ident :evidence/supports
    :db/valueType :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db/doc "Does this evidence support the target claim?"}
   ])

(def relevance-vote
  [{:db/ident :relevance-vote/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The relevance vote's id"}
   {:db/ident :relevance-vote/evidence
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

(defn load [conn]
  (d/transact conn {:tx-data user})
  (d/transact conn {:tx-data claim})
  (d/transact conn {:tx-data claim-vote})
  (d/transact conn {:tx-data evidence})
  (d/transact conn {:tx-data relevance-vote}))
