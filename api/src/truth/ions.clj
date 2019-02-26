(ns truth.ions
  (:require
   [clojure.data.json :as json]
   [com.walmartlabs.lacinia :as lacinia]
   [clojure.walk :as walk]

   [datomic.ion.lambda.api-gateway :as apigw]
   [datomic.client.api :as d]

   [truth.graphql :as graphql]
   [truth.cloud :as cloud]
   [truth.domain :as t])
  (:import (clojure.lang IPersistentMap)))

(def schema (graphql/load-schema))

(defn simplify
  "Converts all ordered maps nested within the map into standard hash maps, and
   sequences into vectors, which makes for easier constants in the tests, and eliminates ordering problems."
  [m]
  (walk/postwalk
   (fn [node]
     (cond
       (instance? IPersistentMap node)
       (into {} node)

       (seq? node)
       (vec node)

       :else
       node))
   m))

(defn q
  [query-string]
  (-> (lacinia/execute schema query-string nil nil)
      simplify))

(def client (d/client cloud/cfg))
(def db-spec {:db-name (str "ohhey-dev")})
(def conn (d/connect client db-spec))

(defn graphql*
  "Lambda ion that executes a graphql query"
  [{:keys [headers body] :as request}]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (let [body (json/read-str body)
               variables (:variables body)
               query (:query body)
               result (lacinia/execute
                       schema query variables
                       (let [db (d/db conn)
                             current-user (t/get-user-by-username db "travis")]
                         {:db db
                          :conn conn
                          :current-user current-user}))]
           (json/write-str result))}

  )

(def graphql
  "API Gateway GraphQL web service ion"
  (apigw/ionize graphql*))
