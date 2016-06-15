package CreatorSendMsg;

import OADRMsgInfo.*;
import Utils.OADRConInfo;
import Utils.OADRMsgObject;
import com.enernoc.open.oadr2.model.v20b.OadrQueryRegistration;

import java.util.HashMap;

/**
 * Created by georg on 07.06.16.
 * This class is used to create oadrQueryRegistration messages.
 */
public class CreateMsg_OADRQueryRegistration extends CreateSendMsg {

    /**
     * Creates a message object with an openADR payload OadrQueryRegistration in it.
     * @param info message info: contains the needed information to create a openADR payload
     * @param receivedMsgMap contains all received messages
     * @return
     */
    @Override
    public OADRMsgObject genSendMsg(OADRMsgInfo info, HashMap<String, OADRMsgInfo> receivedMsgMap) {
        MsgInfo_OADRQueryRegistration con_info = (MsgInfo_OADRQueryRegistration) info;

        OadrQueryRegistration msg = new OadrQueryRegistration();
        msg.setSchemaVersion("2.0b");
        String reqID = OADRConInfo.getUniqueRequestId();
        msg.setRequestID(reqID);

        OADRMsgObject obj = new OADRMsgObject(info.getMsgType(), reqID, msg);

        return obj;
    }

    /**
     * This method returns the message type name for an oadrQueryRegistration message
     * @return supported messege type
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRQueryRegistration().getMsgType();
    }
}
