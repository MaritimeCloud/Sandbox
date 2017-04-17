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
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import net.maritimecloud.internal.client.configuration.ClientConfiguration;
import net.maritimecloud.internal.util.logging.Logger;

import org.cakeframework.container.lifecycle.RunOnStop;

import com.sun.nio.file.SensitivityWatchEventModifier;

/**
 *
 * @author Kasper Nielsen
 */
public class FileManager {

    /** The logger. */
    private static final Logger LOG = Logger.get(FileManager.class);

    final ConcurrentHashMap<Path, Entry> map = new ConcurrentHashMap<>();

    final ConcurrentHashMap<WatchKey, Entry> keys = new ConcurrentHashMap<>();

    private volatile WatchService ws;

    final Path home;

    /**
     * @return the home
     */
    public Path getHome() {
        return home;
    }

    public static final String FILEIO = "fileio";

    public static final String FILEIO_FAIL = FILEIO + "/fail";

    public static final String FILEIO_IN_BROADCAST = FILEIO + "/in/broadcast";

    public static final String FILEIO_OUT_BROADCAST = FILEIO + "/out/broadcast";

    public static final String FILEIO_OUT_ENDPOINT = FILEIO + "/out/service";

    public static final String FILEIO_IN_ENDPOINT = FILEIO + "/in/service";

    public FileManager(ClientConfiguration conf) {
        home = conf.getHome();
    }

    void processEvents() {
        for (;;) {
            WatchKey wc = null;
            try {
                System.out.println("WAITING FOR NEXT");
                wc = ws.take();
                System.out.println("WOKE UP");
            } catch (InterruptedException ignore) {} catch (ClosedWatchServiceException e) {
                LOG.info("FileManager exited");
                return;
            }
            if (wc != null) {
                Entry en = keys.get(wc);
                if (en != null) {
                    for (WatchEvent<?> w : wc.pollEvents()) {
                        Path p = (Path) w.context();
                        Path dir = en.directory.resolve(p).toAbsolutePath();
                        System.out.println("XXXXXX " + dir);

                        for (Consumer<Path> c : en) {
                            try {
                                c.accept(dir);
                            } catch (RuntimeException ex) {
                                LOG.error("internal error", ex);
                            }
                        }
                    }
                }
                wc.reset();
            } else {
                LOG.error("No handlers found for watchkey");
            }
        }
    }

    @RunOnStop
    public void stop() throws IOException {
        WatchService ws = this.ws;
        if (ws != null) {
            ws.close();
        }
    }

    public void subscribe(Path directory, Consumer<Path> consumer) {
        WatchService ws = ws();
        Entry e = map.computeIfAbsent(
                directory,
                p -> {
                    LOG.info("Watching " + directory);
                    try {
                        WatchKey k = directory.register(ws,
                                new WatchEvent.Kind[] { StandardWatchEventKinds.ENTRY_CREATE },
                                SensitivityWatchEventModifier.HIGH);
                        Entry en = new Entry(k, directory);
                        keys.put(k, en);
                        return en;
                    } catch (IOException e1) {
                        throw new RuntimeException(e1);
                    }
                });
        e.add(consumer);
    }

    WatchService ws() {
        WatchService ws = this.ws;
        if (ws == null) {
            synchronized (this) {
                try {
                    ws = FileSystems.getDefault().newWatchService();
                    Thread t = new Thread(() -> processEvents(), "File Watcher thread");
                    t.setDaemon(true);
                    t.start();
                    this.ws = ws;
                } catch (IOException e) {
                    // This should never happen as we are using the default provider
                    throw new RuntimeException("Could not create watch service", e);
                }
            }
        }
        return ws;
    }

    @SuppressWarnings("serial")
    static class Entry extends CopyOnWriteArrayList<Consumer<Path>> {
        final WatchKey key;

        final Path directory;

        Entry(WatchKey key, Path directory) {
            this.key = requireNonNull(key);
            this.directory = directory;
        }
    }
}
