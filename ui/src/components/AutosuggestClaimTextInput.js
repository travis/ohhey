import React, {useState} from 'react';
import { Query } from "@apollo/react-components";
import { compose } from '../util'
import { withStyles } from '@material-ui/core/styles';
import {AutosuggestTextInput} from './form'


import {
  Typography
} from './ui'
import { ClaimBodyLink } from './Claims'
import {StopPropagation} from './util'

import * as queries from '../queries';

const Claim = ({claim, classes, actions}) => (
  <StopPropagation>
    <Typography variant="h6" fontFamily="claimBody">
      <ClaimBodyLink claim={claim}/>
    </Typography>
    {actions && actions(claim)}
  </StopPropagation>
)

const doSuggestionsInclude = (suggestions, query) =>
      suggestions && suggestions.map(suggestion => suggestion.result.body).find(body => body === query)

const autosuggestStyles = theme => ({
  suggestionsList: {
    listStyleType: "none",
    paddingLeft: 0
  },
  suggestionHighlighted: {
  }
})

export default compose(
  withStyles(autosuggestStyles),
)(({create, claimActions, classes, setTimeout, ...props}) => {
  const [term, setTerm] = useState(props.value || "")
  const [suggestionFetch, setSuggestionFetch] = useState(null)
  const queueSuggestionsFetch = (value) => {
    window.clearTimeout(suggestionFetch)
    setSuggestionFetch(window.setTimeout(() => setTerm(value), 200))
  }
  return (
    <Query
      query={queries.QuickSearchClaims}
      skip={!term}
      variables={{term}}>
      {({loading, error, data}) => {
        const suggestions = (data && data.suggestClaims && data.suggestClaims.results) || []
        const shouldShowCreate = (query) =>
              create && query && !doSuggestionsInclude(suggestions, query)
        return  (
          <AutosuggestTextInput
            theme={classes}
            suggestions={suggestions}
            onSuggestionsFetchRequested={({value}) => queueSuggestionsFetch(value)}
            getSuggestionValue={({result: {body}}) => body}
            renderSuggestion={({result: claim}, {query, isHighlighted}) => (
              <Claim claim={claim} actions={claimActions} isHighlighted={isHighlighted}/>
            )}
            renderSuggestionsContainer={({containerProps, children, query}) => (
              <div {...containerProps}>
                {shouldShowCreate(query) ? create(query) : ""}
                {children}
              </div>
            )}
            alwaysRenderSuggestions={true}
            {...props} />
        )
      }}
    </Query>
  )
})
