mc-mms-auth-ticket
==========================

Project used for exploring how a ticket of some form could be communicated from the WebSocket client endpoint
to the WebSocket server endpoint.

The approach in this test is to communicate the ticket, in the form of a basic authentication header, in the
request header of the initial upgrade HTTP request.

In this test, the server websocket endpoint will look for the header and convert it into a principal available 
for the entire endpoint.

If the server websocket endpoint had been running in a servlet container configured to accept basic authentication,
then the container would have set the principal automatically.

Test
========

Build: 

    mvn clean package

Run:

    java -jar target/mc-mms-auth-ticket-0.1.jar
