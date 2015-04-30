mc-mms-tls
==========

Sandbox project for testing the handling of SSL on the websocket client and server side.
 

Creating Self-signed Certificate
================================

On the server side, generate a keystore with a self-signed certificate under the "mms" alias. 
Use "localhost" for the CN (first and last name) and "changeit" for keystore and key passwords:

    keytool -genkeypair -alias mms -keyalg RSA -validity 365 -keystore <path>/server-keystore.jks


Since we are using a self-signed certificate, we need to add it to the clients trust store. First export it:
 
    keytool -export -alias mms -keystore <path>/server-keystore.jks -rfc -file <temp-path>/mmscert.cer


Next, generate and import the certificate in the client trust store under the "mms" alias.
Use "changeit" for the truststore passwords:

    keytool -import -alias mms -file <temp-path>/mmscert.cer -keystore <path>/client-truststore.jks




