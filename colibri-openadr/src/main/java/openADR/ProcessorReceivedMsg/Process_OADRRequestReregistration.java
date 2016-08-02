package openADR.ProcessorReceivedMsg;

import com.enernoc.open.oadr2.model.v20b.OadrCanceledPartyRegistration;
import com.enernoc.open.oadr2.model.v20b.OadrRequestReregistration;
import com.enernoc.open.oadr2.model.v20b.OadrResponse;
import openADR.OADRHandling.AsyncSendFollowUpMsgWorker;
import openADR.OADRHandling.OADRParty;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.FollowUpMsg;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

import java.util.HashMap;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrRequestReregistration.
 */
public class Process_OADRRequestReregistration extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrRequestReregistration.
     * @param obj generate reply for this message. The contained message type has to be OadrRequestReregistration.
     * @param responseCode
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj, String responseCode) {
        OadrRequestReregistration recMsg = (OadrRequestReregistration)obj.getMsg();

        OadrResponse response = new OadrResponse();
        response.setVenID(OADRConInfo.getVENId());
        response.setSchemaVersion("2.0b");


        response.setEiResponse(genEiRespone(null, responseCode));

        return new OADRMsgObject("oadrResponse", null, response);
    }

    /**
     * This method returns null because a oadrRequestReregistration contains no needful information for a engery consumer.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrRequestReregistration.
     * @param party
     * @return  null
     */
    @Override
    public OADRMsgInfo extractInfo(OADRMsgObject obj, OADRParty party) {
        OadrRequestReregistration msg = (OadrRequestReregistration)obj.getMsg();

        // start new thread
        new AsyncSendFollowUpMsgWorker(party, new FollowUpMsg(null, FollowUpMsg.FollowUpMsgType.oadrCreatePartyRegistration)).start();
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doRecMsgViolateConstraints(OADRMsgObject obj, HashMap<String, OADRMsgObject> sendedMsgMap){
        OadrRequestReregistration recMsg = (OadrRequestReregistration)obj.getMsg();
        String venID = recMsg.getVenID();

        return checkConstraints(sendedMsgMap, true, null,
                null, venID, null);
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
        return "oadrRequestReregistration";
    }
}
