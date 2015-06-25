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
package net.maritimecloud.internal.client.destination;

import static java.util.Objects.requireNonNull;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.ws.rs.BadRequestException;

/**
 *
 * @author Kasper Nielsen
 */
public class DestinationManager {

    final FileManager fileManager;

    public DestinationManager(FileManager fileManager) {
        this.fileManager = requireNonNull(fileManager);
    }

    public Destination createRest(String destination) {
        try {
            return create(destination);
        } catch (URISyntaxException e1) {
            throw new BadRequestException(e1.getMessage(), e1);
        }
    }


    public Destination createRest(String destination, Consumer<String> result) {
        return null;
    }

    public Destination create(Path p) {
        return new Destination.FileDestination(p);
    }

    public Destination create(String destination) throws URISyntaxException {
        return Destination.create(this, destination);
    }

}
