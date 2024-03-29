# consumer-maven-archetype

Generates template projects for services that consume messages from Kafka.

### Note that this repo has been upgraded to Java 21.For the Java 11 compatible version please use the main-11 branch.


## Requirements

This project has been tested with Apache Maven 3.9.2.

Projects generated from this archetype require the following:

* Java 21
* Apache Maven
* The following dependencies:
  * uk.gov.companieshouse:structured-logging
  * uk.gov.companieshouse:api-sdk-manager-java-library
  * uk.gov.companieshouse:private-api-sdk-java
  * uk.gov.companieshouse:kafka-models

## Overview

Projects generated with consumer-maven-archetype are configured with a main consumer and an error consumer. The main
consumer consumes messages from the main Kafka topic configured for the service by `TOPIC`. The archetype does not
generate an error consumer and expects the kafka-error-consumer service to be used instead.

Messages data is forwarded by consumers to a
[Service](src/main/resources/archetype-resources/src/main/java/service/Service.java). The Service implementation should contain
all business logic required to process the message. If the Service throws a
[RetryableException](src/main/resources/archetype-resources/src/main/java/exception/RetryableException.java) then the message
will be republished to the configured retry message topic. If the maximum number of retry attempts as  determined by
`MAX_ATTEMPTS` has elapsed then the message will be republished to the error message topic. Conversely, if the Service
throws a [NonRetryableException](src/main/resources/archetype-resources/src/main/java/exception/NonRetryableException.java), 
the message will be republished to the configured invalid message topic. The
[default Service implementation](src/main/resources/archetype-resources/src/main/java/service/NullService.java) arbitrarily
throws a NonRetryableException whenever a message is processed.

## Usage

To install the archetype in your local Maven repository, run `mvn clean install`.

To generate a project using consumer-maven-archetype in interactive mode, run `mvn archetype:generate`, select the
archetype from the list and enter values for the required fields when prompted.

To generate a project in non-interactive mode, run the following command with required values for `description`,
`groupId`, `artifactId` and `version`:

```
mvn archetype:generate -DinteractiveMode=false \
    -DarchetypeGroupId=uk.gov.companieshouse \
    -DarchetypeArtifactId=consumer-maven-archetype \
    -DarchetypeVersion=latest \
    -Ddescription=Example -DgroupId=uk.gov.companieshouse -DartifactId=example-consumer -Dversion=latest`
```

The following arguments can be specified when generating projects using consumer-maven-archetype:

|Property|Description|Example|Required|
|--------|-----------|-------|--------|
|description|The project's description|Example project|true|
|groupId|The project's group ID|uk.gov.companieshouse|true|
|artifactId|The project's artifact ID|example-consumer|true|
|version|The project's version|latest|true|
|package|The package where template files will be rendered|uk.gov.companieshouse.delta|false|

## Configuration

Projects generated using consumer-maven-archetype require the following environment variables to be set:

| Variable                 |Type|Description|Example|
|--------------------------|----|-----------|-------|
| SERVER_PORT              |number|The port on which the embedded web server will run|8080|
| BOOTSTRAP_SERVER_URL     |url|The URLs of the Kafka brokers that the consumers will connect to|localhost:9092|
| GROUP_ID                 |string|The group ID of the main consumer|echo-consumer|
| MAX_ATTEMPTS             |number|The maximum number of times messages will be processed before they are sent to the dead letter topic|5|
| BACKOFF_DELAY            |number|The delay in milliseconds between message republish attempts|100|
| CONCURRENT_LISTENER_INSTANCES |number|The number of consumers that should participate in the consumer group. Must be equal to the number of main topic partitions.|10|
| TOPIC                    |string|The topic from which the main consumer will consume messages.|echo|
| INVALID_MESSAGE_TOPIC    |string|The topic to which consumers will republish messages if any unchecked exception other than RetryableException is thrown|echo-echo-consumer-invalid|
   