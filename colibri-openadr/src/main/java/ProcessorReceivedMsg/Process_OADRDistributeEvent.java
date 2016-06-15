package ProcessorReceivedMsg;

import OADRMsgInfo.*;
import OADRMsgInfo.OADRMsgInfo;
import Utils.FollowUpMsg;
import Utils.OADRMsgObject;
import Utils.TimeDurationConverter;
import com.enernoc.open.oadr2.model.PayloadFloat;
import com.enernoc.open.oadr2.model.v20b.OadrDistributeEvent;
import com.enernoc.open.oadr2.model.v20b.ei.*;

import java.util.List;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrDistributeEvent.
 */
public class Process_OADRDistributeEvent extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrDistributeEvent.
     * @param obj generate reply for this message. The contained message type has to be OadrDistributeEvent.
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj) {
        return new OADRMsgObject("emptyStanze", null, null,
                new FollowUpMsg(extractInfo(obj), FollowUpMsg.FollowUpMsgType.oadrCreatedEvent));
    }

    /**
     * This method returns an MsgInfo_OADRDistributeEvent object.
     * This object contains all needful information for a engery consumer from an OadrDistributeEvent message.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrDistributeEvent.
     * @return  The OADRMsgInfo object contains all needful information for a engery consumer.
     */
    @Override
    public OADRMsgInfo extractInfo(OADRMsgObject obj) {
        OadrDistributeEvent msg = (OadrDistributeEvent)obj.getMsg();
        MsgInfo_OADRDistributeEvent info = new MsgInfo_OADRDistributeEvent();

        info.setRequestID(msg.getRequestID());

        for (OadrDistributeEvent.OadrEvent oadrEvent : msg.getOadrEvents()){
            MsgInfo_OADRDistributeEvent.Event event = info.getNewEvent();

            EventDescriptor descriptor = oadrEvent.getEiEvent().getEventDescriptor();
            event.setEventID(descriptor.getEventID());
            event.setModificationNumber(descriptor.getModificationNumber());
            event.setPriority(descriptor.getPriority());
            event.setTestEvent(!descriptor.getTestEvent().equals("false"));
            event.setMarketContext(descriptor.getEiMarketContext().getMarketContext().getValue());

            event.setResponseRequired(oadrEvent.getOadrResponseRequired());

            event.setStartDate(TimeDurationConverter.ical2Date(oadrEvent.getEiEvent().getEiActivePeriod().getProperties().getDtstart().getDateTime().getValue().toString()));
            event.setToleranceSec(TimeDurationConverter.xCal2Seconds(oadrEvent.getEiEvent().getEiActivePeriod().getProperties().getTolerance().getTolerate().getStartafter().getValue()));
            event.setDurationSec(TimeDurationConverter.xCal2Seconds(oadrEvent.getEiEvent().getEiActivePeriod().getProperties().getDuration().getDuration().getValue()));

            event.setRampUpSec(TimeDurationConverter.xCal2Seconds(oadrEvent.getEiEvent().getEiActivePeriod().getProperties().getXEiRampUp().getDuration().getValue()));
            event.setRecoverySec(TimeDurationConverter.xCal2Seconds(oadrEvent.getEiEvent().getEiActivePeriod().getProperties().getXEiRecovery().getDuration().getValue()));

            for(EiEventSignal eiEventSignal : oadrEvent.getEiEvent().getEiEventSignals().getEiEventSignals()){
                MsgInfo_OADRDistributeEvent.Signal signal = info.getNewSignal();

                signal.setSignalType(eiEventSignal.getSignalType());
                signal.setCurrentValue(eiEventSignal.getCurrentValue().getPayloadFloat().getValue());

                List<MsgInfo_OADRDistributeEvent.Interval> info_intervals = signal.getIntervals();
                for(Interval interval : eiEventSignal.getIntervals().getIntervals()){
                    MsgInfo_OADRDistributeEvent.Interval info_interval = info.getNewInterval();
                    info_interval.setDurationSec(TimeDurationConverter.xCal2Seconds(interval.getDuration().getDuration().getValue()));
                    // conformance rule 100: The number of signalPayload elements in each interval MUST be equal to 1 and the used types are according to the typ casts
                    info_interval.setSignalValue(((PayloadFloatType)((SignalPayload)interval.getStreamPayloadBases().get(0).getValue()).getPayloadBase().getValue()).getValue());

                    info_intervals.add(info_interval);
                }

                event.getSignals().add(signal);
            }
            info.getEvents().add(event);
        }

        System.out.println("obj: \n"+info);

        return info;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRDistributeEvent().getMsgType();
    }
}
