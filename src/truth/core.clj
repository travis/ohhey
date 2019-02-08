(ns truth.core
  (:require [datomic.client.api :as d]
            [truth.data :as data :refer [users pet-claims votes]]
            [truth.schema :as schema]))

(def cfg {:server-type :peer-server
          :access-key "myaccesskey"
          :secret "mysecret"
          :endpoint "localhost:8998"})

(def client (d/client cfg))
(def conn (d/connect client {:db-name "truth"}))

(comment
  (do
    (schema/load conn)
    (data/load conn)
    (def db (d/db conn))

   )


  (d/q all-claims-q db)
  (d/q all-evidence-claims-q db)
  (d/q cats-evidence db)
  (d/q votes-q db)
  (d/q relevance-votes-q db "james")
  (d/q relevance-votes-q db "travis")
  (d/q claim-q db "Dogs are fine")
  )
