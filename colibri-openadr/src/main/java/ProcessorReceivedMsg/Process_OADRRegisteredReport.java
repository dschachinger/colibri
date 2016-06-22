package ProcessorReceivedMsg;

import OADRHandling.OADRParty;
import OADRMsgInfo.*;
import OADRMsgInfo.OADRMsgInfo;
import Utils.OADRConInfo;
import Utils.OADRMsgObject;
import com.enernoc.open.oadr2.model.v20b.*;

import java.util.HashMap;
import java.util.List;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrCanceledPartyRegistration.
 */
public class Process_OADRRegisteredReport extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrCanceledPartyRegistration.
     * Return null, because there is no need to reply to this type of message.
     * @param obj generate reply for this message. The contained message type has to be OadrCanceledPartyRegistration.
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj) {
        OadrRegisteredReport recMsg = (OadrRegisteredReport)obj.getMsg();

        List<OadrReportRequest> reportRequests = recMsg.getOadrReportRequests();
        if(reportRequests.isEmpty()){
            /*
                RETURN no message if the received message does not contain report requests
            */
            return null;
        }


        OadrCreatedReport response = new OadrCreatedReport();
        response.setEiResponse(genEiRespone(null));
        for(OadrReportRequest reportRequest : reportRequests){

        }


        return new OADRMsgObject("oadrCreatedReport", null, response);
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
        OadrRegisteredReport msg = (OadrRegisteredReport)obj.getMsg();
        MsgInfo_OADRRegisteredReport info = new MsgInfo_OADRRegisteredReport();




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
        if(!originMsg.getMsgType().equals("oadrRegisterReport")){
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
        return new MsgInfo_OADRRegisteredReport().getMsgType();
    }
}
