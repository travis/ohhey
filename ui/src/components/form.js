import React from 'react';

import {Form, asField} from 'informed';
import Input from '@material-ui/core/Input';
import Autosuggest from 'react-autosuggest';
import * as informed from 'informed';

export const TextArea = ({inputProps, field, ...props}) =>
  (<Input multiline={true}
          inputProps={{field}}
          inputComponent={({inputRef, ...props}) => (
            <informed.TextArea forwardedRef={inputRef} {...props}/>
          )}
          {...props} />)

const IText = ({inputRef, ...props}) => (
  <informed.Text forwardedRef={inputRef} {...props}/>
)

export const TextInput = ({inputProps, field, validate, validateOnChange,
                           validateOnBlur, forwardedRef,
                           ...props}) =>
  (<Input inputProps={{field, forwardedRef, validate, validateOnChange, validateOnBlur, ...inputProps}}
          inputComponent={IText}
          {...props} />)

export const AutosuggestTextInput = asField(({
  fieldState, fieldApi,

  // props from autosuggest docs to be passed along via autosuggestProps
  // https://github.com/moroshko/react-autosuggest
  suggestions, onSuggestionsFetchRequested, onSuggestionsClearRequested, getSuggestionValue, renderSuggestion,
  onSuggestionSelected, onSuggestionHighlighted, shouldRenderSuggestions,
  alwaysRenderSuggestions, highlightFirstSuggestion, focusInputOnSuggestionClick,
  multiSection, renderSectionTitle, getSectionSuggestions,
  renderSuggestionsContainer, theme, id,

  // props from autosuggest docs that we'll handle here
  inputProps, renderInputComponent,

  // props from asField that shouldn't be passed to Input
  forwardedRef, staticContext,

  ...restProps}) => {
    const autosuggestProps = {
      suggestions, onSuggestionsFetchRequested, onSuggestionsClearRequested, getSuggestionValue, renderSuggestion,
      onSuggestionSelected, onSuggestionHighlighted, shouldRenderSuggestions,
      alwaysRenderSuggestions, highlightFirstSuggestion, focusInputOnSuggestionClick,
      multiSection, renderSectionTitle, getSectionSuggestions,
      renderSuggestionsContainer, theme, id
    }
    return (
      <Autosuggest
        renderInputComponent={ip => (
          <Input inputProps={ip} {...restProps} />
        )}
        inputProps={{
          value: fieldState.value || "",
          onChange: (event, {newValue, method}) => {
            fieldApi.setValue(newValue)
          },
          ...inputProps
        }}
        {...autosuggestProps}/>
    )
  })


export {Form};
