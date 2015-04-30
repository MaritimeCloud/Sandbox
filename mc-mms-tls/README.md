mc-mms-tls
==========

Sandbox project for testing the handling of SSL on the websocket client and server side.
 

Testing Self-signed SSL Certificate
================================

On the server side, generate a keystore with a self-signed certificate under the "mms" alias. 
Use "localhost" for the CN (first and last name) and "changeit" for keystore and key passwords:

    keytool -genkeypair -alias mms -keyalg RSA -validity 365 -keystore <path>/server-keystore.jks


Since we are using a self-signed certificate, we need to add it to the clients trust store. First export it:
 
    keytool -export -alias mms -keystore <path>/server-keystore.jks -rfc -file <temp-path>/mmscert.cer


Next, generate and import the certificate in the client trust store under the "mms" alias.
Use "changeit" for the truststore passwords:

    keytool -import -alias mms -file <temp-path>/mmscert.cer -keystore <path>/client-truststore.jks

With the keystores in place, test the server side using the _SSLServer_ class.<br>
On the client side, use Tomcat and Jetty versions of _JavaX_SystemProperties_ and _JavaX_Programatically_.


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
The java class must be run with system properties for KeyStorePassword and KeyManagerPassword, i.e. -DKeyStorePassword=XXX -DKeyManagerPassword=YYY.

Test the tomcat or jetty _JavaX_NoTrust_ clients.

