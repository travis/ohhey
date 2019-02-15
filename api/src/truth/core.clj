(ns truth.core
  (:require [datomic.api :as d]
            [truth.schema :as schema]
            [truth.data :as data]
            [truth.db :as db]
            [truth.domain :as t]))

(comment
  (do
    (def uri "datomic:mem://test")
    (d/create-database uri)
    (def conn (d/connect uri))
    (schema/load conn)
    (data/load conn)
    (def db (d/db conn))

   )

  (t/get-vote-for-user-and-claim db [:user/username "travis"] [:claim/slug "dogs-are-great"])

  (d/q all-claims-q db)
  (d/q all-evidence-claims-q db)
  (d/q cats-evidence db)
  (d/q votes-q db)
  (d/q relevance-votes-q db "james")
  (d/q relevance-votes-q db "travis")
  (d/q claim-q db "Dogs are fine")
  )
