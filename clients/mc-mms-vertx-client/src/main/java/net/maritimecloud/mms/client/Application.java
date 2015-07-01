package net.maritimecloud.mms.client;

import java.io.IOException;

/**
 * Starts up the application
 */
public class Application {

    /**
     * Starts the application
     * @param args command line arguments
     */
    public static void main(String[] args) throws IOException {

        // Check if we should use a non-default MMS server
        if (args.length > 0) {
            MmsChatClientService.MMS_HOST = args[1];
        }

        MmsChatClientService mmsChatClientService = new MmsChatClientService();
        VertxChatClientService vertxChatClientService = new VertxChatClientService(mmsChatClientService);

        // Prevent the JVM from exiting
        System.in.read();
    }


}
