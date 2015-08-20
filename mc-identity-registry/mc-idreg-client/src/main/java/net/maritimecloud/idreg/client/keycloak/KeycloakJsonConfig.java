/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.maritimecloud.idreg.client.keycloak;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.Reader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.maritimecloud.idreg.client.OIDCUtils.encode;

/**
 * Wraps a keycloak.json configuration file.
 * <p/>
 * Example file:
 * <pre>
 * {
 *    "realm": "demo",
 *    "realm-public-key": "MIIBIjANBgkqhkiG9w0BA ... 8UXykoPr10o0QIDAQAB",
 *    "auth-server-url": "http://localhost:8090/auth",
 *    "ssl-required": "external",
 *    "resource": "msinm",
 *    "credentials": {
 *      "secret": "e8f6f46a-c0c6-47c5-b2e7-d738de328474"
 *    },
 *    "use-resource-role-mappings": true
 *  }
 * </pre>
 */
public class KeycloakJsonConfig {

    private static final String KEYCLOAK_ISSUER_TEMPLATE
            = "%s/realms/%s";

    private static final String KEYCLOAK_AUTH_REQUEST_TEMPLATE
            = "%s/realms/%s/protocol/openid-connect/auth?response_type=code&client_id=%s&redirect_uri=%s&state=%s";

    private static final String KEYCLOAK_TOKEN_TEMPLATE
            = "%s/realms/%s/protocol/openid-connect/token";

    private static final String KEYCLOAK_AUTH_LOGOUT_TEMPLATE
            = "%s/realms/%s/protocol/openid-connect/logout?redirect_uri=%s";

    private final static Logger log = Logger.getLogger(KeycloakJsonConfig.class.getName());

    private JsonObject keycloakJson;

    /**
     * Constructor
     *
     * @param settings the keycloak.json file
     * @throws IOException
     */
    private KeycloakJsonConfig(Reader settings) throws IOException {
        try {
            try (JsonReader rdr = Json.createReader(settings)) {
                keycloakJson = rdr.readObject();
                log.info("Read Keycloak JSON configuration");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error reading Keycloak JSON configuration.",e);
            throw new IOException(e);
        }
    }

    /**
     * Factory method for reading the keycloak.json configuration file
     * @param reader the keycloak.json configuration file
     * @return the initialized configuration entity
     * @throws IOException
     */
    public static KeycloakJsonConfig load(Reader reader) throws IOException {
        return new KeycloakJsonConfig(reader);
    }

    /**
     * Returns the client ID of the configuration
     * @return the client ID of the configuration
     */
    public String getClientId() {
        return keycloakJson.getString("resource");
    }

    /**
     * Returns the issuer ID of the Keycloak server
     * @return the issuer ID of the Keycloak server
     */
    public String getIssuer() {
        return String.format(
                KEYCLOAK_ISSUER_TEMPLATE,
                keycloakJson.getString("auth-server-url"),
                encode(keycloakJson.getString("realm")));
    }

    /**
     * Returns the client secret of the configuration
     * @return the client secret of the configuration
     */
    public String getClientSercret() {
        try {
            return keycloakJson.getJsonObject("credentials").getString("secret");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Constructs an authentication URL for the Keycloak server
     *
     * @param callbackUrl the callback URL
     * @param state the state code
     * @return the authentication URL to re-direct to
     */
    public String getAuthRequest(String callbackUrl, String state) {
        // Create the URL for the authentication request
        return String.format(
                KEYCLOAK_AUTH_REQUEST_TEMPLATE,
                keycloakJson.getString("auth-server-url"),
                encode(keycloakJson.getString("realm")),
                encode(keycloakJson.getString("resource")),
                encode(callbackUrl),
                encode(state));
    }

    /**
     * Constructs a token request URL for the Keycloak server
     *
     * @return the token request URL
     */
    public String getTokenRequest() {
        // Create the URL for the token request
        return String.format(
                KEYCLOAK_TOKEN_TEMPLATE,
                keycloakJson.getString("auth-server-url"),
                encode(keycloakJson.getString("realm")));
    }

    /**
     * Decodes the public key of the keycloak json configuration
     */
    public PublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        String pem = keycloakJson.getString("realm-public-key");
        pem = pem.replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)----", "")
                .replaceAll("\r\n", "")
                .replaceAll("\n", "")
                .trim();
        byte[] der = Base64.getDecoder().decode(pem);
        return KeyFactory
                //.getInstance("RSA", "BC")
                .getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(der));
    }


    /**
     * Constructs an logout URL for the Keycloak server
     *
     * @param callbackUrl the callback URL
     * @return the logout URL to re-direct to
     */
    public String getLougoutRequest(String callbackUrl) {
        // Create the URL for the logout request
        return String.format(
                KEYCLOAK_AUTH_LOGOUT_TEMPLATE,
                keycloakJson.getString("auth-server-url"),
                encode(keycloakJson.getString("realm")),
                encode(callbackUrl));
    }
}
