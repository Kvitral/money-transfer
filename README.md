
[![Build Status](https://travis-ci.org/Kvitral/money-transfer.svg?branch=master)](https://travis-ci.org/Kvitral/money-transfer)
# Money transfer service

### API

#### accounts
*Method* : GET

*Path*: /accounts

  Parameter | Description
------------ | -------------
id | Id of account


#### accounts
*Method*: POST

*Path*: /accounts

*Entity Type* : JSON

  Parameter | Description
------------ | -------------
from | Id of an account from which money will be substracted
to | Id of account to which money will be added
amount | money amount in double
currency | currency of transaction
