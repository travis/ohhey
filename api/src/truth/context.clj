(ns truth.context
  (:require
   [datomic.api :as d]
   [truth.domain :as t]))

;; called once per request
(defn context [{request :request conn :conn}]
  (let [db (d/db conn)]
   {:request request
    :conn conn
    :db db
    :transact #(d/transact conn %)
    :current-user (t/get-user-by-email db "travis@truth.com")}))
