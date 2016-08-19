package openADR.ProcessorReceivedMsg;

import com.enernoc.open.oadr2.model.v20b.OadrCanceledPartyRegistration;
import com.enernoc.open.oadr2.model.v20b.OadrResponse;
import openADR.OADRHandling.OADRParty;
import openADR.OADRMsgInfo.MsgInfo_OADRResponse;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

import java.util.HashMap;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrResponse.
 */
public class Process_OADRResponse extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for an openADR message OadrResponse.
     * Return null, because there is no need to reply to this type of message.
     * @param obj generate reply for this message. The contained message type has to be OadrResponse.
     * @param responseCode
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj, String responseCode) {
        return null;
    }

    /**
     * This method returns an MsgInfo_OADRResponse object.
     * This object contains all needful information for an engery consumer from an OadrResponse message.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrResponse.
     * @param party
     * @return  The openADR.OADRMsgInfo object contains all needful information for an engery consumer.
     */
    @Override
    public OADRMsgInfo extractInfo(OADRMsgObject obj, OADRParty party) {
        OadrResponse msg = (OadrResponse)obj.getMsg();
        MsgInfo_OADRResponse info = new MsgInfo_OADRResponse();

        info.setCorrespondingRequestID(msg.getEiResponse().getRequestID());
        info.setResponseCode(Integer.parseInt(msg.getEiResponse().getResponseCode().getValue()));
        info.setResponseDescription(msg.getEiResponse().getResponseDescription());

        return info;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doRecMsgViolateConstraints(OADRMsgObject obj, HashMap<String, OADRMsgObject> sentMsgMap){
        OadrResponse recMsg = (OadrResponse)obj.getMsg();
        String venID = recMsg.getVenID();

        return checkConstraints(sentMsgMap, true, null,
                null, venID, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateSentMsgMap(OADRMsgObject obj, HashMap<String, OADRMsgObject> sentMsgMap) {
        OadrResponse recMsg = (OadrResponse)obj.getMsg();
        sentMsgMap.remove(recMsg.getEiResponse().getRequestID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRResponse().getMsgType();
    }
}
