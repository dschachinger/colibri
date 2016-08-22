package Bridge;

import Utils.EventType;
import Utils.Pair;
import Utils.TimeDurationConverter;
import com.enernoc.open.oadr2.model.v20b.ei.SignalTypeEnumeratedType;
import openADR.OADRMsgInfo.Interval;
import openADR.OADRMsgInfo.MsgInfo_OADRDistributeEvent;
import openADR.OADRMsgInfo.OADRMsgInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semanticCore.MsgObj.ColibriMessage;
import semanticCore.MsgObj.ContentMsgObj.*;
import semanticCore.WebSocketHandling.ServiceDataConfig;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by georg on 04.07.16.
 * Objects from this class are used to bridge the openADR part with the colibri part.
 * This is only for the direction from openADR to colibri.
 */
public class OpenADRToColibri {

    private Logger logger = LoggerFactory.getLogger(OpenADRToColibri.class);

    /**
     * For a given openADR message this method returns the reply for the colibri side.
     * This method is able to handle all openADR message types which are relevant for the colibri side.
     *
     * @param msg given openADR message
     * @param bridge
     * @return list which contains all the colibri reply messages
     */
    public List<ColibriMessage> convertOpenADRMsg(OADRMsgInfo msg, OpenADRColibriBridge bridge){
        List<ColibriMessage> sendToColibriMessages = null;

        String type = msg.getMsgType();

        switch (type){
            case "oadrDistributeEvent":
                sendToColibriMessages = handle_DistributeEvent(msg, bridge);
                break;
        }

        return sendToColibriMessages;

    }

    /**
     * For a given openADR message this method returns the reply for the colibri side.
     * This method is able to handle the openADR distribute event message types.
     *
     * @param msg given openADR message
     * @param bridge
     * @return list which contains all the colibri reply messages
     */
    private List<ColibriMessage> handle_DistributeEvent(OADRMsgInfo msg, OpenADRColibriBridge bridge) {
        MsgInfo_OADRDistributeEvent con_msg = (MsgInfo_OADRDistributeEvent) msg;
        List<ColibriMessage> colibriMessages = new ArrayList<>();

        for (MsgInfo_OADRDistributeEvent.Event event : con_msg.getEvents()) {

            EventType eventType = getEventTypeFromSignalType(event.getSignals().get(0).getSignalType());

            String serviceURL = null;
            for(String key : bridge.getColClient().getServicesMap().keySet()){
                ServiceDataConfig serviceDataConfig = bridge.getColClient().getServicesMap().get(key).getServiceDataConfig();

                if(serviceDataConfig.getEventType().equals(eventType)){
                    serviceURL = serviceDataConfig.getServiceName();
                }
            }

            if(serviceURL!=null){
                bridge.addOpenADREvent(serviceURL, new Pair<>(event.getStartDate(), TimeDurationConverter.addDurationToDate(event.getStartDate(), event.getDurationSec())), event);

                if (bridge.getColClient().getServicesMap().get(serviceURL).isServiceObserved()) {
                    bridge.getColClient().getServicesMap().get(serviceURL).addEvent(event);
                }
            } else {
                logger.error("no proper service found for event type " + eventType);
            }



        }
        return colibriMessages;
    }

    /**
     * This method converts the given openADR events into the put message content format.
     * @param events given openADR events
     * @param bridge
     * @return content for a put message
     */
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

            /* If there is a need to sort the intervals use this code snippet.
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


            // To insert the signal value and the time interval for each openADR interval element.
            for (Interval interval : signal.getIntervals()) {
                // add reference for new data value element
                descriptionDataValue.getHasDataValue().add(new HasProperty().withRessource(serviceBaseURL + "/" + eventType + "/" + "datavalue" + dataPointNumber));
                date = addSecondLevelDescElemToGivenPutMsg(putMsg, serviceBaseURL, event.getEventID(), dataPointNumber, date, interval, eventType, signal);
                dataPointNumber++;
            }
        }
        return putMsg;
    }

    /**
     * This method detects which signal type was used and returns this inforamtion within an EventType object.
     * @param signalType
     * @return
     */
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
                logger.error("openADR event type " + signalType + " not unknown, will not be forwarded to colibri core");
                return null;
        }

        return eventType;
    }

    /**
     * The message of a colibri put message contains nested data values.
     * This method adds the first level data values into a given PUT message.
     * @param putMsg given put message
     * @param serviceBaseURL
     * @param event gets the information for the data values from this event
     * @param eventType
     * @return the description element of the first level data value
     */
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
        description.setValue(new Value().withDataType("&xsd;string").withValue(event.getEventID()));
        description.getHasParameter().add(new HasProperty().withRessource(serviceBaseURL+"/"+eventType+"/"+"ServiceParameter1-2"));
        putMsg.getDescriptions().add(description);

        // insert creation date of the openADR distribute message
        description = new Description();
        description.setAbout(serviceBaseURL+ "/" + event.getEventID()+"/"+eventType+"/"+"value2");
        description.setValue(new Value().withDataType("&xsd;dateTime").withValue(TimeDurationConverter.date2Ical(event.getCreatedDateTime()).toXMLFormat()));
        description.getHasParameter().add(new HasProperty().withRessource(serviceBaseURL+"/"+eventType+"/"+"ServiceParameter1-2"));
        putMsg.getDescriptions().add(description);

        return descriptionDataValue;
    }

    /**
     * The message of a colibri put message contains nested data values.
     * This method adds the second level data values into a given PUT message.
     * @param putMsg given put message
     * @param serviceBaseURL
     * @param eventID the corresponding event id
     * @param elemNumber the position number of the inserted elements
     * @param startDate start date of the openADR signal interval
     * @param interval duration of the interval
     * @param eventType
     * @param signal the corresponding openADR signal, which consists of multiple signal intervals
     * @return the end date of the openADR signal interval
     */
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
        description.setValue(new Value().withDataType("&xsd;decimal").withValue(sigValue + ""));
        description.getHasParameter().add(new HasProperty().withRessource(serviceBaseURL+"/"+eventType+"/"+"ServiceParameter1-2"));
        putMsg.getDescriptions().add(description);

        // insert time interval
        description = new Description();
        description.setAbout(serviceBaseURL+ "/" + eventID+"/"+eventType+"/"+"value"+elemNumber+"-2");
        description.setMin(new Value().withDataType("&xsd;dateTime").withValue(TimeDurationConverter.date2Ical(startDate).toXMLFormat()));
        startDate = TimeDurationConverter.addDurationToDate(startDate, interval.getDurationSec());
        description.setMax(new Value().withDataType("&xsd;dateTime").withValue(TimeDurationConverter.date2Ical(startDate).toXMLFormat()));
        description.getHasParameter().add(new HasProperty().withRessource(serviceBaseURL+"/"+eventType+"/"+"ServiceParameter2-2"));
        putMsg.getDescriptions().add(description);

        return startDate;
    }
}
