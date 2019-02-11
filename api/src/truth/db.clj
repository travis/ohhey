(ns truth.db
  (:require [datomic.api :as d]))

(def uri "datomic:mem://truth")

(defn conn []
  (d/connect uri))
