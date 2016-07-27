package Utils;

import com.enernoc.open.oadr2.model.v20b.ei.SignalTypeEnumeratedType;
import openADR.OADRMsgInfo.Interval;
import openADR.OADRMsgInfo.MsgInfo_OADRDistributeEvent;
import openADR.OADRMsgInfo.OADRMsgInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semanticCore.MsgObj.ColibriMessage;
import semanticCore.MsgObj.ContentMsgObj.*;
import semanticCore.MsgObj.ContentType;
import semanticCore.MsgObj.Header;
import semanticCore.MsgObj.MsgType;

import java.util.*;

/**
 * Created by georg on 04.07.16.
 * Objects from this class are used to bridge the openADR part with the colibri part.
 * This is only for the direction from openADR to colibri.
 */
public class OpenADRToColibri {

    private Logger logger = LoggerFactory.getLogger(OpenADRToColibri.class);

    public List<ColibriMessage> convertOpenADRMsg(OADRMsgInfo msg, OpenADRColibriBridge bridge){
        List<ColibriMessage> sendToColibriMsges = null;

        String type = msg.getMsgType();

        switch (type){
            case "oadrDistributeEvent":
                sendToColibriMsges = handle_DistributeEvent(msg, bridge);
                break;
        }

        return sendToColibriMsges;

    }

    private List<ColibriMessage> handle_DistributeEvent(OADRMsgInfo msg, OpenADRColibriBridge bridge) {
        MsgInfo_OADRDistributeEvent con_msg = (MsgInfo_OADRDistributeEvent) msg;
        List<ColibriMessage> colibriMessages = new ArrayList<>();

        String serviceBaseURL = bridge.getColClient().getServiceBaseURL();

        Header header;
        PutMsg putMsg;

        for (MsgInfo_OADRDistributeEvent.Event event : con_msg.getEvents()) {
            header = new Header();
            header.setDate(new Date());
            header.setContentType(ContentType.TEXT_PLAIN);
            header.setMessageId(bridge.getColClient().getGenSendMessage().getUniqueMsgID());

            ArrayList<MsgInfo_OADRDistributeEvent.Event> events = new ArrayList<>();
            events.add(event);
            putMsg = convertOpenADREventsToColibriPUTContent(events, bridge);

            ColibriMessage colMsg = new ColibriMessage(MsgType.PUT_DATA_VALUES, header, bridge.getColClient().getGenSendMessage().transformPOJOToXML(putMsg));

            EventType eventType = getEventTypeFromSignalType(event.getSignals().get(0).getSignalType());

            // TODO not hardcoded service URL
            String serviceURL = serviceBaseURL + "/" + eventType + "/" + "Service";

            bridge.addOpenADREvent(serviceURL, new Pair<>(event.getStartDate(), TimeDurationConverter.addDurationToDate(event.getStartDate(), event.getDurationSec())), event);


            // TODO implement later del
            //colibriMessages.add(colMsg);


            if (bridge.getColClient().getKnownServicesHashMap().get(serviceURL).isServiceObserved()) {
                bridge.getColClient().getKnownServicesHashMap().get(serviceURL).addEvent(event);
                // TODO del this colibriMessages.add(colMsg);
            }

        }
        return colibriMessages;
    }

    public PutMsg convertOpenADREventsToColibriPUTContent(List<MsgInfo_OADRDistributeEvent.Event> events, OpenADRColibriBridge bridge){
        PutMsg putMsg = new PutMsg();
        int dataPointNumber = 1;
        String serviceBaseURL = bridge.getColClient().getServiceBaseURL();

        for (MsgInfo_OADRDistributeEvent.Event event : events) {
            // create content
            Description descriptionDataValue;
            Date date = event.getStartDate();

            EventType eventType;

            if (event.getSignals().size() > 1) {
                logger.error("Only one signal per event is supported. Namely the VEN can be a direct target of a price event. Only the first signal will be processed");
            }

            MsgInfo_OADRDistributeEvent.Signal signal = event.getSignals().get(0);

            /* If there is a need to sort the intervals
            Collections.sort(signal.getIntervals(), new Comparator<Interval>() {
                @Override
                public int compare(Interval o1, Interval o2) {
                    Integer uid1 = Integer.parseInt(o1.getUid());
                    Integer uid2 = Integer.parseInt(o2.getUid());

                    return uid1.compareTo(uid2);
                }
            });
            */

            eventType = getEventTypeFromSignalType(signal.getSignalType());

            descriptionDataValue = addFirstLevelDescElemToGivenPutMsg(putMsg, serviceBaseURL, event, eventType);


            // insert for each openADR interval element the signal value and the time interval
            for (Interval interval : signal.getIntervals()) {
                // add reference for new datavalue element
                descriptionDataValue.getHasDataValue().add(new HasProperty().withRessource(serviceBaseURL + "/" + eventType + "/" + "datavalue" + dataPointNumber));
                date = addSecondLevelDescElemToGivenPutMsg(putMsg, serviceBaseURL, event.getEventID(), dataPointNumber, date, interval, eventType, signal);
                dataPointNumber++;
            }
        }
        return putMsg;
    }

    private EventType getEventTypeFromSignalType(SignalTypeEnumeratedType signalType){
        EventType eventType;

        switch (signalType) {
            case PRICE:
                eventType = EventType.PRICE;
                eventType.setMode(EventType.Mode.ABSOLUTE);
                break;
            case PRICE_RELATIVE:
                eventType = EventType.PRICE;
                eventType.setMode(EventType.Mode.RELATIVE);
                break;
            case PRICE_MULTIPLIER:
                eventType = EventType.PRICE;
                eventType.setMode(EventType.Mode.MULTIPLIER);
                break;
            case LEVEL:
                eventType = EventType.LOAD;
                eventType.setMode(EventType.Mode.ABSOLUTE);
                break;
            case SETPOINT:
                eventType = EventType.LOAD;
                eventType.setMode(EventType.Mode.ABSOLUTE);
                break;
            case DELTA:
                eventType = EventType.LOAD;
                eventType.setMode(EventType.Mode.RELATIVE);
                break;
            case MULTIPLIER:
                eventType = EventType.LOAD;
                eventType.setMode(EventType.Mode.MULTIPLIER);
                break;
            default:
                logger.error("openADR event type " + signalType + " not unknown, will not be forwared to colibri core");
                return null;
        }

        return eventType;
    }

    private Description addFirstLevelDescElemToGivenPutMsg(PutMsg putMsg, String serviceBaseURL, MsgInfo_OADRDistributeEvent.Event event, EventType eventType){
        Description descriptionDataValue = new Description();
        Description description;

        // messageID and creation date
        descriptionDataValue.setAbout(serviceBaseURL+ "/" + event.getEventID() + "/"+"datavalue");
        descriptionDataValue.getType().add(new Type().withRessource("&colibri;DataValue"));
        descriptionDataValue.getHasValue().add(new HasProperty().withRessource(serviceBaseURL+ "/" + event.getEventID()+"/"+eventType+"/"+"value1"));
        descriptionDataValue.getHasValue().add(new HasProperty().withRessource(serviceBaseURL+ "/" + event.getEventID()+"/"+eventType+"/"+"value2"));
        putMsg.getDescriptions().add(descriptionDataValue);

        // insert eventID
        description = new Description();
        description.setAbout(serviceBaseURL+ "/" + event.getEventID()+"/"+eventType+"/"+"value1");
        description.setValue(new Value().withDatatype("&xsd;string").withValue(event.getEventID()));
        description.getHasParameter().add(new HasProperty().withRessource(serviceBaseURL+"/"+eventType+"/"+"ServiceParameter1-2"));
        putMsg.getDescriptions().add(description);

        // insert creation date of the openADR distribute message
        description = new Description();
        description.setAbout(serviceBaseURL+ "/" + event.getEventID()+"/"+eventType+"/"+"value2");
        description.setValue(new Value().withDatatype("&xsd;dateTime").withValue(TimeDurationConverter.date2Ical(event.getCreatedDateTime()).toXMLFormat()));
        description.getHasParameter().add(new HasProperty().withRessource(serviceBaseURL+"/"+eventType+"/"+"ServiceParameter1-2"));
        putMsg.getDescriptions().add(description);

        return descriptionDataValue;
    }

    private Date addSecondLevelDescElemToGivenPutMsg(PutMsg putMsg, String serviceBaseURL, String eventID, int elemNumber, Date startDate, Interval interval, EventType eventType, MsgInfo_OADRDistributeEvent.Signal signal){
        Description description;

        // insert new date value element
        description = new Description();
        description.setAbout(serviceBaseURL+ "/" + eventID+"/"+eventType+"/"+"datavalue"+elemNumber);
        description.getType().add(new Type().withRessource("&colibri;DataValue"));
        description.getHasValue().add(new HasProperty().withRessource(serviceBaseURL+ "/" + eventID+"/"+eventType+"/"+"value"+elemNumber+"-1"));
        description.getHasValue().add(new HasProperty().withRessource(serviceBaseURL+ "/" + eventID+"/"+eventType+"/"+"value"+elemNumber+"-2"));
        putMsg.getDescriptions().add(description);

        // calculate signal value
        float sigValue = interval.getSignalValue();
        switch (eventType.getMode()){
            case ABSOLUTE:
                sigValue=sigValue;
                break;
            case RELATIVE:
                sigValue=sigValue + signal.getCurrentValue();
                break;
            case MULTIPLIER:
                sigValue=sigValue * signal.getCurrentValue();
                break;
        }

        // insert signal value
        description = new Description();
        description.setAbout(serviceBaseURL+ "/" + eventID+"/"+eventType+"/"+"value"+elemNumber+"-1");
        description.setValue(new Value().withDatatype("&xsd;decimal").withValue(sigValue + ""));
        description.getHasParameter().add(new HasProperty().withRessource(serviceBaseURL+"/"+eventType+"/"+"ServiceParameter1-2"));
        putMsg.getDescriptions().add(description);

        // insert time interval
        description = new Description();
        description.setAbout(serviceBaseURL+ "/" + eventID+"/"+eventType+"/"+"value"+elemNumber+"-2");
        description.setMin(new Value().withDatatype("&xsd;dateTime").withValue(TimeDurationConverter.date2Ical(startDate).toXMLFormat()));
        startDate = TimeDurationConverter.addDurationToDate(startDate, interval.getDurationSec());
        description.setMax(new Value().withDatatype("&xsd;dateTime").withValue(TimeDurationConverter.date2Ical(startDate).toXMLFormat()));
        description.getHasParameter().add(new HasProperty().withRessource(serviceBaseURL+"/"+eventType+"/"+"ServiceParameter2-2"));
        putMsg.getDescriptions().add(description);

        return startDate;
    }
}
