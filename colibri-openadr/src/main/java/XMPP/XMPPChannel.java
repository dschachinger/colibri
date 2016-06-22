package XMPP;

import OADRHandling.*;
import OADRMsgInfo.OADRMsgInfo;
import Utils.OADRMsgObject;
import Utils.XMPPConInfo;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.iqrequest.IQRequestHandler;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;

import java.io.IOException;

/**
 * Created by georg on 28.05.16.
 * Objects from this class can handle an XMPP openADR message exchange
 */
public class XMPPChannel extends Channel {
    // This object is from smack and is responsible to convey messages to the opposite XMPP party.
    private AbstractXMPPConnection con;

    public XMPPChannel(JAXBManager jaxbManager, OADRParty party){
        super(new Controller(party), jaxbManager, party);

        SmackConfiguration.DEBUG = true;

        XMPPExtensionProvider xmppExtensionProvider = new XMPPExtensionProvider(jaxbManager);

        // Create the configuration for this new connection
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setUsernameAndPassword(XMPPConInfo.VENUsername, XMPPConInfo.VENPassword);
        configBuilder.setResource(XMPPConInfo.VENRessourceeName);
        configBuilder.setServiceName(XMPPConInfo.VENServiceName);

        con = new XMPPTCPConnection(configBuilder.build());
        // without XMPPTCPConnectionConfiguration --> con = new XMPPTCPConnection("birne_vie@jabber.de", "HALLO90");

        // TODO specification side 58
        ServiceDiscoveryManager serviceDiscoveryManager = ServiceDiscoveryManager.getInstanceFor(con);
        serviceDiscoveryManager.addFeature(XMLNS.OADR2.getNamespaceURI());

        for(String msgTypes : controller.getSupportedReceivedMsgTypes()){
            System.out.println("supported types: " + msgTypes + " namespace: " + XMLNS.OADR2.getNamespaceURI() + " long " + XMLNS.OADR2.getPrefix()+":"+msgTypes);
            ProviderManager.addIQProvider(msgTypes, XMLNS.OADR2.getNamespaceURI(), xmppExtensionProvider);
            con.registerIQRequestHandler(
                    new OADRIQRequestHandler(this, XMLNS.OADR2.getPrefix()+":"+msgTypes, XMLNS.OADR2.getNamespaceURI(),
                            IQ.Type.set, IQRequestHandler.Mode.sync, jaxbManager, party));
        }

        try {
            con.connect();
            con.login();

            con.setFromMode(XMPPConnection.FromMode.USER);

            // Create a new presence. Pass in false to indicate we're unavailable._
            Presence presence = new Presence(Presence.Type.available);
            presence.setStatus("Gone fishing");
            // Send the packet (assume we have an XMPPConnection instance called "con").
            con.sendStanza(presence);

        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
     * This method is called to send a message over XMPP to the opposite party.
     * The message is generated out of the information of the given OADRMsgInfo object.
     * @param sendInfo This object contains information which type of message should be transmitted and
     *                 how the message should look like.
     */
    public void sendMsg(OADRMsgInfo sendInfo){
        OADR2IQ oadrIQ = new OADR2IQ(sendInfo.getMsgType(), XMLNS.OADR2.getNamespaceURI());
        oadrIQ.setTo(XMPPConInfo.getVTNFullAdrName());
        System.out.println("new iq id: " + oadrIQ.getStanzaId());
        OADRMsgObject sendObj = controller.createSendMsg(sendInfo);
        oadrIQ.init(new OADR2PacketExtension(sendObj.getMsg(), jaxbManager));

        try {
            con.sendStanza(oadrIQ);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method closes the xmpp channel by setting the XMPP-Status to unavailable and also
     * inform smack to close the connection.
     */
    @Override
    public void close(){
        System.out.println("close XMPP connection");

        Presence presence = new Presence(Presence.Type.unavailable);
        presence.setStatus("Gone eating");
        try {
            con.disconnect(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }
}