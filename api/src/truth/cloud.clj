(ns truth.cloud
  (:require [datomic.client.api :as d]
            [truth.schema :as schema]
            [truth.data :as data]))

(def cfg {:server-type :ion
          :region "us-east-1"
          :system "dev"
          :creds-profile "ohhey"
          :endpoint "http://entry.dev.us-east-1.datomic.net:8182/"
          :proxy-port 8182})

(comment
  (def client (d/client cfg))
  (d/create-database client {:db-name "ohhey-dev"})
  (def conn (d/connect client {:db-name "ohhey-dev"}))
  (schema/client-load conn)
  (data/client-load conn)



  )
