package openADR.ProcessorReceivedMsg;

import com.enernoc.open.oadr2.model.v20b.OadrCanceledPartyRegistration;
import openADR.OADRHandling.OADRParty;
import openADR.OADRMsgInfo.MsgInfo_OADRCanceledPartyRegistration;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

import java.util.HashMap;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrCanceledPartyRegistration.
 */
public class Process_OADRCanceledPartyRegistration extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrCanceledPartyRegistration.
     * Return null, because there is no need to reply to this type of message.
     * @param obj generate reply for this message. The contained message type has to be OadrCanceledPartyRegistration.
     * @param responseCode
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj, String responseCode) {
        return null;
    }

    /**
     * This method returns an MsgInfo_OADRCanceledPartyRegistration object.
     * This object contains all needful information for a engery consumer from an OadrCanceledPartyRegistration message.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrCanceledPartyRegistration.
     * @param party
     * @return  The openADR.OADRMsgInfo object contains all needful information for a engery consumer.
     */
    @Override
    public OADRMsgInfo extractInfo(OADRMsgObject obj, OADRParty party) {
        OadrCanceledPartyRegistration msg = (OadrCanceledPartyRegistration)obj.getMsg();
        MsgInfo_OADRCanceledPartyRegistration info = new MsgInfo_OADRCanceledPartyRegistration();
        OADRConInfo.deleteConnectionInfo();


        info.setRegistrationID(msg.getRegistrationID());

        return info;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doRecMsgViolateConstraints(OADRMsgObject obj, HashMap<String, OADRMsgObject> sendedMsgMap){
        OadrCanceledPartyRegistration recMsg = (OadrCanceledPartyRegistration)obj.getMsg();
        String requestID = recMsg.getEiResponse().getRequestID();
        String originMsgType = "oadrCancelPartyRegistration";
        String venID = recMsg.getVenID();
        String registrationID = recMsg.getRegistrationID();

        return checkConstraints(sendedMsgMap, true, requestID,
                originMsgType, venID, registrationID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateSendedMsgMap(OADRMsgObject obj, HashMap<String, OADRMsgObject> sendedMsgMap) {
        OadrCanceledPartyRegistration recMsg = (OadrCanceledPartyRegistration)obj.getMsg();
        sendedMsgMap.remove(recMsg.getEiResponse().getRequestID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRCanceledPartyRegistration().getMsgType();
    }
}
