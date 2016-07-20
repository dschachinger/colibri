package openADR.ProcessorReceivedMsg;

import com.enernoc.open.oadr2.model.v20b.ei.EiResponse;
import com.enernoc.open.oadr2.model.v20b.ei.ResponseCode;
import openADR.OADRHandling.OADRParty;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRMsgObject;

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
     * @param responseCode
     * @return proper reply
     */
    public abstract OADRMsgObject genResponse(OADRMsgObject obj, String responseCode);

    /**
     * This method returns an openADR.OADRMsgInfo object. This object contains all needful information for a engery consumer.
     * It also updates the internal data.
     * Should be called before the genResponse() method.
     * @param obj extract inforation out of this message object
     * @param party
     * @return  The openADR.OADRMsgInfo object contains all needful information for a engery consumer.
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

    //-------------------------------static part ------------------------------------------------//
    static HashMap<String, String> respValueText;
    static {
        respValueText = new HashMap<>();
        respValueText.put("200", "OK");
        respValueText.put("450", "Out of sequence");
        respValueText.put("452", "Invalid ID");
    }

    static public EiResponse genEiRespone(String requestID, String respCode){
        EiResponse eiResponse = new EiResponse();
        ResponseCode responseCode = new ResponseCode();
        responseCode.setValue(respCode);
        eiResponse.setResponseCode(responseCode);
        eiResponse.setResponseDescription(respValueText.get(respCode));
        if(requestID != null){
            eiResponse.setRequestID(requestID);
        }
        return eiResponse;
    }

}
