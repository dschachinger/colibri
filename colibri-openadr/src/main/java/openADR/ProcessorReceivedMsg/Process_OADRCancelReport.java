package openADR.ProcessorReceivedMsg;

import com.enernoc.open.oadr2.model.v20b.OadrCancelReport;
import com.enernoc.open.oadr2.model.v20b.OadrCanceledPartyRegistration;
import com.enernoc.open.oadr2.model.v20b.OadrCanceledReport;
import openADR.OADRHandling.OADRParty;
import openADR.OADRMsgInfo.MsgInfo_OADRCancelReport;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

import java.util.HashMap;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrCancelReport.
 */
public class Process_OADRCancelReport extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrCancelReport.
     * Return null, because there is no need to reply to this type of message.
     * @param obj generate reply for this message. The contained message type has to be OadrCancelReport.
     * @param responseCode
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj, String responseCode) {
        OadrCancelReport recMsg = (OadrCancelReport)obj.getMsg();


        OadrCanceledReport response = new OadrCanceledReport();
        response.setEiResponse(genEiRespone(recMsg.getRequestID(), responseCode));



        return new OADRMsgObject("oadrCanceledReport", null, response);
    }

    /**
     * This method returns an MsgInfo_OADRCancelReport object.
     * This object contains all needful information for a engery consumer from an OadrCancelReport message.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrCancelReport.
     * @param party
     * @return  The openADR.OADRMsgInfo object contains all needful information for a engery consumer.
     */
    @Override
    public OADRMsgInfo extractInfo(OADRMsgObject obj, OADRParty party) {
        OadrCancelReport msg = (OadrCancelReport)obj.getMsg();
        MsgInfo_OADRCancelReport info = new MsgInfo_OADRCancelReport();

        OADRConInfo.deleteUpdateReportMsgWorkers(msg);

        for(String reportRequestID : msg.getReportRequestIDs()){
            info.getReportRequestIDs().add(reportRequestID);
        }
        info.setReportToFollow(msg.isReportToFollow());

        return info;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doRecMsgViolateConstraints(OADRMsgObject obj, HashMap<String, OADRMsgObject> sendedMsgMap){
        OadrCancelReport recMsg = (OadrCancelReport)obj.getMsg();
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
        return new MsgInfo_OADRCancelReport().getMsgType();
    }
}
