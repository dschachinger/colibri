package openADR.XMPP;

import openADR.OADRHandling.JAXBManager;
import openADR.OADRHandling.OADRParty;
import openADR.Utils.OADRMsgObject;
import openADR.Utils.XMPPConInfo;
import org.jivesoftware.smack.iqrequest.IQRequestHandler;
import org.jivesoftware.smack.packet.IQ;

/**
 * Created by georg on 07.06.16.
 * Objects from this class are used to take a specific received message type
 */
public class OADRIQRequestHandler implements IQRequestHandler {
    // This variable signalize the supported message type or in other words the root element of the openADR message
    private String elemName;
    // This variable signalize the supported message namespace
    private String namespace;
    // This variable signalize the supported iq type
    private IQ.Type type;
    // This variable signalize the incoming messages are processed. asynchronously or synchronously
    private Mode mode;
    // Defines for which channel this OADRIQRequestHandler object is used
    private XMPPChannel channel;
    // This manager transfoms XML messages into java objects
    protected JAXBManager jaxbManager;
    // Defines which party the channel uses
    protected OADRParty party;

    public OADRIQRequestHandler(XMPPChannel channel, String elemName, String namespace,
                                IQ.Type type, Mode mode, JAXBManager jaxbManager,
                                OADRParty party) {
        this.elemName = elemName;
        this.namespace = namespace;
        this.type = type;
        this.mode = mode;
        this.channel = channel;
        this.jaxbManager = jaxbManager;
        this.party = party;
    }

    /**
     * This method is called by smack if a received message matches the supported criteria.
     * Smack will send the returned IQ message back to opposite party.
     * @param iqRequest reveived IQ
     * @return which IQ will replied
     */
    @Override
    public IQ handleIQRequest(IQ iqRequest) {
        OADR2IQ receivedIQ = (OADR2IQ)iqRequest;
        OADRMsgObject obj = new OADRMsgObject(receivedIQ.getExtension().getElementNameWithoutNamespace(),
                null, receivedIQ.getOADRPayload());

        OADRMsgObject responseObj = channel.processPacket(obj);

        if(responseObj != null) {
            IQ responseIQ;
            if(responseObj.getMsgType().equals("emptyStanze")){
                responseIQ = IQ.createResultIQ(iqRequest);
            } else {
                responseIQ = new OADR2IQ(new OADR2PacketExtension(responseObj.getMsg(), jaxbManager));
                responseIQ.setTo(XMPPConInfo.getVTNFullAdrName());
            }

            return responseIQ;
        } else {
            return null;
        }
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public IQ.Type getType() {
        return type;
    }

    @Override
    public String getElement() {
        return elemName;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }
}
