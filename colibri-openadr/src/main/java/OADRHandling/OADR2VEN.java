package OADRHandling;

import OADRMsgInfo.*;
import OADRMsgInfo.OADRMsgInfo;
import Utils.FollowUpMsg;
import com.enernoc.open.oadr2.model.v20b.ei.OptTypeType;

import java.util.List;

/**
 * Created by georg on 02.06.16.
 * This class represents from the openADR standard the VEN party.
 */
public class OADR2VEN extends OADRParty {

    public OADR2VEN() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public void handleFollowUpMsg(FollowUpMsg followUpMsg){
        OADRMsgInfo info = followUpMsg.getOriginMsgInfo();
        switch (followUpMsg.getFollowUpMsgType()){
            case oadrCreatePartyRegistration:
                handelFollowUpMsgOADRCreatePartyRegistration(info);
                break;
            case oadrCreatedEvent:
                handelFollowUpMsgOADRCreateEvent(info);
                break;
        }
    }

    /**
     * This method is called if a follow-up message oadrCreateEvent is needed.
     * It handles the VTN communication by its own.
     * @param info contains information how the message should look like.
     */
    private void handelFollowUpMsgOADRCreateEvent(OADRMsgInfo info) {
        MsgInfo_OADRDistributeEvent con_info = (MsgInfo_OADRDistributeEvent) info;
        MsgInfo_OADRCreatedEvent reply = new MsgInfo_OADRCreatedEvent();

        List<MsgInfo_OADRCreatedEvent.EventResponse> eventResponses = reply.getEventResponses();
        for(MsgInfo_OADRDistributeEvent.Event event : con_info.getEvents()){
            MsgInfo_OADRCreatedEvent.EventResponse eventResponse = reply.getNewEventResponse();

            eventResponse.setRequestID(con_info.getRequestID());
            eventResponse.setEventID(event.getEventID());
            eventResponse.setModificationNumber(event.getModificationNumber());
            eventResponse.setOptType(OptTypeType.OPT_IN);

            eventResponses.add(eventResponse);
        }
        channel.sendMsg(reply);
    }

    /**
     * This method is called if a follow-up message oadrCreatePartyRegistration is needed.
     * It handles the VTN communication by its own.
     * @param info contains information how the message should look like.
     */
    private void handelFollowUpMsgOADRCreatePartyRegistration(OADRMsgInfo info){
        MsgInfo_OADRCreatePartyRegistration reply = new MsgInfo_OADRCreatePartyRegistration();
        channel.sendMsg(reply);
    }

    //--------------------------- the following part is only for test purposes ------------------------------------//

    public void sendExampleOadrQueryRegistration(){
        MsgInfo_OADRQueryRegistration info = new MsgInfo_OADRQueryRegistration();
        channel.sendMsg(info);
    }

    public void sendExampleOadrCreatePartyRegistration(){
        MsgInfo_OADRCreatePartyRegistration info = new MsgInfo_OADRCreatePartyRegistration();
        channel.sendMsg(info);
    }

    public void sendExampleOadrCancelPartyRegistration(){
        MsgInfo_OADRCancelPartyRegistration info = new MsgInfo_OADRCancelPartyRegistration();
        channel.sendMsg(info);
    }

    public void sendExampleOadrRequestEvent(){
        MsgInfo_OADRRequestEvent info = new MsgInfo_OADRRequestEvent();
        info.setReplyLimit(new Long(42));
        channel.sendMsg(info);
    }
}
