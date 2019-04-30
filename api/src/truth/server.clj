(ns truth.server
  (:require
   [org.httpkit.server :refer [run-server]]
   [truth.ions :as ions]))

(defn start-server []
  ;; this function formatted weirdly for convenience of cider-based development
  (def stop-server (run-server #'ions/graphql* {:port 3002}))
  )

(comment
  (stop-server)
)


(defn -main []
  (start-server))
