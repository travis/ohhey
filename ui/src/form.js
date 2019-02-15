import React, {Fragment} from 'react';

import {Form, asField} from 'informed';
import Input from '@material-ui/core/Input';
import * as informed from 'informed';

export const TextArea = ({inputProps, field, ...props}) =>
  (<Input multiline={true}
          inputComponent={({inputRef, ...props}) => <informed.TextArea {...props}/>}
          inputProps={{field}}
          {...props} />)
export const TextInput = ({inputProps, field, ...props}) =>
  (<Input inputComponent={({inputRef, ...props}) => <informed.Text {...props}/>}
          inputProps={{field}}
          {...props} />)


export {Form};
