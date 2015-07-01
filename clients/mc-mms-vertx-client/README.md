## MMS Vert.x Client

This MMS chat client is largely based on the work of the KMOU SQU team. Please refer to:
https://github.com/KMOUSQA/MCSA

In this simplified version, the client code does not depend on EPD, but accesses MMS directly.

The purpose of the project is to have a test-bench for the type of MMS client that handles 
multiple MMS clients on behalf of end-users.

## Prerequisites
* Java JDK 1.8
* Maven 3.x

## Building and running

    mvn clean package
    java -jar target/vertx-client.jar

The default MMS server is ws://mms.sandbox04.maritimecloud.net, but you can pass another MMS sever as an
argument to the application.
    
## Testing

Use a browser test client at: http://localhost:8080

Log in using a proper maritime id (e.g. "11111111") and send messages to another client.

The receiving client might also be a browser client, or e.g. an EPD client.





