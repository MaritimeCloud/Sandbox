mc-mms-tls-client-javaee
==========================

A standard JavaEE (web) application that uses the container-provided Tomcat websocket provider.

The web application is based on a sprint-boot Tomcat sample application found at:
https://github.com/spring-projects/spring-boot/tree/master/spring-boot-samples/spring-boot-sample-tomcat

A dependency has been added for the *mc-mms-client-javaee* client library, to ensure that the container-provided 
Tomcat websocket factory is used.

## Testing

To test the application, first start the **net.maritimecloud.mms.server.Main** MMS server class with the arguments:

    -port 43234 -securePort 43235 -rest 9090 -accessLog stdout \
    -keystore ../Sandbox/mc-mms-tls/mc-mms-tls-server/src/main/resources/server-keystore.jks \
    -keystorePassword changeit \
    -truststore ../Sandbox/mc-mms-tls/mc-mms-tls-server/src/main/resources/server-truststore.jks \
    -truststorePassword changeit

NB: Update the paths to point to the test keystore and truststore of the MartitimeCloud/Sandbox project.

Then start the *mc-mms-tls-client-javaee* by running the *main* method of the **sample.tomcat.SampleTomcatApplication** class.
