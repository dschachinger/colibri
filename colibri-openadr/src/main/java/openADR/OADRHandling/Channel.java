package openADR.OADRHandling;

import Utils.TimeoutWatcher;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRMsgObject;

import java.util.HashMap;

/**
 * Created by georg on 28.05.16.
 * Descendant classes from this abstract class define channels for the openADR message exchange.
 */
public abstract class Channel {
    // The controller handles the openADR messages
    protected Controller controller;
    // This manager transforms XML messages into java objects
    protected JAXBManager jaxbManager;
    // Defines which party the channel uses
    protected OADRParty party;

    /* This hash map holds the sent messages and
    the key is the openADR requestID */
    protected HashMap<String, OADRMsgObject> sentMsgMap;

    // this object monitors the reply time
    protected TimeoutWatcher<Channel, OADRMsgObject> timeoutWatcher;

    public Channel(Controller controller, JAXBManager jaxbManager, OADRParty party, int timeoutSec){
        this.controller = controller;
        this.jaxbManager = jaxbManager;
        this.party = party;

        // init sentMsgMap
        sentMsgMap = new HashMap<>();

        timeoutWatcher = TimeoutWatcher.initOpenADRTimeoutWatcher(timeoutSec*1000, this);
    }

    public HashMap<String, OADRMsgObject> getSentMsgMap() {
        return sentMsgMap;
    }

    public Controller getController() {
        return controller;
    }

    /**
     * This method sends an openADR message with the given infos
     * @param sendInfo This object contains information how the message should look like.
     */
    public abstract void sendMsg(OADRMsgInfo sendInfo);

    /**
     * This method sends an openADR message object with the given infos
     * @param sendObj This object contains information how the message should look like.
     */
    public abstract void sendMsgObj(OADRMsgObject sendObj);

    /**
     * This method is called to process a received openADR message.
     * The return value contains the reply openADR message and a optional follow-up message.
     * @param obj received openADR message
     * @return how to react on this message.
     */
    public abstract OADRMsgObject processPacket(OADRMsgObject obj);

    /**
     * This method closes the channel.
     * It is not guaranteed that this channel still works afterwards.
     * It is only allowed to call this method if the channel was successfully opened beforehand.
     */
    public abstract void close();
}