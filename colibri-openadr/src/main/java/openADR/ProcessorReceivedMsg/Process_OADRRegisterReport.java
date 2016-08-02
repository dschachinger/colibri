package openADR.ProcessorReceivedMsg;

import com.enernoc.open.oadr2.model.v20b.OadrCanceledPartyRegistration;
import com.enernoc.open.oadr2.model.v20b.OadrRegisterReport;
import com.enernoc.open.oadr2.model.v20b.OadrRegisteredReport;
import openADR.OADRHandling.OADRParty;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

import java.util.HashMap;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrRegisterReport.
 */
public class Process_OADRRegisterReport extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrRegisterReport.
     * Return null, because there is no need to reply to this type of message.
     * @param obj generate reply for this message. The contained message type has to be OadrRegisterReport.
     * @param responseCode
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj, String responseCode) {
        OadrRegisterReport recMsg = (OadrRegisterReport)obj.getMsg();

        OadrRegisteredReport response = new OadrRegisteredReport();
        response.setSchemaVersion("2.0b");
        response.setEiResponse(genEiRespone(recMsg.getRequestID(), responseCode));

        return new OADRMsgObject("oadrRegisteredReport", null, response);
    }

    /**
     * This method returns an MsgInfo_OADRRegisterReport object.
     * This object contains all needful information for a engery consumer from an OadrRegisterReport message.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrRegisterReport.
     * @param party
     * @return  The openADR.OADRMsgInfo object contains all needful information for a engery consumer.
     */
    @Override
    public OADRMsgInfo extractInfo(OADRMsgObject obj, OADRParty party) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doRecMsgViolateConstraints(OADRMsgObject obj, HashMap<String, OADRMsgObject> sendedMsgMap){
        OadrRegisterReport recMsg = (OadrRegisterReport)obj.getMsg();
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
        return "oadrRegisterReport";
    }
}
