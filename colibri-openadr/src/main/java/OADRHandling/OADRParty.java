package OADRHandling;

import Utils.FollowUpMsg;
import XMPP.XMPPChannel;

import javax.xml.bind.JAXBException;

/**
 * Created by georg on 09.06.16.
 * This class represents one party of the openADR standard.
 * It can be either a VTN (=server) or a VEN (=client).
 */
public abstract class OADRParty {
    // This manager transforms XML messages into java objects
    protected JAXBManager jaxbManager;
    // This object defines the used channel to communicate with the opposite party
    protected Channel channel;

    public OADRParty() {
        try {
            jaxbManager = new JAXBManager();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        channel = new XMPPChannel(jaxbManager, this);
    }

    /**
     * This method is called if a follow-up message is needed.
     * It handles the VTN communication by its own.
     * @param followUpMsg contains information which message should be transmitted and how the message should look like.
     */
    public abstract void handleFollowUpMsg(FollowUpMsg followUpMsg);

    /**
     * This method terminates the party.
     * It is not guaranteed that this party still works afterwards.
     * It is only allowed to call this method if the party was successfully started beforehand.
     */
    public abstract void terminate();

    public Channel getChannel(){
        return channel;
    }
}
