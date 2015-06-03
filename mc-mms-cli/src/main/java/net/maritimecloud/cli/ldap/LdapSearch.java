/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.maritimecloud.cli.ldap;

import com.beust.jcommander.Parameter;
import com.google.inject.Injector;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;

/**
 * Command line utility for performing an LDAP search
 * and print out the result in an LDIF-like format.
 * <p>
 * Parameters are somewhat based on the <code>ldapsearch</code> command:
 * <a href="http://linuxcommand.org/man_pages/ldapsearch1.html">ldapsearch command</a>
 */
public class LdapSearch extends AbstractLdapCommandLineTool {

    @Parameter(names="-h", description = "The host")
    String host = "localhost";

    @Parameter(names="-D", description = "The bind dn")
    String bindDN = "uid=admin,ou=system";

    @Parameter(names="-w", description = "The bind password")
    String password = "secret";

    // Main parameter. Optionally specifies a search filter and returning attributes
    @Parameter(description = "Search filter and list of returned attributes")
    List<String> mainParams = new ArrayList<>();

    /**
     * Returns the combined host and port
     * @return the combined host and port
     */
    public String getHostAndPort() {
        if (host.indexOf(':') != -1) {
            return host;
        }
        return host + ":" + port;
    }

    /**
     * Returns the provider URL
     * @return the provider URL
     */
    public String getProviderURL() {
        return  "ldap://" + getHostAndPort();
    }

    /**
     * Returns the LDAP Naming Context environment
     * @return the LDAP Naming Context environment
     */
    protected Hashtable<Object, Object> createEnv() {

        Hashtable<Object, Object> env = new Hashtable<>();
        env.put(Context.PROVIDER_URL, getProviderURL());
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

        env.put(Context.SECURITY_PRINCIPAL, bindDN);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");

        return env;
    }

    /**
     * Performs a simple ldap search
     */
    @Override
    protected final void run(Injector injector) throws Exception {

        // Get hold of filter and returning attributes from
        String filter = !mainParams.isEmpty()
                ? mainParams.get(0)
                : "(objectClass=*)";

        String[] attributes = mainParams.size() > 1
                ? mainParams.stream().skip(1).toArray(String[]::new)
                : new String[] { "*", "+" };

        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(createEnv());

            SearchControls ctls = new SearchControls();
            ctls.setReturningAttributes(attributes);
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> result = ctx.search(baseDN, filter, ctls);
            while (result.hasMore()) {
                SearchResult entry = result.next();
                System.out.println("\ndn: " + entry.getNameInNamespace());
                Attributes as = entry.getAttributes();

                NamingEnumeration<String> ids = as.getIDs();
                while (ids.hasMore()) {
                    String id = ids.next();
                    Attribute attr = as.get(id);
                    for (int i = 0; i < attr.size(); ++i) {
                        if (attr.get(i) instanceof byte[]) {
                            System.out.println(id + ":: " + Base64.getEncoder().encodeToString((byte[]) attr.get(i)));
                        } else {
                            System.out.println(id + ": " + attr.get(i));
                        }
                    }
                }
            }

        } catch (NamingException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * Main method of the LdapSearch class
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        new LdapSearch().execute(args);
    }
}
