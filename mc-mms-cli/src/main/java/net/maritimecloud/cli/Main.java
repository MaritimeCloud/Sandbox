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

        try {
            c.invoke(args);
        } catch (Exception e) {
            LOG.error("Error invoking MMS CLI", e);
        }
    }

}
