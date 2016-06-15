package CreatorSendMsg;

import OADRMsgInfo.*;
import OADRMsgInfo.OADRMsgInfo;
import Utils.OADRConInfo;
import Utils.OADRMsgObject;
import Utils.XMPPConInfo;
import com.enernoc.open.oadr2.model.v20b.OadrCreatePartyRegistration;
import com.enernoc.open.oadr2.model.v20b.OadrQueryRegistration;
import com.enernoc.open.oadr2.model.v20b.OadrTransportType;

import java.util.HashMap;

/**
 * Created by georg on 07.06.16.
 * This class is used to create oadrCreatePartyRegistration messages.
 */
public class CreateMsg_OADRCreatePartyRegistration extends CreateSendMsg {

    /**
     * Creates a message object with an openADR payload OadrCreatePartyRegistration in it.
     * @param info message info: contains the needed information to create a openADR payload
     * @param receivedMsgMap contains all received messages
     * @return
     */
    @Override
    public OADRMsgObject genSendMsg(OADRMsgInfo info, HashMap<String, OADRMsgInfo> receivedMsgMap) {
        MsgInfo_OADRCreatePartyRegistration con_info = (MsgInfo_OADRCreatePartyRegistration) info;

        OadrCreatePartyRegistration msg = new OadrCreatePartyRegistration();

        String reqID = OADRConInfo.getUniqueRequestId();
        msg.setRequestID(reqID);
        if(OADRConInfo.getVENId() != null){
            msg.setVenID(OADRConInfo.getVENId());
        }
        msg.setOadrProfileName("2.0b");
        msg.setOadrTransportName(OadrTransportType.XMPP);
        msg.setOadrTransportAddress(XMPPConInfo.getVENFullAdrName());
        msg.setOadrReportOnly(false);
        msg.setOadrXmlSignature(false);
        msg.setOadrVenName(OADRConInfo.getVENName());
        if(OADRConInfo.getRegistrationId() != null){
            msg.setRegistrationID(OADRConInfo.getRegistrationId());
        }

        OADRMsgObject obj = new OADRMsgObject(info.getMsgType(), reqID, msg);

        return obj;
    }

    /**
     * This method returns the message type name for an oadrCreatePartyRegistration message
     * @return supported messege type
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRCreatePartyRegistration().getMsgType();
    }
}
