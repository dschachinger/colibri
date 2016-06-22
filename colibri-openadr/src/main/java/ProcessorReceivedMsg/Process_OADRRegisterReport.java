package ProcessorReceivedMsg;

import OADRHandling.OADRParty;
import OADRMsgInfo.MsgInfo_OADRRegisteredReport;
import OADRMsgInfo.OADRMsgInfo;
import Utils.OADRConInfo;
import Utils.OADRMsgObject;
import com.enernoc.open.oadr2.model.v20b.OadrCreatedReport;
import com.enernoc.open.oadr2.model.v20b.OadrRegisterReport;
import com.enernoc.open.oadr2.model.v20b.OadrRegisteredReport;
import com.enernoc.open.oadr2.model.v20b.OadrReportRequest;

import java.util.HashMap;
import java.util.List;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrCanceledPartyRegistration.
 */
public class Process_OADRRegisterReport extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrCanceledPartyRegistration.
     * Return null, because there is no need to reply to this type of message.
     * @param obj generate reply for this message. The contained message type has to be OadrCanceledPartyRegistration.
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj) {
        OadrRegisterReport recMsg = (OadrRegisterReport)obj.getMsg();

        OadrRegisteredReport response = new OadrRegisteredReport();
        response.setSchemaVersion("2.0b");
        response.setEiResponse(genEiRespone(recMsg.getRequestID()));

        return new OADRMsgObject("oadrRegisteredReport", null, response);
    }

    /**
     * This method returns an MsgInfo_OADRCanceledPartyRegistration object.
     * This object contains all needful information for a engery consumer from an OadrCanceledPartyRegistration message.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrCanceledPartyRegistration.
     * @param party
     * @return  The OADRMsgInfo object contains all needful information for a engery consumer.
     */
    @Override
    public OADRMsgInfo extractInfo(OADRMsgObject obj, OADRParty party) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doRecMsgViolateConstraintsAndUpdateSendMap(OADRMsgObject obj, HashMap<String, OADRMsgObject> sendedMsgMap){
        if(OADRConInfo.getVENId() == null){
            return true;
        }

        OadrRegisterReport recMsg = (OadrRegisterReport)obj.getMsg();

        if(!recMsg.getVenID().equals(OADRConInfo.getVENId())){
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return "oadrRegisterReport";
    }
}
