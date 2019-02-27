(ns truth.ions
  (:require
   [clojure.data.json :as json]
   [com.walmartlabs.lacinia :as lacinia]
   [clojure.walk :as walk]

   [datomic.ion.lambda.api-gateway :as apigw]
   [datomic.client.api :as d]

   [truth.graphql :as graphql]
   [truth.cloud :as cloud]
   [truth.domain :as t]
   [truth.schema :as schema]
   [truth.data :as data]
   [truth.search :as search])
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

(def db-spec {:db-name (str "ohhey-dev")})
(def base-search-creds {:profile "ohhey"})
(def search-domain "ohhey-dev")

(defn make-client [] (d/client cloud/cfg))
(def client (memoize make-client))
(defn get-conn [] (d/connect (client) db-spec))


(defn make-search-creds []
  (search/make-creds base-search-creds search-domain))
(def search-creds (memoize make-search-creds))

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
                        :current-user current-user
                        :search-creds (search-creds)}))]
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

  (clojure.core.memoize/memo-clear! client)
  (clojure.core.memoize/memo-clear! search-creds)

  (d/create-database (client) db-spec)
  (schema/client-load (get-conn))
  (data/load-and-index-default-dataset (get-conn) (:search (search-creds)))

  (data/add-all-claims-to-search-index (get-conn) (:search (search-creds)))

  (data/delete-claims-from-search-index (get-conn) (:search (search-creds)))
  (d/delete-database (client) db-spec)

  (map :id (:suggestions (:suggest (search/suggest (:search (search-creds)) "cats are"))))
  (map :id (:hit (:hits (search/suggest (:search (search-creds)) "cats are"))))


  (t/search-claims-as (d/db (get-conn)) (:search (search-creds)) [:user/username "travis"] "cats are")
  (t/suggest-claims-as (d/db (get-conn)) (:search (search-creds)) [:user/username "toby"] "cats are great")

  )
