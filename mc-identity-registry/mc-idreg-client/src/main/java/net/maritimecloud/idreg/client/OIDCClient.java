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
package net.maritimecloud.idreg.client;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * The OpenID Connect client has functionality for performing an authentication request, as described in
 * <a href="https://openid.net/specs/openid-connect-basic-1_0.html">the OpenID Connect specification.</a>
 * <p/>
 * Call {@code redirectToAuthServer()} to prepare the authentication request and redirect to
 * the OpenID Connect authorization server (section 2.1.1 - 2.1.2).
 * <p/>
 * When the callback URL is invoked by the authorization server, call {@code handleAuthServerCallback()} to make
 * a subsequent token request (section 2.1.6). If the returned ID token is valid, user information
 * is collected and returned. Otherwise, an {@code Exception} is thrown.
 */
public interface OIDCClient {

    ServiceLoader<Builder> builderLoader = ServiceLoader.load(Builder.class);

    /**
     * Creates a new OpenID Connect builder
     *
     * @return the new initialized OpenID Connect client
     */
    static Builder newBuilder() {
        return builderLoader.iterator().next();
    }

    /**
     * Redirects to the OpenID Connect auth server
     *
     * @param response the HTTP servlet response
     * @param callbackUrl the callback url
     */
    void redirectToAuthServer(HttpServletResponse response, String callbackUrl) throws IOException;

    /**
     * Based on the request received from the authorization server, make
     * a token request and collect user information.
     *
     * @param request the HTTP servlet request
     * @param callbackUrl the callback url
     */
    AccessTokenData handleAuthServerCallback(HttpServletRequest request, String callbackUrl) throws AuthErrorException;

    /**
     * Constructs an OpenID Connect client
     */
    abstract class Builder {

        protected Properties properties;
        protected Reader configuration;
        protected String customClaims[];

        /**
         * Sets the OpenID Connect client configuration based on a Reader
         *
         * @param configuration the configuration
         * @return the builder
         */
        public Builder configuration(Reader configuration) {
            this.configuration = configuration;
            return this;
        }

        /**
         * Sets the OpenID Connect client configuration based on properties
         *
         * @param properties the properties
         * @return the builder
         */
        public Builder configuration(Properties properties) {
            this.properties = properties;
            return this;
        }

        /**
         * Sets the custom claims to extract from the access token
         *
         * @param customClaims the custom claims to extract from the access token
         * @return the builder
         */
        public Builder customClaims(String... customClaims) {
            this.customClaims = customClaims;
            return this;
        }

        /**
         * Builds the OpenID Connect client
         * @return the instantiated OpenID Connect client
         */
        public abstract OIDCClient build() throws Exception;
    }
}
