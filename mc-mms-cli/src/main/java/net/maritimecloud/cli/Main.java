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
package net.maritimecloud.cli;

import dk.dma.commons.app.CliCommandList;
import net.maritimecloud.cli.benchmark.Benchmark;
import net.maritimecloud.cli.ldap.LdapSearch;
import net.maritimecloud.cli.ldap.LdapServer;
import net.maritimecloud.cli.proxy.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class called from the command line
 */
public class Main {

    static final Logger LOG = LoggerFactory.getLogger(Main.class);

    /**
     * Main command line execution point
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CliCommandList c = new CliCommandList("MmsCli");

        c.add(Benchmark.class, "benchmark", "Benchmarks the MMS server");
        c.add(Proxy.class, "proxy", "Runs a proxy MMS server");

        c.add(LdapServer.class, "ldapserver", "Runs an Apache DS LDAP server");
        c.add(LdapSearch.class, "ldapsearch", "Runs an Apache DS LDAP search command");

        try {
            c.invoke(args);
        } catch (Exception e) {
            LOG.error("Error invoking MMS CLI", e);
        }
    }

}
