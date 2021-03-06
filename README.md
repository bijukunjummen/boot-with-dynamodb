## Sample With Spring Boot Webflux and DynamoDB (using AWS SDK 2+)

This sample demonstrates an end to end reactive application using Spring Boot 2 Webflux
and reactive/non-blocking AWS SDK 2+

## Setting up a local stack version of AWS Services

Follow the instructions [here](https://github.com/localstack/localstack)
to setup a local version of AWS DynamoDB.

Start up localstack:

```
cd localstack
./start-localstack-mac.sh
# OR docker-compose up
```

## Start up the Application

```
./gradlew bootRun
```


## Testing

Make sure that a table has been created in dynamoDB:

```
 aws --endpoint-url=http://localhost:4566 dynamodb describe-table --table-name hotels
```

Create Hotel entities:

```
http -v :9080/hotels id=1 name=test1 address=address1 zip=zip1 state=OR
http -v :9080/hotels id=2 name=test2 address=address2 zip=zip2 state=OR
http -v :9080/hotels id=3 name=test3 address=address3 zip=zip3 state=WA
```


Get Hotels by State names:

```
http ":9080/hotels?state=OR"
http ":9080/hotels?state=WA"
```

Get Hotels by ID:

```
http ":9080/hotels/1"
http ":9080/hotels/2"
http ":9080/hotels/3"
```

Update Hotel:
```
http PUT :9080/hotels/1 name=test1updated address=address1 zip=zip1 state=OR version=1
```

Version Demo:
```
http :9080/hotels id=4 name=name address=address zip=zip state=OR
http PUT :9080/hotels/4 name=name1 address=address zip=zip state=OR version=1
http PUT :9080/hotels/4 name=name1 address=address1 zip=zip state=OR version=2
```


