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
package net.maritimecloud.internal.client.configuration;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.maritimecloud.internal.msdl.db.DefaultMsdlDatabase;
import net.maritimecloud.msdl.model.EndpointDefinition;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

/**
 *
 * @author Kasper Nielsen
 */
public class ServiceConfiguration extends YamlParser {

    private static final String STR_DESTINATION = "destination";

    private static final String STR_TYPES = "type";

    private final List<ServiceGroup> groups = new ArrayList<>();

    /**
     * @return the groups
     */
    public List<ServiceGroup> getGroups() {
        return groups;
    }

    public ServiceConfiguration addGroup(ServiceGroup group) {
        groups.add(requireNonNull(group));
        return this;
    }

    void populate(DefaultMsdlDatabase database, InputStream configuration) {
        Yaml y = new Yaml();

        for (Node n : y.composeAll(new InputStreamReader(configuration))) {
            ServiceGroup group = new ServiceGroup();
            MappingNode nn = mappingNode(n);
            for (NodeTuple t : nn.getValue()) {
                ScalarNode key = (ScalarNode) t.getKeyNode();
                if (STR_DESTINATION.equals(key.getValue())) {
                    group.setDestination(readUrl(t.getValueNode()));
                } else if (STR_TYPES.equals(key.getValue())) {
                    SequenceNode sn = sequenceNode(t.getValueNode());
                    for (Node no : sn.getValue()) {
                        String endpointType = readString(no);
                        EndpointDefinition em = database.getEndpointDefinition(endpointType);
                        if (em == null) {
                            throw new IllegalArgumentException("Unknown endpointType type '" + endpointType + "'"
                                    + no.getStartMark());
                        }
                        group.addEndpoint(em);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown tag '" + key.getValue() + "'" + key.getStartMark());
                }
            }
            groups.add(group);
        }
    }

    public static class ServiceGroup {
        final List<EndpointDefinition> broadcastTypes = new ArrayList<>();

        URL destination;

        public ServiceGroup setDestination(File destination) {
            try {
                return setDestination(destination.toURI().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }


        public ServiceGroup setDestination(URL destination) {
            this.destination = destination;
            return this;
        }

        public ServiceGroup addEndpoint(EndpointDefinition bmd) {
            broadcastTypes.add(bmd);
            return this;
        }

        /**
         * @return the broadcastTypes
         */
        public List<EndpointDefinition> getEndpoints() {
            return broadcastTypes;
        }

        /**
         * @return the destinations
         */
        public URL getDestination() {
            return destination;
        }
    }
}
