package ProcessorReceivedMsg;

import OADRHandling.AsyncSendUpdateReportMsgWorker;
import OADRHandling.OADRParty;
import OADRMsgInfo.*;
import OADRMsgInfo.OADRMsgInfo;
import Utils.OADRConInfo;
import Utils.OADRMsgObject;
import com.enernoc.open.oadr2.model.v20b.*;

import java.util.HashMap;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrCanceledPartyRegistration.
 */
public class Process_OADRCancelReport extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrCanceledPartyRegistration.
     * Return null, because there is no need to reply to this type of message.
     * @param obj generate reply for this message. The contained message type has to be OadrCanceledPartyRegistration.
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj) {
        OadrCancelReport recMsg = (OadrCancelReport)obj.getMsg();


        OadrCanceledReport response = new OadrCanceledReport();
        response.setEiResponse(genEiRespone(recMsg.getRequestID()));



        return new OADRMsgObject("oadrCanceledReport", null, response);
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
    public boolean doRecMsgViolateConstraintsAndUpdateSendMap(OADRMsgObject obj, HashMap<String, OADRMsgObject> sendedMsgMap){
        if(OADRConInfo.getVENId() == null){
            return true;
        }

        OadrCancelReport recMsg = (OadrCancelReport)obj.getMsg();

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
        return new MsgInfo_OADRCancelReport().getMsgType();
    }
}
