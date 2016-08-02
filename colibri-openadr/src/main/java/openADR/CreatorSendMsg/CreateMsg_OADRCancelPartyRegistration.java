package openADR.CreatorSendMsg;

import com.enernoc.open.oadr2.model.v20b.OadrCancelPartyRegistration;
import openADR.OADRMsgInfo.MsgInfo_OADRCancelPartyRegistration;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

/**
 * Created by georg on 07.06.16.
 * This class is used to create oadrCancelPartyRegistration messages.
 */
public class CreateMsg_OADRCancelPartyRegistration extends CreateSendMsg {

    /**
     * Creates a message object with an openADR payload OadrCancelPartyRegistration in it.
     * @param info message info: contains the needed information to create a openADR payload
     * @return
     */
    @Override
    public OADRMsgObject genSendMsg(OADRMsgInfo info) {
        MsgInfo_OADRCancelPartyRegistration con_info = (MsgInfo_OADRCancelPartyRegistration) info;

        OadrCancelPartyRegistration msg = new OadrCancelPartyRegistration();
        String reqID = OADRConInfo.getUniqueRequestId();
        msg.setRequestID(reqID);

        msg.setSchemaVersion("2.0b");
        msg.setVenID(OADRConInfo.getVENId());
        msg.setRegistrationID(OADRConInfo.getRegistrationId());

        OADRMsgObject obj = new OADRMsgObject(info.getMsgType(), reqID, msg);

        return obj;
    }

    /**
     * This method returns the message type name for an oadrCancelPartyRegistration message
     * @return supported messege type
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRCancelPartyRegistration().getMsgType();
    }

    /**
     * {@inheritDoc}
     */
    public boolean doSendMsgViolateMsgOrder(OADRMsgInfo info){
        return checkConstraints(info, true);
    }
}
