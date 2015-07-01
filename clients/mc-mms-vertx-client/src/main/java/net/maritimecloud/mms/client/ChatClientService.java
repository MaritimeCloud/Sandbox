package net.maritimecloud.mms.client;

import java.util.function.Consumer;

/**
 * Interface to be implemented by chat services.
 */
public interface ChatClientService<C> {

    /**
     * Adds a new chat client
     * @param id the id of the client
     * @param c optionally, the client
     * @param consumer a conument of incoming chat messages
     */
    void addChatClient(String id, C c, Consumer<ChatMessage> consumer);

    /**
     * Removes an existing chat client
     * @param id the id of the client
     */
    void removeClient(String id);

    /**
     * Returns if this service has the registered client
     * @param id the id of the client
     * @return if this service has the registered client
     */
    boolean hasClient(String id);

    /**
     * Sends the given chat message
     * @param msg the message to send
     */
    void sendChatMessage(ChatMessage msg);
}
