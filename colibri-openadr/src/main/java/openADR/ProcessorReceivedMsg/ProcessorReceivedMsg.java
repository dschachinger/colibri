package openADR.ProcessorReceivedMsg;

import com.enernoc.open.oadr2.model.v20b.OadrCanceledPartyRegistration;
import com.enernoc.open.oadr2.model.v20b.ei.EiResponse;
import com.enernoc.open.oadr2.model.v20b.ei.ResponseCode;
import openADR.OADRHandling.OADRParty;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
     * This method returns an openADR.OADRMsgInfo object. This object contains all needful information for an engery consumer.
     * It also updates the internal data.
     * Should be called before the genResponse() method.
     * @param obj extract inforation out of this message object
     * @param party
     * @return  The openADR.OADRMsgInfo object contains all needful information for an engery consumer.
     */
    public abstract OADRMsgInfo extractInfo(OADRMsgObject obj, OADRParty party);

    /**
     * This method returns which received message type the class supports.
     * @return supported message type
     */
    public abstract String getMsgType();

    /**
     * This method returns if a given received message object violates openADR constraints.
     * This means if you receive such a message and the method returns true
     * than the opposit party does not comply the openADR rules.
     * @param obj given received message object
     * @param sentMsgMap contains all sent Messages which have not received a reply
     * @return true...It violates constraints, false...It does not violate constraints.
     */
    public abstract String doRecMsgViolateConstraints(OADRMsgObject obj, HashMap<String, OADRMsgObject> sentMsgMap);

    /**
     * see second checkConstraintsExtendedOriginMsgTypes method.
     * The only difference is that only one originMsgType instead of multiple types is checked.
     */
    protected String checkConstraints(HashMap<String, OADRMsgObject> sentMsgMap, boolean checkIfRegistered,
                                      String requestID, String originMsgType, String venID,
                                      String registrationID){
        List<String> originMsgTypes = new ArrayList<>();
        originMsgTypes.add(originMsgType);

        return checkConstraintsExtendedOriginMsgTypes(sentMsgMap, checkIfRegistered,requestID,originMsgTypes,venID,registrationID);
    }

    /**
     * This method copes with all similar constraints checks for the received messages.
     * The parameters specify the needed constraints.
     * @param sentMsgMap contains all sent messages which are not acknowledged yet
     * @param checkIfRegistered true...check if the connector is registered, false..otherwise
     * @param requestID check if there is a sent message which has the given message id, null means do not check this constraint
     * @param originMsgTypes this parameter depends in the requestID parameter. If not null it checks if the sent message has this given type
     * @param venID check if this ven id matches with the registered one
     * @param registrationID check if this registration id matches with the registered one
     * @return true...constraints are violated, otherwise...everything is fine
     */
    protected String checkConstraintsExtendedOriginMsgTypes(HashMap<String, OADRMsgObject> sentMsgMap, boolean checkIfRegistered,
                                      String requestID, List<String> originMsgTypes, String venID,
                                      String registrationID){
        if(checkIfRegistered && OADRConInfo.getVENId() == null){
            return "450";
        }

        if(requestID != null){
            if(sentMsgMap.get(requestID) == null) {
                return "452";
            }

            OADRMsgObject originMsg = sentMsgMap.get(requestID);
            boolean properType = false;
            for (String originMsgType : originMsgTypes){
                if(originMsgType.equals(originMsg.getMsgType())){
                    properType=true;
                    break;
                }
            }
            if(!properType){
                return "452";
            }
        }

        if(venID != null && OADRConInfo.getVENId() != null &&
                !venID.equals(OADRConInfo.getVENId())){
            return "462";
        }

        if(registrationID != null && OADRConInfo.getRegistrationId() != null
                && !registrationID.equals(OADRConInfo.getRegistrationId())){
            return "462";
        }

        return "200";
    }

    /**
     * The origin message from a received is removed from the given map by this method.
     * This method should be called after no more methods are called from this class for a certain message.
     * @param obj reply
     * @param sentMsgMap given map
     */
    public abstract void updateSentMsgMap(OADRMsgObject obj, HashMap<String, OADRMsgObject> sentMsgMap);

    //-------------------------------static part ------------------------------------------------//
    static public HashMap<String, String> respValueText;
    static {
        respValueText = new HashMap<>();
        respValueText.put("200", "OK");
        respValueText.put("450", "Out of sequence");
        respValueText.put("451", "Not Allowed");
        respValueText.put("452", "Invalid ID");
        respValueText.put("453", "Not recognized");
        respValueText.put("454", "Invalid Data");

        respValueText.put("460", "Signal not supported");
        respValueText.put("461", "Report not supported");
        respValueText.put("462", "Target mismatch");
        respValueText.put("463", "Not Registered/Authorized");
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
