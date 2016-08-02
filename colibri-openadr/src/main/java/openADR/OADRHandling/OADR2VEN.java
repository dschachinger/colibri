package openADR.OADRHandling;

import Bridge.OpenADRColibriBridge;
import com.enernoc.open.oadr2.model.v20b.ei.OptTypeType;
import openADR.OADRMsgInfo.*;
import openADR.Utils.FollowUpMsg;
import openADR.Utils.OADRConInfo;

import java.util.Date;
import java.util.List;

/**
 * Created by georg on 02.06.16.
 * This class represents from the openADR standard the VEN party.
 */
public class OADR2VEN extends OADRParty {

    public OADR2VEN(OpenADRColibriBridge bridge, int timeoutSec) {
        super(bridge, timeoutSec);
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

        try {
            Thread.sleep(timeoutSec * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<MsgInfo_OADRCreatedEvent.EventResponse> eventResponses = reply.getEventResponses();
        for(MsgInfo_OADRDistributeEvent.Event event : con_info.getEvents()){
            if(!event.isCreatedEventToVTNSent()){
                MsgInfo_OADRCreatedEvent.EventResponse eventResponse = reply.getNewEventResponse();

                eventResponse.setRequestID(con_info.getRequestID());
                eventResponse.setEventID(event.getEventID());
                eventResponse.setModificationNumber(event.getModificationNumber());
                eventResponse.setOptType(OptTypeType.OPT_OUT);

                eventResponses.add(eventResponse);
            }

        }

        if(!reply.getEventResponses().isEmpty()){
            channel.sendMsg(reply);
        }
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

    public MsgInfo_OADRCancelPartyRegistration createCancelRegInfoObj(){
        MsgInfo_OADRCancelPartyRegistration cancelPartyRegistration = new MsgInfo_OADRCancelPartyRegistration();
        cancelPartyRegistration.setRegistrationID(OADRConInfo.getRegistrationId());
        return cancelPartyRegistration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terminate(){
        channel.sendMsg(createCancelRegInfoObj());
        channel.close();
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

    public void sendExampleOadrRegisterReport(){
        MsgInfo_OADRRegisterReport info = new MsgInfo_OADRRegisterReport();

        info.getReports().addAll(OADRConInfo.getAllReportPossibilities());

        channel.sendMsg(info);
    }

    public void sendExampleOadrUpdateReport(){
        MsgInfo_OADRUpdateReport info = new MsgInfo_OADRUpdateReport();

        Report report = new Report();

        report.setCreatedDateTime(new Date());
        report.setDurationSec(960);
        report.setReportName("HISTORY_USAGE");
        report.setReportRequestID("RR_65432");
        report.setReportSpecifierID("RS_12345");

        Interval interval = new Interval();
        interval.setDurationSec(961);
        interval.setSignalValue(88);
        interval.setUid("0");

        report.getIntervals().add(interval);

        info.getReports().add(report);

        channel.sendMsg(info);
    }
}
