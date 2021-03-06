mc-mms-tls
==========

Sandbox project for testing the handling of SSL on the websocket client and server side.
 

Testing Self-signed SSL Certificate
================================

Generate server and client keystores and truststores using:

    ./gen-certs.sh

To test server SSL certificate validation, run:

* On the server side, start _SSLServer_.
* On the client side, run the Tomcat and Jetty versions of _JavaX_SystemProperties_ and _JavaX_Programatically_.

To test both client and server SSL certificate validation, run:

* On the server side, start _SSLServerWithClientCertificateValidation_.
* On the client side, run the Tomcat and Jetty versions of _JavaX_ClientCertificate_.


Testing CA-issued Certificate
=============================

If you are using a CA-issued SSL certificate, consult your documentation regarding how to import the certificate + private key
into a java keystore.<br>
If you have used **keytool** to generate a CSR, you will have the private key in a keystore already. You then need to get hold of 
a PKCS-7 version of the certificate, and import it into the private key keystore. Something along the lines of:

    keytool -import -keystore maritimecloud.net.jks -alias maritimecloud.net -file cert.p7s

Next, ensure that

* You have added localhost.maritimecloud.net for localhost in /etc/hosts
* The keystore should be placed at ~/cert/maritimecloud.net.jks

Start the websocket server using the class: _SSL_CustomCertificate_. 
The java class must be run with a system property for KeyStorePassword, i.e. -DKeyStorePassword=XXX.

Test the tomcat or jetty _JavaX_NoTrust_ clients.


Testing with SSL Proxy Server
=========================

If you wish to test with a proxy-server, such as nginx, in front of MMS and use it to handle SSL-decryption, first convert the keystore to PKCS12:
  
    keytool -importkeystore -srckeystore maritimecloud.net.jks \
            -destkeystore maritimecloud.net.p12 -deststoretype PKCS12

Extract the pem (certificate) and key from  p12 keystore:

    openssl  pkcs12 -nokeys -in maritimecloud.net.p12 -out nginx-ssl.pem
    openssl pkcs12 -nocerts -nodes -in maritimecloud.net.p12 -out nginx-ssl.key

Configure nginx to point to the pem and key files using the *ssl_certificate* and *ssl_certificate_key* parameters.

