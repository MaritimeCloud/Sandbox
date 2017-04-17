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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;


/**
 *
 * @author Kasper Nielsen
 */
public abstract class Destination {

    public static Destination create(DestinationManager dm, String destination) throws URISyntaxException {
        final URI uri = new URI(destination);
        if (uri.getScheme().equals("file")) {
            if (uri.getHost() != null) {
                throw new URISyntaxException(destination, "Only localhost file urls are supported, file:///directory");
            }
            Path p;
            if (uri.getPath() == null) {
                Path home = dm.fileManager.home;
                p = home.resolve(uri.getSchemeSpecificPart());
            } else {
                p = Paths.get(uri.getPath());
            }
            if (!Files.exists(p)) {
                throw new URISyntaxException(destination, "The directory " + p + " does not exist");
            } else if (!Files.isDirectory(p)) {
                throw new URISyntaxException(destination, "The path " + p + " is not a directory");
            } else if (!Files.isWritable(p)) {
                throw new URISyntaxException(destination, "The directory " + p + " is not writeable");
            }
            return new FileDestination(p);
        }
        throw new UnsupportedOperationException("The scheme '" + uri.getScheme() + "' is not supported");
    }

    public abstract void send(String filename, String msg);

    // file write xxx.request, wait yyy.request
    public abstract CompletableFuture<String> sendRequest(String filename, String msg);

    public static class FileDestination extends Destination {
        final Path directory;

        FileDestination(Path directory) {
            this.directory = requireNonNull(directory);
        }

        public void send(String filename, String msg) {
            Path p = directory.resolve(filename);
            // We write a temporary file first, to make the file is completely written before the user picks it up
            try {
                Path tmp = Files.createTempFile("ms_client", ".tmp");
                Files.write(tmp, msg.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);

                try {
                    System.err.println(tmp);
                    Files.move(tmp, p);
                } catch (FileAlreadyExistsException ignore) {
                    Files.delete(tmp);
                }
            } catch (IOException e) {
                // TODO log it
                e.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public CompletableFuture<String> sendRequest(String filename, String msg) {
            // Write ...request

            return null;
        }
    }
}
