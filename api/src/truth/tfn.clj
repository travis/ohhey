(ns truth.tfn
  (:require [datomic.client.api :as d]
            [truth.schema :as schema]
            [truth.data :as data]
            [truth.domain :as t]))

(defn reject-long-bodies! [{body :body}]
  (when (< t/char-limit (count body))
    (throw (ex-info "claim body must be at most 255 characters long" {:body body :count (count body)}))))

(defn reject-invalid-chars! [{body :body}]
  (doall
   (for [c body]
     (when (not (= (java.lang.Character$UnicodeBlock/of c) java.lang.Character$UnicodeBlock/BASIC_LATIN))
       (throw (ex-info (str "claim body must not contain invalid character '"c"'") {:body body :char c}))))))

(defn create-claim! [db claim-input creator]
  (reject-long-bodies! claim-input)
  (reject-invalid-chars! claim-input)
  [(t/new-claim
    (assoc claim-input :creator creator))])

(comment

  (create-claim! [] {:body "HAMS"} nil)


  )
