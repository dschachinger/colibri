package openADR.ProcessorReceivedMsg;

import com.enernoc.open.oadr2.model.v20b.OadrCanceledPartyRegistration;
import com.enernoc.open.oadr2.model.v20b.OadrCreatedPartyRegistration;
import com.enernoc.open.oadr2.model.v20b.OadrProfiles;
import com.enernoc.open.oadr2.model.v20b.OadrTransports;
import openADR.OADRHandling.OADRParty;
import openADR.OADRMsgInfo.MsgInfo_OADRCreatedPartyRegistration;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrCreatedPartyRegistration.
 */
public class Process_OADRCreatedPartyRegistration extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for an openADR message OadrCreatedPartyRegistration.
     * Return null, because there is no need to reply to this type of message.
     * @param obj generate reply for this message. The contained message type has to be OadrCreatedPartyRegistration.
     * @param responseCode
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj, String responseCode) {

        return null;
    }

    /**
     * This method returns an MsgInfo_OADRCreatedPartyRegistration object.
     * This object contains all needful information for an engery consumer from an OadrCreatedPartyRegistration message.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrCreatedPartyRegistration.
     * @param party
     * @return  The openADR.OADRMsgInfo object contains all needful information for an engery consumer.
     */
    @Override
    public OADRMsgInfo extractInfo(OADRMsgObject obj, OADRParty party) {
        OadrCreatedPartyRegistration recMsg = (OadrCreatedPartyRegistration)obj.getMsg();
        MsgInfo_OADRCreatedPartyRegistration info = new MsgInfo_OADRCreatedPartyRegistration();

        if(recMsg.getVenID() != null){
            OADRConInfo.setVENId(recMsg.getVenID());
        }

        OADRConInfo.setVTNId(recMsg.getVtnID());

        if(recMsg.getRegistrationID() != null){
            OADRConInfo.setRegistrationId(recMsg.getRegistrationID());
        }

        for(OadrProfiles.OadrProfile profile : recMsg.getOadrProfiles().getOadrProfiles()){
            if(profile.getOadrProfileName().equals("2.0b")){
                for(OadrTransports.OadrTransport transport : profile.getOadrTransports().getOadrTransports()){
                    info.addTransportMethod(transport.getOadrTransportName().value());
                }
            }
        }

        return info;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doRecMsgViolateConstraints(OADRMsgObject obj, HashMap<String, OADRMsgObject> sentMsgMap){
        OadrCreatedPartyRegistration recMsg = (OadrCreatedPartyRegistration)obj.getMsg();
        String requestID = recMsg.getEiResponse().getRequestID();
        List<String> originMsgTypes = new ArrayList<>();
        originMsgTypes.add("oadrCreatePartyRegistration");
        originMsgTypes.add("oadrQueryRegistration");
        String registrationID = recMsg.getRegistrationID();

        String statusCode = checkConstraintsExtendedOriginMsgTypes(sentMsgMap, false, requestID,
                originMsgTypes, null, registrationID);


        if(statusCode.equals("200")){
            OADRMsgObject originMsg = sentMsgMap.get(recMsg.getEiResponse().getRequestID());

            if(originMsg.getMsgType().equals("oadrCreatePartyRegistration") &&
                    (recMsg.getVenID() == null ||
                            recMsg.getRegistrationID() == null) ){
                return "452";
            }
        }
        return statusCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateSentMsgMap(OADRMsgObject obj, HashMap<String, OADRMsgObject> sentMsgMap) {
        OadrCreatedPartyRegistration recMsg = (OadrCreatedPartyRegistration)obj.getMsg();
        sentMsgMap.remove(recMsg.getEiResponse().getRequestID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRCreatedPartyRegistration().getMsgType();
    }
}
