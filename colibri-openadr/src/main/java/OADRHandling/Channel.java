package OADRHandling;

import OADRMsgInfo.OADRMsgInfo;
import Utils.OADRMsgObject;
import org.jivesoftware.smack.packet.IQ;

/**
 * Created by georg on 28.05.16.
 * Descendant classes from this abstract class defines channels for the openADR message exchange.
 */
public abstract class Channel {
    // The controller handles the openADR messages
    protected Controller controller;
    // This manager transforms XML messages into java objects
    protected JAXBManager jaxbManager;
    // Defines which party the channel uses
    protected OADRParty party;

    public Channel(Controller controller, JAXBManager jaxbManager, OADRParty party){
        this.controller = controller;
        this.jaxbManager = jaxbManager;
        this.party = party;
    }

    /**
     * This method sends an openADR message with the given infos
     * @param sendInfo This object contains information how the message should look like.
     */
    public abstract void sendMsg(OADRMsgInfo sendInfo);

    /**
     * This method is called to process a received openADR message.
     * The return value contains the reply openADR message and a optional follow-up message.
     * @param obj received openADR message
     * @return how to react on this message.
     */
    public abstract OADRMsgObject processPacket(OADRMsgObject obj);

    // only if before a connection had been established

    /**
     * This method closes the channel.
     * It is not guaranteed that this channel still works afterwards.
     * It is only allowed to call this method if the channel was successfully opened beforehand.
     */
    public abstract void close();
}