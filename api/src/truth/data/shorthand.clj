(ns truth.data.shorthand
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::claim nil)

(s/def ::body string?)

(s/def ::url string?)

(s/def ::title string?)

(s/def ::book string?)

(s/def ::source (s/or
                 :source-db-ref string?
                 :source (s/keys :opt-un [::url ::title ::book])))

(s/def ::sources (s/coll-of ::source))

(s/def ::evidence (s/coll-of ::claim))

(s/def ::evidence-list (s/coll-of ::evidence))

(s/def ::support ::evidence-list)
(s/def ::oppose ::evidence-list)

(s/def ::evidence-lists (s/keys :opt-un [::support ::oppose]))

(s/def ::claim (s/or
                :shorthand (s/cat :body ::body :sources (s/? ::sources) :evidence (s/? ::evidence-lists))
                :longhand (s/keys :req-un [::body]
                                  :opt-un [::sources])))

(s/def ::claims (s/coll-of ::claim))
