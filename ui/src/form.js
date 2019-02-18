import React, {Fragment} from 'react';

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
export const TextInput = ({inputProps, field, ...props}) =>
  (<Input inputProps={{field}}
          inputComponent={({inputRef, ...props}) => (
            <informed.Text forwardedRef={inputRef} {...props}/>
          )}
          {...props} />)


export {Form};
