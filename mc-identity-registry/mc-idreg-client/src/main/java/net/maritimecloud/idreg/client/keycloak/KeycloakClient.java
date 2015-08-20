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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import net.maritimecloud.idreg.client.AccessTokenData;
import net.maritimecloud.idreg.client.AuthErrorException;
import net.maritimecloud.idreg.client.OIDCClient;
import net.maritimecloud.idreg.client.OIDCUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Keycloak implementation of the {@code OIDCClient}
 */
public class KeycloakClient implements OIDCClient {

    private static final String OAUTH_TOKEN_REQUEST_STATE
            = "OAuth_Token_Request_State";

    private final static Logger log = Logger.getLogger(KeycloakClient.class.getName());

    private KeycloakJsonConfig config;
    private String[] customClaims;

    /**
     * Constructor
     * @param config the configuration
     * @param customClaims optionally, a list of custom claims
     */
    private KeycloakClient(KeycloakJsonConfig config, String[] customClaims) {
        this.config = Objects.requireNonNull(config);
        this.customClaims = customClaims;
    }


    /** {@inheritDoc} */
    @Override
    public void redirectToAuthServerLogout(HttpServletResponse response, String callbackUrl) throws IOException {
        // Redirect to the logout request
        String url = config.getLougoutRequest(callbackUrl);
        log.log(Level.FINE, "Redirecting to logout request: " + url);
        response.sendRedirect(url);
    }


    /** {@inheritDoc} */
    public void redirectToAuthServer(HttpServletResponse response, String callbackUrl) throws IOException {

        // Create a state code used for Cross-Site Request Forgery (CSRF, XSRF) prevention
        String state = OIDCUtils.getStateCode();

        // Set up cookie used for Cross-Site Request Forgery (CSRF, XSRF) prevention
        Cookie cookie = new Cookie(OAUTH_TOKEN_REQUEST_STATE, state);
        //cookie.setSecure(isSecure);
        cookie.setPath("/");
        response.addCookie(cookie);

        // Redirect to the authentication request
        String url = config.getAuthRequest(callbackUrl, state);
        log.log(Level.FINE, "Redirecting to auth request: " + url);
        response.sendRedirect(url);
    }


    /** {@inheritDoc} */
    public AccessTokenData handleAuthServerCallback(HttpServletRequest request, String callbackUrl) throws AuthErrorException {

        Map<String, String[]> reqParams = request.getParameterMap();

        if (reqParams.containsKey("error")) {

            // Handle errors received from the Keycloak server
            String errorMessage = reqParams.get("error")[0];
            throw new AuthErrorException("Failed Keycloak authentication: " + errorMessage);

        } else if (reqParams.containsKey("code")) {
            String code = request.getParameter("code");

            // Check the state parameter against the state cookie
            checkStateParam(request);

            return executeAuthServerTokenRequest(code, callbackUrl, false);

        } else {

            // Invalid request
            throw new AuthErrorException("Invalid Keycloak callback request");
        }

    }


    /**
     * Execute the auth server token request
     *
     * @param code the code
     * @param callbackUrl the callback URL
     * @param publicClient whether this is a public client or not
     * @return the parsed token result
     */
    private AccessTokenData executeAuthServerTokenRequest(String code, String callbackUrl, boolean publicClient) throws AuthErrorException {

        // Assemble the POST request
        HttpPost post = new HttpPost(config.getTokenRequest());
        try {
            List<NameValuePair> formparams = new ArrayList<>();
            formparams.add(new BasicNameValuePair("grant_type", "authorization_code"));
            formparams.add(new BasicNameValuePair("code", code));
            formparams.add(new BasicNameValuePair("redirect_uri", callbackUrl));
            if (publicClient) {
                formparams.add(new BasicNameValuePair("client_id", config.getClientId()));
            } else if (config.getClientSercret() != null) {
                String secret = config.getClientId() + ":" + config.getClientSercret();
                post.setHeader(
                        "Authorization",
                        "Basic " + Base64.getEncoder().encodeToString(secret.getBytes("UTF-8"))
                );
            }
            post.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AuthErrorException("Error composing token request", e);
        }

        // Execute the HTTP request
        String result;
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            result = (entity == null) ? null : EntityUtils.toString(entity);
            if (result == null || status != 200) {
                throw new AuthErrorException("Error result from token request: " + result);
            }
        } catch (IOException e) {
            throw new AuthErrorException("Error making token request to: " + config.getTokenRequest(), e);
        }

        // Parse the resulting JSON token
        return parseAndValidateTokenResponse(result);
    }


    /**
     * Parses the JSON token received from the auth server upon issuing a token request.
     * The retuned token should contain the fields defined in section 2.1.6.2 of the specification.
     *
     * @param token the JSON token to parse
     * @return the parsed JSON token
     */
    private AccessTokenData parseAndValidateTokenResponse(String token) throws AuthErrorException {

        // Parse the JSON result
        try (JsonReader rdr = Json.createReader(new StringReader(token))) {
            JsonObject json = rdr.readObject();

            // Validate toke type
            String tokenType = json.getString("token_type");
            if (!"bearer".equalsIgnoreCase(tokenType)) {
                throw new AuthErrorException("Invalid token type: " + tokenType);
            }

            // TODO: Validate various fields of ID token
            //JsonObject idToken = json.getJsonObject("id_token");

            // Get user info from access token
            String accessToken = json.getString("access_token");
            try {
                // Check that signature is valid upon parsing the JWT token
                Claims claims = Jwts.parser()
                        .setSigningKey(config.getPublicKey())
                        .parseClaimsJws(accessToken)
                        .getBody();

                // Validate the JWT fields
                if (!config.getIssuer().equals(claims.getIssuer())) {
                    throw new AuthErrorException("Invalid access token issuer " + claims.getIssuer());
                }
                if (!config.getClientId().equals(claims.getAudience())) {
                    throw new AuthErrorException("Invalid access token audience " + claims.getIssuer());
                }
                if (claims.getExpiration().before(new Date())) {
                    throw new AuthErrorException("JWT token expired at " + claims.getExpiration());
                }

                // Read out user info
                AccessTokenData data = new AccessTokenData();
                data.setName(toString(claims.get("name")));
                data.setUserName(toString(claims.get("preferred_username")));
                data.setEmail(toString(claims.get("email")));
                data.setGivenName(toString(claims.get("given_name")));
                data.setFamilyName(toString(claims.get("family_name")));

                // Keycloak-specific roles encoding
                addRoles(data.getRealmRoles(), claims.get("realm_access"));
                if (claims.get("resource_access") != null) {
                    addRoles(data.getResourceRoles(), ((Map)claims.get("resource_access")).get(config.getClientId()));
                }

                // Read out custom claims
                if (customClaims != null) {
                    Arrays.stream(customClaims)
                            .filter(claims::containsKey)
                            .forEach(c -> data.getCustomClaims().put(c, claims.get(c)));
                }

                return data;

            } catch (AuthErrorException ae) {
                throw ae;
            } catch (SignatureException se) {
                throw new AuthErrorException("Access token has invalid signature", se);
            } catch (Exception e) {
                throw new AuthErrorException("Error parsing access token", e);
            }
        }
    }


    /**
     * Validate the the 'state' request parameter matches the state cookie.
     * Throws an exception if the state values are invalid or differ.
     */
    protected void checkStateParam(HttpServletRequest request) throws AuthErrorException {
        String stateCookie = OIDCUtils.getCookieValue(request, OAUTH_TOKEN_REQUEST_STATE);
        if (stateCookie == null || !stateCookie.equals(request.getParameter("state"))) {
            throw new AuthErrorException("Invalid 'state' value");
        }
    }


    /** Add roles from the roleMap to the roles list */
    @SuppressWarnings("unchecked")
    private void addRoles(Set<String> roles, Object rolesMap) {
        if (rolesMap != null && rolesMap instanceof Map) {
            Object rolesList = ((Map) rolesMap).get("roles");
            if (rolesList != null && rolesList instanceof List) {
                roles.addAll((List<String>) rolesList);
            }
        }
    }


    /** Null-safe version of returning the toString() of an object */
    private static String toString(Object obj) {
        return obj == null ? null : obj.toString();
    }


    /**
     * Used for building the Keycloak client
     */
    @SuppressWarnings("unused")
    public static class KeycloakClientBuilder extends OIDCClient.Builder {

        /** {@inheritDoc} */
        @Override
        public OIDCClient build() throws Exception {

            // Properties are currently not supported
            if (properties != null) {
                throw new IllegalArgumentException("You must instantiate a Keycloak client from a keycloak.json Reader");
            }

            // You can only instantiate the Keycloak client using a keycloak.json Reader
            Objects.requireNonNull(configuration, "You must instantiate a Keycloak client from a keycloak.json Reader");

            try {
                KeycloakJsonConfig config = KeycloakJsonConfig.load(configuration);
                KeycloakClient client = new KeycloakClient(config, customClaims);
                log.info("Initialized Keycloak client");

                return client;
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error initialized Keycloak client. Service not enabled.");
                throw e;
            }
        }

    }

}
