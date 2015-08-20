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
package net.maritimecloud.idreg;

import net.maritimecloud.idreg.client.AccessTokenData;
import net.maritimecloud.idreg.client.AuthErrorException;
import net.maritimecloud.idreg.client.OIDCClient;
import net.maritimecloud.idreg.client.OIDCUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used for OpenID Connect authentication
 */
@Controller
@SuppressWarnings("unused")
public class OIDCService {

    private final static Logger log = Logger.getLogger(OIDCService.class.getName());

    private OIDCClient oidcClient;

    /**
     * Called when the service is initialized
     */
    @PostConstruct
    public void init() throws Exception {
        Reader configFile = new InputStreamReader(OIDCService.class.getResourceAsStream("/keycloak.json"));
        oidcClient = OIDCClient.newBuilder()
            .configuration(configFile)
            .customClaims("mmsi")
            .build();
    }

    /**
     * Called in order to authenticate via OpenID Connect.
     *
     * @param request the servlet request
     * @param response the servlet response
     * @throws IOException
     */
    @RequestMapping(value = "/oidc-login")
    public void oidcLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("OpenID Connect login called");
        OIDCUtils.nocache(response);
        String callbackUrl = OIDCUtils.getUrl(request, "/oidc-callback");
        oidcClient.redirectToAuthServer(response, callbackUrl);
    }

    /**
     * The callback endpoint called by the OpenID Connect service.
     *
     * @param request the servlet request
     * @param response the servlet response
     * @throws IOException
     */
    @RequestMapping(value = "/oidc-callback")
    public void oidcCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getSession() != null) {
            request.getSession().invalidate();
        }

        log.info("OpenID Connect callback called");
        try {
            OIDCUtils.nocache(response);
            String callbackUrl = OIDCUtils.getUrl(request, "/oidc-callback");
            AccessTokenData accessTokenData = oidcClient.handleAuthServerCallback(request, callbackUrl);
            log.info("OpenID Connect authentication success: " + accessTokenData);
            request.getSession(true).setAttribute("user", accessTokenData);
        } catch (AuthErrorException e) {
            log.log(Level.SEVERE, "OpenID Connect authentication error", e);
            request.getSession(true).setAttribute("error", e.getMessage());
        }

        response.sendRedirect("/");
    }

    /**
     * Returns the currently logged in user
     * @return the currently logged in user
     */
    @RequestMapping(
            value = "/user",
            method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public AccessTokenData getUser(HttpServletRequest request) {
        return (AccessTokenData)request.getSession(true).getAttribute("user");
    }

    /**
     * Returns and removes the last error
     * @return the last error
     */
    @RequestMapping(
            value = "/error",
            method = RequestMethod.GET,
            produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String getError(HttpServletRequest request) {
        if (request.getSession() != null) {
            String error = (String)request.getSession().getAttribute("error");
            request.getSession().removeAttribute("error");
            return error;
        }
        return null;
    }

    /**
     * Invalidates the user http session.
     *
     * @param response the servlet response
     * @throws IOException
     */
    @RequestMapping(value = "/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getSession() != null) {
            request.getSession().invalidate();
        }
        response.sendRedirect("/");
    }


    /**
     * Logs the user out and redirects to the auth server to log out the session there
     *
     * @param request the servlet request
     * @param response the servlet response
     * @throws IOException
     */
    @RequestMapping(value = "/logout-auth-server")
    public void logoutAuthServer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getSession() != null) {
            request.getSession().invalidate();
        }

        OIDCUtils.nocache(response);
        String callbackUrl = OIDCUtils.getUrl(request);
        oidcClient.redirectToAuthServerLogout(response, callbackUrl);
    }

    /**
     * Redirects to the edit-account page for the current user
     *
     * @param response the servlet response
     * @throws IOException
     */
    @RequestMapping(value = "/account")
    public void editAccount(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OIDCUtils.nocache(response);
        oidcClient.redirectToAuthServerAccount(response);
    }

}
