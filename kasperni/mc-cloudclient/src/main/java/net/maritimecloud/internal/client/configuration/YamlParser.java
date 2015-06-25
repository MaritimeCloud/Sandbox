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

import java.net.MalformedURLException;
import java.net.URL;

import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

/**
 *
 * @author Kasper Nielsen
 */
public class YamlParser {

    static SequenceNode sequenceNode(Node n) {
        return (SequenceNode) n;
    }

    static MappingNode mappingNode(Node n) {
        return (MappingNode) n;
    }

    static String readString(Node n) {
        if (!(n instanceof ScalarNode)) {
            throw new IllegalArgumentException("Expect a string " + n.getStartMark() + " but was a " + n.getClass());
        }
        return ((ScalarNode) n).getValue();
    }

    static URL readUrl(Node n) {
        String url = readString(n);
        if (!url.startsWith("file:")) {
            throw new IllegalArgumentException("Only file URLs allowed, must start with 'file:'" + n.getStartMark());
        }
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Only file URLs allowed, must start with 'file:'" + n.getStartMark());
        }
    }
}
