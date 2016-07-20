package openADR.ProcessorReceivedMsg;

import com.enernoc.open.oadr2.model.v20b.OadrRegisteredReport;
import com.enernoc.open.oadr2.model.v20b.OadrUpdatedReport;
import openADR.OADRHandling.OADRParty;
import openADR.OADRMsgInfo.MsgInfo_OADRUpdatedReport;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

import java.util.HashMap;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrUpdatedReport.
 */
public class Process_OADRUpdatedReport extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrUpdatedReport.
     * Return null, because there is no need to reply to this type of message.
     * @param obj generate reply for this message. The contained message type has to be OadrUpdatedReport.
     * @param responseCode
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj, String responseCode) {
        return null;
    }

    /**
     * This method returns an MsgInfo_OADRUpdatedReport object.
     * This object contains all needful information for a engery consumer from an OadrUpdatedReport message.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrUpdatedReport.
     * @param party
     * @return  The openADR.OADRMsgInfo object contains all needful information for a engery consumer.
     */
    @Override
    public OADRMsgInfo extractInfo(OADRMsgObject obj, OADRParty party) {
        OadrUpdatedReport msg = (OadrUpdatedReport)obj.getMsg();
        MsgInfo_OADRUpdatedReport info = new MsgInfo_OADRUpdatedReport();

        OADRConInfo.deleteUpdateReportMsgWorkers(msg.getOadrCancelReport());

        for(String reportRequestID : msg.getOadrCancelReport().getReportRequestIDs()){
            info.getCancelReport().getReportRequestIDs().add(reportRequestID);
        }
        info.getCancelReport().setReportToFollow(msg.getOadrCancelReport().isReportToFollow());
        return info;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doRecMsgViolateConstraintsAndUpdateSendMap(OADRMsgObject obj, HashMap<String, OADRMsgObject> sendedMsgMap){
        if(OADRConInfo.getVENId() == null){
            return true;
        }

        OadrRegisteredReport recMsg = (OadrRegisteredReport)obj.getMsg();
        if(sendedMsgMap.get(recMsg.getEiResponse().getRequestID()) == null){
            return true;
        }
        OADRMsgObject originMsg = sendedMsgMap.get(recMsg.getEiResponse().getRequestID());
        if(!originMsg.getMsgType().equals("oadrUpdateReport")){
            return true;
        }
        sendedMsgMap.remove(recMsg.getEiResponse().getRequestID());


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
        return new MsgInfo_OADRUpdatedReport().getMsgType();
    }
}
