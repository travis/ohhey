(ns truth.ions
  (:require
   [clojure.data.json :as json]
   [com.walmartlabs.lacinia :as lacinia]
   [clojure.walk :as walk]
   [clojure.tools.logging :as log]

   [datomic.ion.lambda.api-gateway :as apigw]
   [datomic.client.api :as d]

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

(def db-spec {:db-name (str "ohhey-dev")})
(def base-search-creds {
                        :profile "ohhey"
                        })
(def search-domain "ohhey-dev")

(defn make-client [] (d/client cloud/cfg))
(def client (memoize make-client))
(defn get-conn [] (d/connect (client) db-spec))


(defn make-search-creds []
  (search/make-creds base-search-creds search-domain))
(def search-creds (memoize make-search-creds))

(defn handle-graphql*
  "Lambda ion that executes a graphql query"
  [{:keys [request-method headers body session] :as request}]
  (if (= :options request-method)
    {:status 200
     :headers {"Content-Type" "application/json"
               ;;"Access-Control-Allow-Origin" "https://ohhey.fyi"
               ;;"Access-Control-Allow-Origin" "http://local.ohhey.fyi:3000"
               ;;"access-control-allow-headers" "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,Set-Cookie,*"
               ;;"access-control-allow-methods" "POST,OPTIONS"
               ;;"Access-Control-Allow-Credentials" "true"
               }}
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
                         :search-creds (search-creds)
                         }))]
          {:status 200
           :headers {"Content-Type" "application/json"
                     ;;"Access-Control-Allow-Origin" "https://ohhey.fyi"
                     ;;"Access-Control-Allow-Origin" "http://local.ohhey.fyi:3000"
                     ;;"Access-Control-Allow-Credentials" "true"
                     }
           :body (json/write-str (dissoc result :truth/session))
           :session @request-session}))
      (catch Throwable t
        (println "error processing graphql request:")
        (println t)
        (log/error t "error processing graphql request")))))

;;(def auth-backend
;;  (session-backend
;;   {
    ;;:unauthorized-handler unauthorized-handler
;;    }))

(defn wrap-fix-set-cookie [handler]
  (fn [request]
    (let [{headers :headers :as response} (handler request)]
      (assoc response
             :headers
             (assoc headers "Set-Cookie" (first (get headers "Set-Cookie")))))))

(def graphql*
  (-> handle-graphql*
;;      (wrap-authorization auth-backend)
;;      (wrap-authentication auth-backend)
      wrap-session
      wrap-fix-set-cookie))

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

  (data/load-and-index-default-dataset (get-conn) (:doc (search-creds)))
  (data/clear-and-delete-database (get-conn) (search-creds) (client) db-spec)

  (map :id (:suggestions (:suggest (search/suggest (:search (search-creds)) "cats are"))))
  (map :id (:hit (:hits (search/suggest (:search (search-creds)) "cats are"))))


  (t/search-claims-as (d/db (get-conn)) (:search (search-creds)) [:user/username "travis"] "cats are")
  (t/suggest-claims-as (d/db (get-conn)) (:search (search-creds)) [:user/username "toby"] "cats are great")

  )
