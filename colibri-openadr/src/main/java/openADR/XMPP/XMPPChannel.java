package openADR.XMPP;

import openADR.OADRHandling.*;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRMsgObject;
import openADR.Utils.XMPPConInfo;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.iqrequest.IQRequestHandler;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by georg on 28.05.16.
 * Objects from this class can handle an openADR.XMPP openADR message exchange
 */
public class XMPPChannel extends Channel {
    // This object is from smack and is responsible to convey messages to the opposite openADR.XMPP party.
    private AbstractXMPPConnection con;

    private Logger logger = LoggerFactory.getLogger(XMPPChannel.class);

    public XMPPChannel(JAXBManager jaxbManager, OADRParty party) throws Exception{
        super(new Controller(party), jaxbManager, party);

        SmackConfiguration.DEBUG = true;

        XMPPExtensionProvider xmppExtensionProvider = new XMPPExtensionProvider(jaxbManager);

        // Create the configuration for this new connection
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setUsernameAndPassword(XMPPConInfo.VENUsername, XMPPConInfo.VENPassword);
        configBuilder.setResource(XMPPConInfo.VENRessourceeName);
        configBuilder.setServiceName(XMPPConInfo.VENServiceName);

        con = new XMPPTCPConnection(configBuilder.build());

        ServiceDiscoveryManager serviceDiscoveryManager = ServiceDiscoveryManager.getInstanceFor(con);
        serviceDiscoveryManager.addFeature(XMLNS.OADR2.getNamespaceURI());

        for(String msgTypes : controller.getSupportedReceivedMsgTypes()){
            logger.info("supported received message types: " + msgTypes);
            ProviderManager.addIQProvider(msgTypes, XMLNS.OADR2.getNamespaceURI(), xmppExtensionProvider);
            con.registerIQRequestHandler(
                    new OADRIQRequestHandler(this, XMLNS.OADR2.getPrefix()+":"+msgTypes, XMLNS.OADR2.getNamespaceURI(),
                            IQ.Type.set, IQRequestHandler.Mode.sync, jaxbManager, party));
        }


        con.connect();
        con.login();

        con.setFromMode(XMPPConnection.FromMode.USER);

        // Create a new presence. Pass in false to indicate we're unavailable._
        Presence presence = new Presence(Presence.Type.available);
        presence.setStatus("Gone fishing");
        // Send the packet (assume we have an XMPPConnection instance called "con").
        con.sendStanza(presence);

    }

    /**
     * This method should be called to handle a received message
     * @param obj received openADR message
     * @return the reply to the reveived message
     */
    public OADRMsgObject processPacket(OADRMsgObject obj){
        OADRMsgObject responseObj = controller.processReceivedMessage(obj);
        return responseObj;

    }

    /**
     * This method is called to send a message over openADR.XMPP to the opposite party.
     * The message is generated out of the information of the given openADR.OADRMsgInfo object.
     * @param sendInfo This object contains information which type of message should be transmitted and
     *                 how the message should look like.
     */
    public void sendMsg(OADRMsgInfo sendInfo){


        OADRMsgObject sendObj = controller.createSendMsg(sendInfo);
        sendMsgObj(sendObj);
    }

    /**
     * This method is called to send a message over openADR.XMPP to the opposite party.
     * The message is generated out of the information of the given openADR.OADRMsgInfo object.
     * @param sendObj This object contains information which type of message should be transmitted and
     *                 how the message should look like.
     */
    public void sendMsgObj(OADRMsgObject sendObj){
        if(sendObj.getID() != null){
            sendedMsgMap.put(sendObj.getID(), sendObj);
            timeoutWatcher.addMonitoredMsg(sendObj.getID());
        }

        OADR2IQ oadrIQ = new OADR2IQ(sendObj.getMsgType(), XMLNS.OADR2.getNamespaceURI());
        oadrIQ.setTo(XMPPConInfo.getVTNFullAdrName());
        oadrIQ.init(new OADR2PacketExtension(sendObj.getMsg(), jaxbManager));
        try {
            con.sendStanza(oadrIQ);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method closes the xmpp channel by setting the openADR.XMPP-Status to unavailable and also
     * inform smack to close the connection.
     */
    @Override
    public void close(){
        logger.info("close XMPP connection");

        Presence presence = new Presence(Presence.Type.unavailable);
        presence.setStatus("Gone eating");
        try {
            con.disconnect(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }
}