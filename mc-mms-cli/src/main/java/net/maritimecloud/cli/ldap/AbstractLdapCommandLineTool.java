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
import dk.dma.commons.app.AbstractCommandLineTool;

/**
 * Abstract base class for LDAP command line tools
 */
public abstract class AbstractLdapCommandLineTool extends AbstractCommandLineTool {

    @Parameter(names="-b", description = "The base dn to use")
    String baseDN = "dc=MaritimeCloud,dc=Net";

    @Parameter(names="-p", description = "The port")
    int port = 10389;

}
