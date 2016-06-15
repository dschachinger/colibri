package ProcessorReceivedMsg;

import OADRMsgInfo.OADRMsgInfo;
import Utils.FollowUpMsg;
import Utils.OADRMsgObject;
import com.enernoc.open.oadr2.model.v20b.ei.EiResponse;
import com.enernoc.open.oadr2.model.v20b.ei.ResponseCode;

/**
 * Created by georg on 06.06.16.
 * Descendant Classes from this abstract class are used to handle a specific received openADR message
 */
public abstract class ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a specific openADR message.
     * @param obj generate reply for this message
     * @return proper reply
     */
    public abstract OADRMsgObject genResponse(OADRMsgObject obj);

    /**
     * This method returns an OADRMsgInfo object. This object contains all needful information for a engery consumer.
     * @param obj extract inforation out of this message object
     * @return  The OADRMsgInfo object contains all needful information for a engery consumer.
     */
    public abstract OADRMsgInfo extractInfo(OADRMsgObject obj);

    /**
     * This method returns which received message type the class supports.
     * @return supported messege type
     */
    public abstract String getMsgType();

    protected EiResponse genEiRespone(String requestID){
        EiResponse eiResponse = new EiResponse();
        ResponseCode responseCode = new ResponseCode();
        responseCode.setValue("200");
        eiResponse.setResponseCode(responseCode);
        eiResponse.setResponseDescription("OK");
        if(requestID != null){
            eiResponse.setRequestID(requestID);
        }
        return eiResponse;
    }

}
