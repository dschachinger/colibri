package openADR.CreatorSendMsg;

import com.enernoc.open.oadr2.model.v20b.OadrQueryRegistration;
import openADR.OADRMsgInfo.MsgInfo_OADRQueryRegistration;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

/**
 * Created by georg on 07.06.16.
 * This class is used to create oadrQueryRegistration messages.
 */
public class CreateMsg_OADRQueryRegistration extends CreateSendMsg {

    /**
     * Creates a message object with an openADR payload OadrQueryRegistration in it.
     * @param info message info: contains the needed information to create an openADR payload
     * @return
     */
    @Override
    public OADRMsgObject genSendMsg(OADRMsgInfo info) {
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
     * @return supported message type
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRQueryRegistration().getMsgType();
    }

    /**
     * {@inheritDoc}
     */
    public boolean doSendMsgViolateMsgOrder(OADRMsgInfo info){
        return checkConstraints(info,false);
    }
}
