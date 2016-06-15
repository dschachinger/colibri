package ProcessorReceivedMsg;

import OADRMsgInfo.MsgInfo_OADRCreatedPartyRegistration;
import OADRMsgInfo.OADRMsgInfo;
import Utils.FollowUpMsg;
import Utils.OADRConInfo;
import Utils.OADRMsgObject;
import com.enernoc.open.oadr2.model.v20b.*;
import com.enernoc.open.oadr2.model.v20b.ei.EiResponse;
import com.enernoc.open.oadr2.model.v20b.ei.ResponseCode;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrRequestReregistration.
 */
public class Process_OADRRequestReregistration extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrRequestReregistration.
     * @param obj generate reply for this message. The contained message type has to be OadrRequestReregistration.
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj) {
        OadrRequestReregistration recMsg = (OadrRequestReregistration)obj.getMsg();

        OadrResponse response = new OadrResponse();
        response.setVenID(OADRConInfo.getVENId());
        response.setSchemaVersion("2.0b");


        response.setEiResponse(genEiRespone(null));

        return new OADRMsgObject("oadrResponse", null, response,
                new FollowUpMsg(extractInfo(obj), FollowUpMsg.FollowUpMsgType.oadrCreatePartyRegistration));
    }

    /**
     * This method returns null because a oadrRequestReregistration contains no needful information for a engery consumer.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrRequestReregistration.
     * @return  null
     */
    @Override
    public OADRMsgInfo extractInfo(OADRMsgObject obj) {
        OadrRequestReregistration msg = (OadrRequestReregistration)obj.getMsg();
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return "oadrRequestReregistration";
    }
}
