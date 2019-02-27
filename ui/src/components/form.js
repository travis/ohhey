import React from 'react';

import {Form, asField} from 'informed';
import Input from '@material-ui/core/Input';
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
  (<Input inputProps={{field, forwardedRef, validate, validateOnChange, validateOnBlur}}
          inputComponent={IText}
          {...props} />)

export {Form};
