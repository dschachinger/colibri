package ProcessorReceivedMsg;

import OADRMsgInfo.*;
import OADRMsgInfo.OADRMsgInfo;
import Utils.OADRConInfo;
import Utils.OADRMsgObject;
import com.enernoc.open.oadr2.model.v20b.*;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrCancelPartyRegistration.
 */
public class Process_OADRCancelPartyRegistration extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrCancelPartyRegistration.
     * @param obj generate reply for this message. The contained message type has to be OadrCancelPartyRegistration.
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj) {
        OadrCancelPartyRegistration recMsg = (OadrCancelPartyRegistration)obj.getMsg();

        OadrCanceledPartyRegistration response = new OadrCanceledPartyRegistration();
        response.setVenID(OADRConInfo.getVENId());
        response.setSchemaVersion("2.0b");


        response.setEiResponse(genEiRespone(recMsg.getRequestID()));
        // TODO checken, ob das eh die richtige id ist
        response.setRegistrationID(recMsg.getRegistrationID());

        return new OADRMsgObject("oadrResponse", null, response);
    }

    /**
     * This method returns an MsgInfo_OADRCancelPartyRegistration object.
     * This object contains all needful information for a engery consumer from an OadrCancelPartyRegistration message.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrCancelPartyRegistration.
     * @return  The OADRMsgInfo object contains all needful information for a engery consumer.
     */
    @Override
    public OADRMsgInfo extractInfo(OADRMsgObject obj) {
        OadrCancelPartyRegistration msg = (OadrCancelPartyRegistration)obj.getMsg();
        MsgInfo_OADRCancelPartyRegistration info = new MsgInfo_OADRCancelPartyRegistration();
        OADRConInfo.setRegistrationId(null);
        info.setRegistrationID(msg.getRegistrationID());
        return info;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRCancelPartyRegistration().getMsgType();
    }
}
