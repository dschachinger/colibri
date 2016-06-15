package ProcessorReceivedMsg;

import OADRMsgInfo.*;
import Utils.OADRConInfo;
import Utils.OADRMsgObject;
import com.enernoc.open.oadr2.model.v20b.OadrCancelPartyRegistration;
import com.enernoc.open.oadr2.model.v20b.OadrCanceledPartyRegistration;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrCanceledPartyRegistration.
 */
public class Process_OADRCanceledPartyRegistration extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrCanceledPartyRegistration.
     * Return null, because there is no need to reply to this type of message.
     * @param obj generate reply for this message. The contained message type has to be OadrCanceledPartyRegistration.
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj) {
        return null;
    }

    /**
     * This method returns an MsgInfo_OADRCanceledPartyRegistration object.
     * This object contains all needful information for a engery consumer from an OadrCanceledPartyRegistration message.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrCanceledPartyRegistration.
     * @return  The OADRMsgInfo object contains all needful information for a engery consumer.
     */
    @Override
    public OADRMsgInfo extractInfo(OADRMsgObject obj) {
        OadrCanceledPartyRegistration msg = (OadrCanceledPartyRegistration)obj.getMsg();
        MsgInfo_OADRCanceledPartyRegistration info = new MsgInfo_OADRCanceledPartyRegistration();

        OADRConInfo.setRegistrationId(null);


        info.setRegistrationID(msg.getRegistrationID());


        return info;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRCanceledPartyRegistration().getMsgType();
    }
}
