(ns truth.ions
  (:require
   [clojure.data.json :as json]
   [com.walmartlabs.lacinia :as lacinia]
   [clojure.walk :as walk]
   [clojure.tools.logging :as log]

   [datomic.ion.lambda.api-gateway :as apigw]
   [datomic.client.api :as d]
   [datomic.ion :refer [get-env]]

   [truth.graphql :as graphql]
   [truth.cloud :as cloud]
   [truth.domain :as t]
   [truth.schema :as schema]
   [truth.data :as data]
   [truth.search :as search]

   [ring.middleware.session :refer [wrap-session]]
;;   [buddy.auth.backends.session :refer [session-backend]]
;;   [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
   )
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

(defn db-name []
  (let [env (name (or (get (get-env) :env) "dev"))]
    (str "ohhey-" env)))

(def db-spec {:db-name (db-name)})
(def search-domain (db-name))

(defn make-client [] (d/client cloud/cfg))
(def client (memoize make-client))
(defn get-conn [] (d/connect (client) db-spec))

(defn make-search-client [] (search/client-for-domain search-domain))
(def search-client (memoize make-search-client))

(defn handle-graphql*
  "Lambda ion that executes a graphql query"
  [{:keys [request-method headers body session] :as request}]
  (if (= :options request-method)
    {:status 200
     :headers {"Content-Type" "application/json"}}
    (try
      (with-local-vars [request-session session]
        (let [body-str (slurp body)
              body-json (json/read-str body-str
                                       :key-fn keyword)
              variables (:variables body-json)
              query (:query body-json)
              conn (get-conn)
              result (lacinia/execute
                      schema query variables
                      (let [db (d/db conn)
                            current-user (when-let [username (:identity session)]
                                           (t/get-user-by-username db username))]
                        {:db db
                         :conn conn
                         :session request-session
                         :current-user current-user
                         :search-client (search-client)
                         }))]
          {:status 200
           :headers {"Content-Type" "application/json"}
           :body (json/write-str (dissoc result :truth/session))
           :session @request-session}))
      (catch Throwable t
        (println "error processing graphql request:")
        (println t)
        (log/error t "error processing graphql request")))))

(defn wrap-fix-set-cookie [handler]
  (fn [request]
    (let [{headers :headers :as response} (handler request)]
      (assoc response
             :headers
             (assoc headers "Set-Cookie" (first (get headers "Set-Cookie")))))))

(def graphql*
  (-> handle-graphql*
      wrap-session
      wrap-fix-set-cookie))

(def graphql
  "API Gateway GraphQL web service ion"
  (apigw/ionize graphql*))

(comment
  (use 'org.httpkit.server)

  (d/transact (get-conn) {:tx-data [(last data/users)]})

  (def stop-server (run-server #'graphql* {:port 3002}))
  (stop-server)

  (clojure.core.memoize/memo-clear! client)
  (clojure.core.memoize/memo-clear! search-client)

  (d/create-database (client) db-spec)
  (schema/client-load (get-conn))
  (name :foo)
  (data/load-and-index-default-dataset (get-conn) (search-client))
  (data/clear-and-delete-database (get-conn) (search-client) (client) db-spec)

  (map :id (:suggestions (:suggest (search/suggest (search-client) "cats are"))))
  (map :id (:hit (:hits (search/suggest (search-client) "cats are"))))


  (t/search-claims-as (d/db (get-conn)) (search-client) [:user/username "travis"] "cats are")
  (t/suggest-claims-as (d/db (get-conn)) (search-client) [:user/username "toby"] "cats are great")
  (:claim/created-at (first (t/get-all-claims (d/db (get-conn)))))

  (-> (d/transact (get-conn) {:tx-data [`(truth.domain/create-claim! ~{:body "hams12" :db/id "test"} [:user/username "travis"])]})
      :tempids
      (get "test"))
  (log/error 1)
  )
