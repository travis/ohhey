(ns truth.server
  (:require
   [datomic.api :as d]
   [com.walmartlabs.lacinia :as lacinia]
   [com.walmartlabs.lacinia.pedestal :as lp]
   [io.pedestal.http :as http]
   [clojure.java.browse :refer [browse-url]]
   [clojure.walk :as walk]
   [truth.graphql :as graphql]
   [truth.schema :as schema]
   [truth.data :as data])
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

(defonce server nil)

(defn start-server
  [_]
  (let [uri "datomic:mem://dev"
        _ (d/create-database uri)
        conn (d/connect uri)
        _ (do (schema/load conn)
              (data/load conn))
        server (-> schema
                   (lp/service-map {:graphiql true
                                    :interceptors
                                    (-> (lp/default-interceptors schema {})
                                                      (lp/inject {:name ::set-app-context
                                                                  :enter (fn [context]
                                                                           (assoc-in context [:request :lacinia-app-context]
                                                                                     {:db (d/db conn)})
                                                                           )}
                                                                 :replace :com.walmartlabs.lacinia.pedestal/inject-app-context)
                                                      )
                                    })
                   http/create-server
                   http/start)]
    (browse-url "http://localhost:8888/")
    server))

(defn stop-server
  [server]
  (http/stop server)
  nil)

(defn start
  []
  (alter-var-root #'server start-server)
  :started)

(defn stop
  []
  (alter-var-root #'server stop-server)
  :stopped)
