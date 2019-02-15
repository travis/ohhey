import React, {Fragment} from 'react';

import {Form, asField} from 'informed';

import * as g from 'grommet';


const asInformed = (GrommetInput) => asField(
  ({fieldState, fieldApi: {setValue}, ...props}) => (
    <Fragment>
      <GrommetInput {...props} onChange={(e) => setValue(e.target.value)}/>
      {fieldState.error ? (<small style={{color: 'red'}}>{fieldState.error}</small>) : null}
    </Fragment>
  )
)

export const TextArea = asInformed(g.TextArea)
export const Text = asInformed(g.TextInput)
export const TextInput = asInformed(g.TextInput)


export {Form};
