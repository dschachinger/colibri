package semanticCore.del;

/**
 * Default Interface for a communication channel
 */
public interface Channel {

    /**
     * This method sends the given message.
     * @param msg Message
     */
    void sendMessage(byte[] msg);

    /**
     * This method returns a received message.
     * @return
     */
    byte[] receiveMessage();

    /**
     * This method closes the channel. Afterwards messages can be neither received nor sent.
     */
    void close();
}
