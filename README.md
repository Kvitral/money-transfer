
[![Build Status](https://travis-ci.org/Kvitral/scala-money-store.svg?branch=master)](https://travis-ci.org/Kvitral/scala-money-store)
# Money transfer service

### API

#### getAccounts
*Method* : GET
*Path*: /getAccounts

  Parameter | Description
------------ | -------------
accountId | Id of account


#### transfer
*Method*: POST
*Path*: /getAccounts

*Entity Type* : JSON

  Parameter | Description
------------ | -------------
from | Id of an account from which money will be substracted
to | Id of account to which money will be added
amount | money amount in double
currency | currency of transaction
