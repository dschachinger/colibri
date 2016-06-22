package ProcessorReceivedMsg;

import OADRHandling.OADRParty;
import OADRMsgInfo.OADRMsgInfo;
import Utils.OADRMsgObject;
import com.enernoc.open.oadr2.model.v20b.ei.EiResponse;
import com.enernoc.open.oadr2.model.v20b.ei.ResponseCode;

import java.util.HashMap;

/**
 * Created by georg on 06.06.16.
 * Descendant Classes from this abstract class are used to handle a specific received openADR message
 */
public abstract class ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a specific openADR message.
     * Should be called after the extractInfo() method because extractInfo updates the internal data.
     * @param obj generate reply for this message
     * @return proper reply
     */
    public abstract OADRMsgObject genResponse(OADRMsgObject obj);

    /**
     * This method returns an OADRMsgInfo object. This object contains all needful information for a engery consumer.
     * It also updates the internal data.
     * Should be called before the genResponse() method.
     * @param obj extract inforation out of this message object
     * @param party
     * @return  The OADRMsgInfo object contains all needful information for a engery consumer.
     */
    public abstract OADRMsgInfo extractInfo(OADRMsgObject obj, OADRParty party);

    /**
     * This method returns which received message type the class supports.
     * @return supported messege type
     */
    public abstract String getMsgType();

    /**
     * This method returns if a given received message object violates openADR constraints.
     * This means if you receive such a message and the method returns true
     * than the opposit party does not comply the openADR rules.
     * @param obj given received message object
     * @param sendedMsgMap contains all sended Messages which have not received a reply
     * @return true...It violates constraints, false...It does not violate constraints.
     */
    public abstract boolean doRecMsgViolateConstraintsAndUpdateSendMap(OADRMsgObject obj, HashMap<String, OADRMsgObject> sendedMsgMap);

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
