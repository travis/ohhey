(ns truth.cloud
  (:require [datomic.client.api :as d]
            [truth.schema :as schema]
            [truth.data :as data]))

(def cfg {:server-type :ion
          :region "us-east-1"
          :system "prod"
          :creds-profile "ohhey"
          :endpoint "http://entry.prod.us-east-1.datomic.net:8182/"
          :proxy-port 8182})
