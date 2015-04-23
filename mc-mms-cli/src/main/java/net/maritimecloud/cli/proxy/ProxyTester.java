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
package net.maritimecloud.cli.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of an MMS proxy service.
 *
 * Originally from the mc-mms-tck project
 */
@SuppressWarnings("unused")
public class ProxyTester {
    final LinkedList<Connection> connections = new LinkedList<>();

    final SocketAddress proxyAddress;

    final SocketAddress remoteAddress;

    final ExecutorService es = Executors.newSingleThreadExecutor();

    final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

    volatile ServerSocket ss;

    volatile CountDownLatch pause = new CountDownLatch(0);

    public ProxyTester(SocketAddress proxyAddress, SocketAddress remoteAddress) {
        this.proxyAddress = requireNonNull(proxyAddress);
        this.remoteAddress = requireNonNull(remoteAddress);
    }

    public synchronized void pause() {
        if (pause.getCount() == 0) {
            pause = new CountDownLatch(1);
        }
    }

    public void noPause() {
        pause.countDown();
    }

    public void start() throws IOException {
        ss = new ServerSocket();
        ss.bind(proxyAddress);
        System.out.println("Starting proxy");
        es.submit(() -> {
            for (;;) {
                try {
                    pause.await();
                    final Socket in = ss.accept();
                    pause.await();
                    Socket out = new Socket();
                    out.connect(remoteAddress);
                    Connection con = new Connection(in, out);
                    con.inToOut.start();
                    con.outToIn.start();
                    connections.add(con);
                } catch (Throwable t) {
                    return;
                }
            }
        });
    }

    public void killLastConnection() {
        close(connections.pollLast());
    }

    public void killFirstConnection() {
        close(connections.pollFirst());
    }

    public void killRandom() {
        close(connections.remove(ThreadLocalRandom.current().nextInt(connections.size())));
    }

    public Future<?> killRandom(long time, TimeUnit unit) {
        return ses.scheduleWithFixedDelay(this::killRandom, 0, time, unit);
    }

    public void killAll() {
        connections.forEach(this::close);
        connections.clear();
    }

    public void shutdown() throws InterruptedException {
        pause.countDown();
        ses.shutdown();
        es.shutdown();
        try {
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        killAll();
        es.awaitTermination(10, TimeUnit.SECONDS);
    }

    private void close(Connection c) {
        if (c != null) {
            try {
                c.incoming.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                c.outgoing.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class Connection {
        final Socket incoming;

        final Socket outgoing;

        final Thread inToOut;

        final Thread outToIn;

        Connection(Socket incoming, Socket outgoing) {
            this.incoming = requireNonNull(incoming);
            this.outgoing = requireNonNull(outgoing);
            inToOut = new Thread(this::inToOut);
            outToIn = new Thread(this::outToIn);

        }

        void inToOut() {
            for (;;) {
                try {
                    byte[] buffer = new byte[1024]; // Adjust if you want
                    int bytesRead;
                    InputStream is = incoming.getInputStream();
                    OutputStream os = outgoing.getOutputStream();
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                } catch (Throwable t) {
                    return;
                }
            }
        }

        void outToIn() {
            for (;;) {
                try {
                    byte[] buffer = new byte[1024]; // Adjust if you want
                    int bytesRead;
                    InputStream is = outgoing.getInputStream();
                    OutputStream os = incoming.getOutputStream();
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                } catch (Throwable t) {
                    return;
                }
            }
        }
    }
}
