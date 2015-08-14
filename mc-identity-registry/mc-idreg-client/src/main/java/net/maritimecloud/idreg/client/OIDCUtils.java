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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility methods used in the OpenID Connect handling
 */
@SuppressWarnings("unused")
public class OIDCUtils {

    private static final AtomicLong stateCodeCounter = new AtomicLong();

    private OIDCUtils() {}

    /**
     * Returns a state code
     *
     * @return a state code
     */
    public static String getStateCode() {
        return stateCodeCounter.getAndIncrement() + "/" + UUID.randomUUID().toString();
    }

    /**
     * URL encodes the given value in UTF-8 with no exception thrown
     *
     * @param val the value to encode
     * @return the encoded value
     */
    public static String encode(String val) {
        if (val == null) {
            return null;
        }

        try {
            return URLEncoder.encode(val, "utf-8");
        } catch (UnsupportedEncodingException e) {
            // Should never happen...
            throw new IllegalArgumentException("Error encoding value " + val, e);
        }
    }

    /**
     * Add headers to the response to ensure no caching takes place
     * @param response the response
     * @return the response
     */
    public static HttpServletResponse nocache(HttpServletResponse response) {
        response.setHeader("Cache-Control","no-cache");
        response.setHeader("Cache-Control","no-store");
        response.setHeader("Pragma","no-cache");
        response.setDateHeader ("Expires", 0);
        return response;
    }

    /**
     * Returns the base URL of the request
     *
     * @param request the request
     * @param appends list of URI components to append
     * @return the base URL + optional appends
     */
    public static String getUrl(HttpServletRequest request, String... appends) {
        String result = String.format(
                "%s://%s%s%s",
                request.getScheme(),
                request.getServerName(),
                request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort(),
                request.getContextPath());
        for (String a : appends) {
            result = result + a;
        }
        return result;
    }

    /**
     * Returns the value associated with the given cookie, or null if not present
     *
     * @param request the request
     * @param name the name of the cookie
     * @return the value of the cookie, or null if not present
     */
    public static String getCookieValue(HttpServletRequest request, String name) {
        return request.getCookies() == null
                ? null
                : Arrays.stream(request.getCookies())
                        .filter(c -> c.getName().equals(name))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElse(null);
    }

}
