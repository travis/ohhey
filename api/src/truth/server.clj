(ns truth.server
  (:require
   [org.httpkit.server :refer [run-server]]
   [truth.ions :as ions]
   [datomic.ion.cast :as cast]
))

;; this function formatted weirdly for convenience of cider-based development
(defn start-server []
  ;; pre-load client to avoid classpath weirdness I was seeing
  (ions/client)
  (def stop-server (run-server #'ions/graphql* {:port 3002})))

(comment
  (start-server)
  (stop-server)
)


(defn -main []
  (cast/initialize-redirect :stdout)
  (start-server))
