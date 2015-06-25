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
import net.maritimecloud.msdl.model.BroadcastMessageDeclaration;

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
public class BroadcastConfiguration extends YamlParser {

    private static final String STR_DESTINATION = "destination";

    private static final String STR_TYPES = "types";

    private final List<BroadcastGroup> groups = new ArrayList<>();

    /**
     * @return the groups
     */
    public List<BroadcastGroup> getGroups() {
        return groups;
    }

    public BroadcastConfiguration addGroup(BroadcastGroup group) {
        groups.add(requireNonNull(group));
        return this;
    }

    void populate(DefaultMsdlDatabase database, InputStream configuration) {
        Yaml y = new Yaml();

        for (Node n : y.composeAll(new InputStreamReader(configuration))) {
            BroadcastGroup group = new BroadcastGroup();
            MappingNode nn = mappingNode(n);
            for (NodeTuple t : nn.getValue()) {
                ScalarNode key = (ScalarNode) t.getKeyNode();
                if (STR_DESTINATION.equals(key.getValue())) {
                    SequenceNode sn = sequenceNode(t.getValueNode());
                    for (Node no : sn.getValue()) {
                        group.addDestination(readUrl(no));
                    }
                } else if (STR_TYPES.equals(key.getValue())) {
                    SequenceNode sn = sequenceNode(t.getValueNode());
                    for (Node no : sn.getValue()) {
                        String broadcastType = readString(no);
                        BroadcastMessageDeclaration bm = database.getBroadcastMessage(broadcastType);
                        if (bm == null) {
                            throw new IllegalArgumentException("Unknown broadcast type '" + broadcastType + "'"
                                    + no.getStartMark());
                        }
                        group.addType(bm);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown tag '" + key.getValue() + "'" + key.getStartMark());
                }
            }
            groups.add(group);
        }
    }

    public static class BroadcastGroup {
        final List<BroadcastMessageDeclaration> broadcastTypes = new ArrayList<>();

        final List<URL> destinations = new ArrayList<>();

        public BroadcastGroup addDestination(File destination) {
            try {
                return addDestination(destination.toURI().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        public BroadcastGroup addDestination(URL destination) {
            destinations.add(destination);
            return this;
        }

        public BroadcastGroup addType(BroadcastMessageDeclaration bmd) {
            broadcastTypes.add(bmd);
            return this;
        }

        /**
         * @return the broadcastTypes
         */
        public List<BroadcastMessageDeclaration> getBroadcastTypes() {
            return broadcastTypes;
        }

        /**
         * @return the destinations
         */
        public List<URL> getDestinations() {
            return destinations;
        }
    }
}
