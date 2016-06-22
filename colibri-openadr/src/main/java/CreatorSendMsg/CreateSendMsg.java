package CreatorSendMsg;

import OADRMsgInfo.OADRMsgInfo;
import Utils.OADRConInfo;
import Utils.OADRMsgObject;
import com.enernoc.open.oadr2.model.v20b.ei.EiResponse;
import com.enernoc.open.oadr2.model.v20b.ei.ResponseCode;

import java.util.HashMap;

/**
 * Created by georg on 06.06.16.
 * Descendant Classes from this abstract class are used to transmit a specific openADR message
 */
public abstract class CreateSendMsg {
    /**
     * This method generates a openADR message object out of a message info.
     * The return value contains the payload of the openADR message,
     * but no information about layers underneath the openADR layer.
     * A example layer below the openADR layer is XMPP or HTTP.
     *
     * @param info message info: contains the needed information to create a openADR payload
     * @param receivedMsgMap contains all received messages
     * @return openADR message object
     */
    abstract public OADRMsgObject genSendMsg(OADRMsgInfo info, HashMap<String, OADRMsgInfo> receivedMsgMap);

    /**
     * This method returns which message type the class supports.
     * @return supported messege type
     */
    abstract public String getMsgType();


    /**
     * This method returns if a given send message info object violates openADR constraints
     * when it will be sended.
     * @param info given send message info object
     * @param receivedMsgMap contains all received Messages which are not confirmed with a reply
     * @return true...It violates constraints, false...It does not violate constraints.
     */
    public abstract boolean doSendMsgViolateMsgOrderAndUpdateRecMap(OADRMsgInfo info, HashMap<String, OADRMsgInfo> receivedMsgMap);

    /**
     * This method returns an everything is fine EIResponse Object which contains the
     * given requestID.
     *
     * @param requestID If the parameter requestID is empty then the regarding element
     * will not be initialized.
     * @return an EIResponse Object which is normally used in a reply message
     */
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
