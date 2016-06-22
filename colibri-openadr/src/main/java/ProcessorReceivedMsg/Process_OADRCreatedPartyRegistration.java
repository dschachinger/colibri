package ProcessorReceivedMsg;

import OADRHandling.OADRParty;
import OADRMsgInfo.*;
import Utils.OADRConInfo;
import Utils.OADRMsgObject;
import com.enernoc.open.oadr2.model.v20b.*;

import java.util.HashMap;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrCreatedPartyRegistration.
 */
public class Process_OADRCreatedPartyRegistration extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrCreatedPartyRegistration.
     * Return null, because there is no need to reply to this type of message.
     * @param obj generate reply for this message. The contained message type has to be OadrCreatedPartyRegistration.
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj) {

        return null;
    }

    /**
     * This method returns an MsgInfo_OADRCreatedPartyRegistration object.
     * This object contains all needful information for a engery consumer from an OadrCreatedPartyRegistration message.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrCreatedPartyRegistration.
     * @param party
     * @return  The OADRMsgInfo object contains all needful information for a engery consumer.
     */
    @Override
    public OADRMsgInfo extractInfo(OADRMsgObject obj, OADRParty party) {
        OadrCreatedPartyRegistration recMsg = (OadrCreatedPartyRegistration)obj.getMsg();
        MsgInfo_OADRCreatedPartyRegistration info = new MsgInfo_OADRCreatedPartyRegistration();

        if(recMsg.getVenID() != null){
            OADRConInfo.setVENId(recMsg.getVenID());
        }

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
    public boolean doRecMsgViolateConstraintsAndUpdateSendMap(OADRMsgObject obj, HashMap<String, OADRMsgObject> sendedMsgMap){
        if(OADRConInfo.getVENId() == null){
            return true;
        }

        OadrCreatedPartyRegistration recMsg = (OadrCreatedPartyRegistration)obj.getMsg();
        if(sendedMsgMap.get(recMsg.getEiResponse().getRequestID()) == null){
            return true;
        }

        OADRMsgObject originMsg = sendedMsgMap.get(recMsg.getEiResponse().getRequestID());
        if(!originMsg.getMsgType().equals("oadrCreatePartyRegistration") ||
                ! originMsg.getMsgType().equals("oadrQueryRegistration")){
            return true;
        }

        if(originMsg.getMsgType().equals("oadrCreatePartyRegistration") &&
                (recMsg.getVenID() == null ||
                recMsg.getRegistrationID() == null) ){
            return true;
        }

        if(!recMsg.getVenID().equals(OADRConInfo.getVENId())){
            return true;
        }

        if(OADRConInfo.getRegistrationId() != null && !recMsg.getRegistrationID().equals(OADRConInfo.getRegistrationId())){
            return true;
        }

        sendedMsgMap.remove(recMsg.getEiResponse().getRequestID());

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRCreatedPartyRegistration().getMsgType();
    }
}
