(ns truth.domain.rules)

(def rules
  '[[(zero-agreement ?agreement ?agreement-count)
     [(ground 0) ?agreement]
     [(ground 0) ?agreement-count]]
    [(zero-support-oppose ?support ?oppose)
     [(ground 0) ?support]
     [(ground 0) ?oppose]]
    [(zero-rating ?rating ?rating-count)
     [(ground 0) ?rating]
     [(ground 0) ?rating-count]]
    [(nil-my-rating ?my-rating)
     [(ground -1) ?my-rating]]
    [(zero-score ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
     [(ground 0) ?agree-disagree-score]
     [(ground 0) ?support-oppose-score]
     [(ground 0) ?support-oppose-score-component-count]]
    [(nil-my-agreement ?my-agreement)
     [(ground -101) ?my-agreement]]

    ;; relevance-rating
    [(my-rating [?relevance-vote ?user] ?my-rating)
     (or-join [?relevance-vote ?user ?my-rating]
              (and
               [?relevance-vote :relevance-vote/voter ?user]
               [?relevance-vote :relevance-vote/rating ?my-rating])
              (and
               (not [?relevance-vote :relevance-vote/voter ?user])
               (nil-my-rating ?my-rating)))]
    [(evidence-rating [?evidence] ?uniqueness ?rating ?rating-count)
     (or-join [?evidence ?uniqueness ?rating ?rating-count]
              (and
               [?evidence :evidence/votes ?relevance-vote]
               [(identity ?relevance-vote) ?uniqueness]
               [?relevance-vote :relevance-vote/rating ?rating]
               [(ground 1) ?rating-count])
              (and
               [(identity ?evidence) ?uniqueness]
               (zero-rating ?rating ?rating-count)))]
    [(my-evidence-rating [?evidence ?user] ?uniqueness ?my-rating)
     (or-join [?evidence ?user ?uniqueness ?my-rating]
              (and
               [?evidence :evidence/votes ?relevance-vote]
               [(identity ?relevance-vote) ?uniqueness]
               (my-rating ?relevance-vote ?user ?my-rating))
              (and
               [(identity ?evidence) ?uniqueness]
               (nil-my-rating ?my-rating)))]

    ;; scoring
    [(agree-disagree-score [?claim] ?uniqueness ?score)
     (agree-disagree ?claim ?uniqueness ?score _)]
    [(support-coeff [?evidence] ?support-coeff)
     (or-join [?evidence ?support-coeff]
              (and
               [?evidence :evidence/supports true]
               [(ground 1) ?support-coeff])
              (and
               [?evidence :evidence/supports false]
               [(ground -1) ?support-coeff]))]
    [(evidence-score [?evidence] ?uniqueness ?score ?score-component-count)
     [?evidence :evidence/claim ?claim]
     (agree-disagree ?claim ?agreement-uniqueness ?agreement ?agreement-count)
     (evidence-rating ?evidence ?rating-uniqueness ?rating ?rating-count)
     (support-coeff ?evidence ?support-coeff)

     [(list ?rating-uniqueness ?agreement-uniqueness)  ?uniqueness]
     [(* ?agreement ?rating ?support-coeff) ?score]
     [(* ?agreement-count ?rating-count) ?score-component-count]]
    [(support-oppose-score [?claim ?depth] ?uniqueness ?score ?score-component-count)
     (or-join [?claim ?uniqueness ?score ?score-component-count]
              (and
               [?claim :claim/evidence ?evidence]
               (evidence-score ?evidence ?uniqueness ?score ?score-component-count))
              (and
               [(identity ?claim) ?uniqueness]
               [(ground 0) ?score]
               [(ground 0) ?score-component-count]))]
    [(claim-score [?claim ?depth] ?uniqueness ?agree-disagree-score ?score ?support-oppose-component-count)
     [(/ 1 ?depth) ?depth-multiplier]
     [(* ?support-oppose-score ?depth-multiplier) ?score]
     (or
      (and
       [?depth]
       (agree-disagree-score ?claim ?uniqueness ?agree-disagree-score)
       [(ground 0) ?support-oppose-score]
       [(ground 0) ?support-oppose-component-count])
      (and
       (support-oppose-score ?claim ?depth ?uniqueness ?support-oppose-score ?support-oppose-component-count)
       [(ground 0) ?agree-disagree-score]))]

    [(agree-disagree ?claim ?uniqueness ?agreement ?agreement-count)
     (or-join [?claim ?uniqueness ?agreement ?agreement-count]
              (and
               [?claim :claim/votes ?vote]
               [(identity ?vote) ?uniqueness]
               [?vote :claim-vote/agreement ?agreement]
               [(ground 1) ?agreement-count])
              (and
               [(identity ?claim) ?uniqueness]
               (zero-agreement ?agreement ?agreement-count)))]
    [(my-agreement ?claim ?user ?uniqueness ?my-agreement)
     (or-join [?claim ?user ?uniqueness ?my-agreement]
              (and
               [?claim :claim/votes ?vote]
               [(identity ?vote) ?uniqueness]
               [?vote :claim-vote/voter ?user]
               [?vote :claim-vote/agreement ?my-agreement])
              (and
               [(identity ?claim) ?uniqueness]
               (nil-my-agreement ?my-agreement)))]
    [(support-oppose ?claim ?uniqueness ?support ?oppose)
     (or-join [?claim ?uniqueness ?support ?oppose]
              (and
               [?claim :claim/evidence ?evidence]
               [(identity ?evidence) ?uniqueness]
               (or-join
                [?evidence ?support ?oppose]
                (and
                 [?evidence :evidence/supports true]
                 [(ground 1) ?support]
                 [(ground 0) ?oppose])
                (and
                 [?evidence :evidence/supports false]
                 [(ground 0) ?support]
                 [(ground 1) ?oppose])))
              (and
               [(identity ?claim) ?uniqueness]
               (zero-support-oppose ?support ?oppose)))]
    [(zero-stats ?agreement ?agreement-count ?support ?oppose ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
     (zero-score ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
     (zero-agreement ?agreement ?agreement-count)
     (zero-support-oppose ?support ?oppose)]
    [(basic-claim-stats ?claim ?uniqueness ?agreement ?agreement-count ?support ?oppose)
     (or-join [?claim ?uniqueness ?agreement ?agreement-count ?support ?oppose]
              (and
               (agree-disagree ?claim ?uniqueness ?agreement ?agreement-count)
               (zero-support-oppose ?support ?oppose))
              (and
               (support-oppose ?claim ?uniqueness ?support ?oppose)
               (zero-agreement ?agreement ?agreement-count)))]
    [(claim-stats [?claim] ?uniqueness ?agreement ?agreement-count ?support ?oppose ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
     (or-join [?claim ?uniqueness ?agreement ?agreement-count ?support ?oppose ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count]
              (and
               (claim-score ?claim 1 ?uniqueness ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
               (zero-agreement ?agreement ?agreement-count)
               (zero-support-oppose ?support ?oppose))
              (and
               (basic-claim-stats ?claim ?uniqueness ?agreement ?agreement-count ?support ?oppose)
               (zero-score ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)))]
    [(claim-stats-as ?claim ?user ?uniqueness ?agreement ?agreement-count
                     ?support ?oppose ?my-agreement ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
     (or
      (and
       [?user]
       (claim-stats ?claim ?uniqueness ?agreement ?agreement-count ?support ?oppose ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
       (nil-my-agreement ?my-agreement))
      (and
       (my-agreement ?claim ?user ?uniqueness ?my-agreement)
       (zero-stats ?agreement ?agreement-count ?support ?oppose ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
       ))]
    [(evidence-rating-as [?evidence ?user] ?uniqueness ?rating ?rating-count ?my-rating)
     (evidence-rating ?evidence ?uniqueness ?rating ?rating-count)
     (my-evidence-rating ?evidence ?user ?uniqueness ?my-rating)]
    [(evidence-stats-as [?evidence ?user] ?uniqueness ?agreement ?agreement-count
                        ?support ?oppose ?my-agreement
                        ?rating ?rating-count ?my-rating
                        ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
     (or-join [?evidence ?user ?uniqueness ?agreement ?agreement-count
               ?support ?oppose ?my-agreement
               ?rating ?rating-count ?my-rating
               ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count]
              (and
               [?evidence :evidence/claim ?claim]
               (claim-stats-as ?claim ?user ?uniqueness
                               ?agreement ?agreement-count ?support ?oppose
                               ?my-agreement ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
               (zero-rating ?rating ?rating-count)
               (nil-my-rating ?my-rating))
              (and
               (evidence-rating-as ?evidence ?user ?uniqueness ?rating ?rating-count ?my-rating)
               (zero-stats ?agreement ?agreement-count ?support ?oppose ?agree-disagree-score ?support-oppose-score ?support-oppose-score-component-count)
               (nil-my-agreement ?my-agreement)))]
    [(agreement-for [?claim ?user] ?uniqueness ?agreement)
     (or-join [?claim ?user ?uniqueness ?agreement]
              (and
               [?claim :claim/votes ?vote]
               [?vote :claim-vote/voter ?user]
               [?vote :claim-vote/agreement ?agreement]
               [(identity ?vote) ?uniqueness])
              (and
               (zero-agreement ?agreement ?agreement-count)
               [(identity ?claim) ?uniqueness]))]
    [(evidence-for [?claim ?user] ?evidence ?uniqueness ?rating ?agreement)
     [?claim :claim/evidence ?evidence]
     [(identity ?evidence) ?uniqueness]
     (or-join [?evidence ?user ?rating ?agreement]
              (and
               [?evidence :evidence/creator ?user]
               (nil-my-rating ?rating)
               (nil-my-agreement ?agreement))
              (and
               [?evidence :evidence/votes ?vote]
               [?vote :relevance-vote/voter ?user]
               [?vote :relevance-vote/rating ?rating]
               (nil-my-agreement ?agreement))
              (and
               [?evidence :evidence/claim ?evidence-claim]
               [?evidence-claim :claim/votes ?vote]
               [?vote :claim-vote/voter ?user]
               [?vote :claim-vote/agreement ?agreement]
               (nil-my-rating ?rating)))]
    [(claim-for [?claim ?user] ?uniqueness ?agreement)
     (agreement-for ?claim ?user ?uniqueness ?agreement)]])
