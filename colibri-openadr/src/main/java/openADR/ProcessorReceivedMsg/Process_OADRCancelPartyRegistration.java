package openADR.ProcessorReceivedMsg;

import com.enernoc.open.oadr2.model.v20b.OadrCancelPartyRegistration;
import com.enernoc.open.oadr2.model.v20b.OadrCanceledPartyRegistration;
import openADR.OADRHandling.OADRParty;
import openADR.OADRMsgInfo.MsgInfo_OADRCancelPartyRegistration;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

import java.util.HashMap;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrCancelPartyRegistration.
 */
public class Process_OADRCancelPartyRegistration extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrCancelPartyRegistration.
     * @param obj generate reply for this message. The contained message type has to be OadrCancelPartyRegistration.
     * @param responseCode
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj, String responseCode) {
        OadrCancelPartyRegistration recMsg = (OadrCancelPartyRegistration)obj.getMsg();

        OadrCanceledPartyRegistration response = new OadrCanceledPartyRegistration();
        response.setVenID(OADRConInfo.getVENId());
        response.setSchemaVersion("2.0b");

        response.setEiResponse(genEiRespone(recMsg.getRequestID(), responseCode));
        response.setRegistrationID(recMsg.getRegistrationID());

        return new OADRMsgObject("oadrResponse", null, response);
    }

    /**
     * This method returns an MsgInfo_OADRCancelPartyRegistration object.
     * This object contains all needful information for a engery consumer from an OadrCancelPartyRegistration message.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrCancelPartyRegistration.
     * @param party
     * @return  The openADR.OADRMsgInfo object contains all needful information for a engery consumer.
     */
    @Override
    public OADRMsgInfo extractInfo(OADRMsgObject obj, OADRParty party) {
        OadrCancelPartyRegistration msg = (OadrCancelPartyRegistration)obj.getMsg();
        MsgInfo_OADRCancelPartyRegistration info = new MsgInfo_OADRCancelPartyRegistration();
        OADRConInfo.deleteConnectionInfo();
        info.setRegistrationID(msg.getRegistrationID());
        return info;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doRecMsgViolateConstraints(OADRMsgObject obj, HashMap<String, OADRMsgObject> sendedMsgMap){
        OadrCancelPartyRegistration recMsg = (OadrCancelPartyRegistration)obj.getMsg();
        String venID = recMsg.getVenID();
        String registrationID = recMsg.getRegistrationID();

        return checkConstraints(sendedMsgMap, true, null,
                null, venID, registrationID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateSendedMsgMap(OADRMsgObject obj, HashMap<String, OADRMsgObject> sendedMsgMap) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRCancelPartyRegistration().getMsgType();
    }
}
