(ns truth.rdf
  (:require
   [arachne.aristotle.registry :refer [prefix] :as reg]
   [arachne.aristotle.graph :refer [triples]]))


(prefix 'foaf "http://xmlns.com/foaf/0.1/")

(prefix 'ohhey.* "https://ohhey.fyi/")

(prefix 'travis.beliefs "https://tvachon.inrupt.net/beliefs/")

(prefix 'belief "http://ohhey.fyi/thisisa/belief#")

(triples [:travis.beliefs/cats-are-cool :ohhey.beliefs/title "Cats are cool"])

(def travis "https://tvachon.inrupt.net/")

(defn belief-iri [root slug]
  (java.net.URI. (str root "beliefs/" slug)))

(defn evidence-iri [belief-iri slug]
  (java.net.URI. (str belief-iri "/evidence/" slug)))

(comment
 (let [cats-are-cool (belief-iri travis "cats-are-cool")]
   (triples {:rdf/about cats-are-cool
             :belief/title "Cats are cool"
             :belief/evidence (for [slug ["animals-are-awesome" "cats-are-cute"]]
                                (evidence-iri cats-are-cool slug))})))

(defn claim->rdf [{slug :claim/slug title :claim/title evidence :claim/evidence}]
  (let [claim-iri (belief-iri travis slug)]
    (triples {:rdf/about claim-iri
              :belief/title title
              :belief/evidence (for [{{ev-slug :claim/slug} :evidence/claim} evidence]
                                 (evidence-iri claim-iri ev-slug))})))

(claim->rdf {:claim/slug "cats-are-cool"
             :claim/title "Cats are cool"
             :claim/evidence [{:evidence/claim {:claim/slug "animals-are-awesome"}}
                              {:evidence/claim {:claim/slug "cats-are-cute"}}]})
