
[![Build Status](https://travis-ci.org/Kvitral/money-transfer.svg?branch=master)](https://travis-ci.org/Kvitral/money-transfer)
# Money transfer service

### API

#### accounts
*Method* : GET

*Path*: /accounts

  Parameter | Description
------------ | -------------
id | Id of account

~~~
curl -X GET \
  'http://localhost:8080/accounts?id=1' \
  -H 'cache-control: no-cache'
~~~


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


~~~
curl -X POST \
  http://localhost:8080/accounts \
  -H 'Content-Type: application/json' \
  -H 'cache-control: no-cache' \
  -d '{
	"from":1,
	"to":2,
	"amount":100,
	"currency":"RUB"
}'
~~~

### Gatling

Simple load test which performs transaction in parallel
to ensure that in-memory store working as expected.

Main goal is to perform transaction and "reverse" transaction so that
system will end up to it original state.

Because of that behavior we don`t transfer big amount of money
which can bring us to insufficient balance exceptions.

It is possible that 2 account id will match each other and it will end
up with request failure. It is desirable behavior.

To run in simply write in sbt console:
~~~
gatling-it:test
~~~

Notice
that if you run this test locally inside your idea project with server running
you will end up with very small rps rate but still it will to the job.


