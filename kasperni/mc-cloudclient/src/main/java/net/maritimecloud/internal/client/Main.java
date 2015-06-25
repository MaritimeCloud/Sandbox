package net.maritimecloud.internal.client;


import java.util.concurrent.TimeUnit;

import net.maritimecloud.internal.client.configuration.ClientConfiguration;

/**
 * Used to start a server from the command line.
 *
 * @author Kasper Nielsen
 */
public class Main {

    public static void main(String[] args) throws Exception {
        ClientConfiguration configuration = ClientConfiguration.create(args);

        InternalClient server = new InternalClient(configuration);
        server.startBlocking();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
            try {
                for (int i = 0; i < 30; i++) {
                    if (!server.awaitTerminated(1, TimeUnit.SECONDS)) {
                        System.out.println("Awaiting shutdown " + i + " / 30 seconds");
                    } else {
                        return;
                    }
                }
                System.err.println("Could not shutdown server properly");
            } catch (InterruptedException ignore) {}
        }));

        System.out.println("Wuhuu Maritime Messing Client started!");
        if (configuration.getPort() >= 0) {
            // System.out.println("Client  : Running on port " + configuration.getPort() + " (unsecure)");
        }
        System.out.println("Use CTRL+C to stop it");
    }
}
