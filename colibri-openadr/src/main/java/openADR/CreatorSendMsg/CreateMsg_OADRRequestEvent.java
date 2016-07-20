package openADR.CreatorSendMsg;

import com.enernoc.open.oadr2.model.v20b.OadrRequestEvent;
import com.enernoc.open.oadr2.model.v20b.pyld.EiRequestEvent;
import openADR.OADRMsgInfo.MsgInfo_OADRRequestEvent;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

import java.util.HashMap;

/**
 * Created by georg on 07.06.16.
 * This class is used to create oadrRequestEvent messages.
 */
public class CreateMsg_OADRRequestEvent extends CreateSendMsg {

    /**
     * Creates a message object with an openADR payload OadrRequestEvent in it.
     * @param info message info: contains the needed information to create a openADR payload
     * @param receivedMsgMap contains all received messages
     * @return
     */
    @Override
    public OADRMsgObject genSendMsg(OADRMsgInfo info, HashMap<String, OADRMsgInfo> receivedMsgMap) {
        MsgInfo_OADRRequestEvent con_info = (MsgInfo_OADRRequestEvent) info;

        OadrRequestEvent msg = new OadrRequestEvent();
        String reqID = OADRConInfo.getUniqueRequestId();

        msg.setSchemaVersion("2.0b");

        EiRequestEvent eiRequestEvent = new EiRequestEvent();
        eiRequestEvent.setReplyLimit(con_info.getReplyLimit());
        eiRequestEvent.setRequestID(reqID);
        eiRequestEvent.setVenID(OADRConInfo.getVENId());

        msg.setEiRequestEvent(eiRequestEvent);

        OADRMsgObject obj = new OADRMsgObject(info.getMsgType(), reqID, msg);

        return obj;
    }

    /**
     * This method returns the message type name for an oadrRequestEvent message
     * @return supported messege type
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRRequestEvent().getMsgType();
    }

    /**
     * {@inheritDoc}
     */
    public boolean doSendMsgViolateMsgOrderAndUpdateRecMap(OADRMsgInfo info, HashMap<String, OADRMsgInfo> receivedMsgMap){
        if(OADRConInfo.getVENId() == null){
            return true;
        }
        return false;
    }
}
