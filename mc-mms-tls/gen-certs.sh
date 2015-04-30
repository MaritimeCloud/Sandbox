
# echo: Check out: https://sites.google.com/site/paratumignempetere/software-development/security/mutually-authenticated-secure-socket-layer

echo "Generating self-signed certificate keystores and truststores"
keytool -keystore server-keystore.jks -genkey -v -keyalg RSA -alias server -storepass changeit -keypass changeit -dname "CN=localhost, OU=Server, O=Unknown, L=Unknown, ST=Unknown, C=Unknown"
keytool -keystore server-keystore.jks -exportcert -v -alias server -storepass changeit -file server.der
keytool -keystore client-keystore.jks -genkey -v -keyalg RSA -alias client -storepass changeit -keypass changeit -dname "CN=localhost, OU=Client, O=Unknown, L=Unknown, ST=Unknown, C=Unknown"
keytool -keystore client-keystore.jks -exportcert -v -alias client -storepass changeit -file client.der
keytool -keystore server-truststore.jks -importcert -v -noprompt -trustcacerts -storepass changeit -alias client -file client.der
keytool -keystore client-truststore.jks -importcert -v -noprompt -trustcacerts -storepass changeit -alias server -file server.der

cp client-* mc-mms-tls-client-jetty/src/main/resources/
cp client-* mc-mms-tls-client-tomcat/src/main/resources/
cp server-* mc-mms-tls-server/src/main/resources/

rm client.der
rm server.der
rm client-*
rm server-*

