(ns truth.schema
  (:require [datomic.api :as d]
            [datomic.client.api :as cd]))

(def user
  [{:db/ident :user/username
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The user's username"}
   {:db/ident :user/password
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The user's password"}
   {:db/ident :user/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/value
    :db/doc "The user's id"}
   {:db/ident :user/email
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/value
    :db/doc "The user's email"}
   ])

(def source
  [{:db/ident :source/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The source's id"}
   {:db/ident :source/url
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The url of the source"}
   {:db/ident :source/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The title of the source"}
   {:db/ident :source/lccn
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The url of the source"}
   {:db/ident :source/page
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "The page number of the source"}])

(def claim
  [{:db/ident :claim/body
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The text of the claim"}
   {:db/ident :claim/created-at
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc "The instant this claim was created. Can be different from the instant of the transaction it was created in, but usually not by much."}
   {:db/ident :claim/slug
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/value
    :db/doc "The url slug of the claim"}
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
    :db/doc "Users who have contributed this claim"}
   {:db/ident :claim/sources
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/isComponent true
    :db/doc "Sources for this claim"}
   {:db/ident :claim/evidence
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/isComponent true
    :db/doc "Supporting and counter evidence for this claim"}
   {:db/ident :claim/votes
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/isComponent true
    :db/doc "Votes on this claim"}
   {:db/ident :claim/standalone
    :db/valueType :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db/doc "Should this claim be presented as a standlone claim?"}])

(def claim-vote
  [{:db/ident :claim-vote/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The claim vote's id"}
   {:db/ident :claim-vote/voter
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "The user who voted"}
   {:db/ident :claim-vote/agreement
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "The degree to which :claim-vote/user agrees with the claim"}
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
   {:db/ident :evidence/supports
    :db/valueType :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db/doc "Does this evidence support the target claim?"}
   {:db/ident :evidence/votes
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/isComponent true
    :db/doc "Votes about the relevance of this evidence"}
   ])

(def relevance-vote
  [{:db/ident :relevance-vote/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The relevance vote's id"}
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
  @(d/transact conn user)
  @(d/transact conn source)
  @(d/transact conn claim)
  @(d/transact conn claim-vote)
  @(d/transact conn evidence)
  @(d/transact conn relevance-vote))

(defn client-load [conn]
  (cd/transact
   conn
   {:tx-data
    (concat user source claim claim-vote evidence relevance-vote)}))
