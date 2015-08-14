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

import java.util.HashSet;
import java.util.Set;

/**
 * Used for parsed access token data
 */
@SuppressWarnings("unused")
public class AccessTokenData {

    String name;
    String userName;
    String email;
    String givenName;
    String familyName;
    Set<String> realmRoles = new HashSet<>();
    Set<String> resourceRoles = new HashSet<>();

    /**
     * Constructor
     */
    public AccessTokenData() {
    }

    /** Returns a string representation of the access token */
    @Override
    public String toString() {
        return "AccessTokenData{" +
                "name='" + name + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", realmRoles=" + realmRoles +
                ", resourceRoles=" + resourceRoles +
                '}';
    }

    // ** Getters and setters **

    private static String toString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public Set<String> getRealmRoles() {
        return realmRoles;
    }

    public void setRealmRoles(Set<String> realmRoles) {
        this.realmRoles = realmRoles;
    }

    public Set<String> getResourceRoles() {
        return resourceRoles;
    }

    public void setResourceRoles(Set<String> resourceRoles) {
        this.resourceRoles = resourceRoles;
    }
}
