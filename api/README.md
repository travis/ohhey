# truth

A Clojure library designed to ... well, that part is up to you.

## Usage

## bastion test

``` bash
export DATOMIC_SYSTEM=dev
export DATOMIC_REGION=us-east-1
export DATOMIC_SOCKS_PORT=8182

curl -x socks5h://localhost:$DATOMIC_SOCKS_PORT http://entry.$DATOMIC_SYSTEM.$DATOMIC_REGION.datomic.net:8182/
```

## starting local socks proxy

```bash
./bin/datomic-socks-proxy -p ohhey -r us-east-1 dev
```


FIXME

## License

Copyright © 2019 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
