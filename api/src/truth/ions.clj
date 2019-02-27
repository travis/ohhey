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

(defn make-client [] (d/client cloud/cfg))
(def client (memoize make-client))
(def db-spec {:db-name (str "ohhey-dev")})
(defn get-conn [] (d/connect (client) db-spec))

(defn graphql*
  "Lambda ion that executes a graphql query"
  [{:keys [request-method headers body] :as request}]
  (if (= :options request-method)
    {:status 200
     :headers {"Content-Type" "application/json"
               "Access-Control-Allow-Origin" "*"
               "access-control-allow-headers" "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token"
               "access-control-allow-methods" "POST,OPTIONS" }}
    {:status 200
     :headers {"Content-Type" "application/json"
               "Access-Control-Allow-Origin" "*"}
     :body
     (try
       (let [body-str (slurp body)
             body-json (json/read-str body-str
                                      :key-fn keyword)
             variables (:variables body-json)
             query (:query body-json)
             conn (get-conn)
             result (lacinia/execute
                     schema query variables
                     (let [db (d/db conn)
                           current-user (t/get-user-by-username db "travis")]
                       {:db db
                        :conn conn
                        :current-user current-user}))]
         (json/write-str result))
       (catch Throwable t
         (println "error processing graphql request:")
         (println t)))
     })

  )

(def graphql
  "API Gateway GraphQL web service ion"
  (apigw/ionize graphql*))

(comment
  (use 'org.httpkit.server)

  (def stop-server (run-server #'graphql* {:port 3002}))
  (stop-server)
  )
