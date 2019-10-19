(ns truth.rdf
  (:require
   [arachne.aristotle.registry :refer [prefix] :as reg]
   [arachne.aristotle.graph :refer [triples]]
   [arachne.aristotle :as aa])
  (:import [org.apache.jena.rdf.model ModelFactory]
           [org.apache.jena.riot RDFDataMgr]))


(prefix 'foaf "http://xmlns.com/foaf/0.1/")


(prefix 'terms "http://purl.org/dc/terms/")
(prefix 'prov "http://www.w3.org/ns/prov#")


(prefix 'ohhey.* "https://ohhey.fyi/")
(prefix 'belief "https://ohhey.fyi/thisisa/belief#")
(prefix 'source "https://ohhey.fyi/thisisa/source#")

(prefix 'ibis "https://privatealpha.com/ontology/ibis/1#")

;;(triples [:travis.beliefs/cats-are-cool :ohhey.beliefs/title "Cats are cool"])

(def owner-root-iri (java.net.URI. "https://tobytoberson.inrupt.net/"))

(def owner-profile-iri (java.net.URI. (str owner-root-iri "profile/card#me")))

(defn belief-iri [root slug]
  (java.net.URI. (str root "beliefs/" slug)))

(defn evidence-iri [belief-iri slug]
  (java.net.URI. (str belief-iri "/evidence/" slug)))

(comment
  (let [cats-are-cool (belief-iri owner-root-iri "cats-are-cool")]
   (triples {:rdf/about cats-are-cool
             :belief/body "Cats are cool"
             :belief/evidence (for [slug ["animals-are-awesome" "cats-are-cute"]]
                                (evidence-iri cats-are-cool slug))})))

(defn claim->rdf [{slug :claim/slug body :claim/body evidence :claim/evidence
                   created :claim/created-at sources :claim/sources
                   quoting :claim/quoting}]
  (let [claim-iri (belief-iri owner-root-iri slug)]
    (doto (aa/graph :simple)
      (aa/add {:rdf/about claim-iri
               :belief/body body
               :terms/creator owner-profile-iri
               :terms/created created
               :terms/source (for [{url :source/url} sources]
                               (java.net.URI. url))
               :prov/wasQuotedFrom (if quoting
                                     (java.net.URI. (:source/url quoting))
                                     [])
               :ibis/supported-by (for [{{ev-slug :claim/slug} :evidence/claim} (filter :evidence/supports evidence)]
                                    (belief-iri owner-root-iri ev-slug))
               :ibis/opposed-by (for [{{ev-slug :claim/slug} :evidence/claim} (filter #(not (:evidence/supports %)) evidence)]
                                  (belief-iri owner-root-iri ev-slug))}))
    ))

(defn print-triples [triples]
  (->
   (ModelFactory/createModelForGraph
    (doto (aa/graph :simple)
      (aa/add triples)))
   (.write *out* "ttl")))

(print-triples (claim->rdf {:claim/slug "cats-are-cool"
                            :claim/created-at #inst "1985-04-12T23:20:50.520-00:00"
                            :claim/body "Cats are cool"
                            :claim/quoting {:source/url "http://allabout.com/cats"}
                            :claim/sources [{:source/url "http://coolornot.com/cats"}]
                            :claim/evidence [{:evidence/supports true
                                              :evidence/claim {:claim/slug "animals-are-awesome"}}
                                             {:evidence/supports true
                                              :evidence/claim {:claim/slug "cats-are-cute"}}
                                             {:evidence/supports false
                                              :evidence/claim {:claim/slug "cats-are-mean"}}]}))
