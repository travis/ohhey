(ns truth.queries)

(def all-claims-q '[:find ?e ?claim-body
                    :where [?e :claim/body ?claim-body]])

(def all-evidence-claims-q
  '[:find ?body
    :where
    [_ :evidence/claim ?evidence-target]
    [?evidence-target :claim/body ?body]
    ])

(def cats-evidence '[:find ?body ?agree
                     :where
                     [?claim :claim/body "Cats are great"]
                     [?evidence :evidence/target ?claim]
                     [?evidence :evidence/claim ?evidence-claim]
                     [?evidence-claim :claim/body ?body]
                     [?vote :claim-vote/claim ?claim]
                     [?vote :claim-vote/agree ?agree]
                     ])

(def votes-q '[:find ?vote
               :where
               ;;[?vote :claim-vote/claim ?evidence-claim]
               [_ :claim-vote/agree ?vote]
               ])

(def relevance-votes-q
  '[:find ?agree ?target-body ?supports ?claim-body (avg ?rating)
    :in $ ?current-user-username
    :where
    [?vote :relevance-vote/rating ?rating]
    [?vote :relevance-vote/evidence ?evidence]
    [?evidence :evidence/supports ?supports]
    [?evidence :evidence/target ?target]
    [?target :claim/body ?target-body]
    [?target-vote :claim-vote/claim ?target]
    [?target-vote :claim-vote/agree ?agree]
    [?target-vote :claim-vote/voter ?current-user]
    [?current-user :user/username ?current-user-username]
    [?evidence :evidence/claim ?claim]
    [?claim :claim/body ?claim-body]
    ])

(def relevance-votes-q
  '[:find ?agree
    (pull ?target [:claim/body])
    (pull ?evidence [:evidence/supports])
    (pull ?claim [:claim/body])
    (avg ?rating)
    :in $ ?current-user-username
    :where
    [?vote :relevance-vote/rating ?rating]
    [?vote :relevance-vote/evidence ?evidence]
    [?evidence :evidence/target ?target]
    [?target-vote :claim-vote/claim ?target]
    [?target-vote :claim-vote/agree ?agree]
    [?target-vote :claim-vote/voter ?current-user]
    [?current-user :user/username ?current-user-username]
    [?evidence :evidence/claim ?claim]
    ])

(def claim-q
  '[:find (pull ?claim [* {:claim/contributors [:user/username]}])
    :in $ ?claim-body
    :where
    [?claim :claim/body ?claim-body]

    ])
