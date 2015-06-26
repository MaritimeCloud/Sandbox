## MMS Wildfly Client

The main purpose of this MMS Wildfly client is to demonstrate how to integrate an MMS service,
in this case the *MaritimeTextingService* service, into a JEE application.

The classes in the *net.maritimecloud.mms.client.generated* package could in theory be auto-generated 
by the maven msdl plugin.

To test that the resulting MmsTextService implementation works properly with standard JEE concepts such
as injection and transactions, each message sent and received is persisted.

Also, to test error handling, 50% of the calls will fail with an exception.

Test sending MMS text messages to all registered *MaritimeTextingService* services using the REST endpoint:

* http://localhost:8080/rest/chat/broadcast?iterations=20&msg=kurt2333

You can test receiving text messages by e.g. running EPD-Ship and sending a chat message to the 
"7771112222" maritime id.

## Prerequisites
* Java JDK 1.8
* Maven 3.x
* JBoss Wildfly 8.2.0.Final or later

## Initial setup

### JBoss Wildfly
Install and configure the Wildfly application server by running:

    ./install-widlfly.sh

You can now run the Wildfly app server using:

    ./wildfly-8.2.0.Final/bin/standalone.sh


## Deploying to Wildfly

Build the project and deploy it using:

    mvn clean install
    mvn wildfly:deploy


