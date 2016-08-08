package Bridge;

import Utils.Main;
import Utils.Pair;
import Utils.TimeDurationConverter;
import com.enernoc.open.oadr2.model.v20b.ei.OptTypeType;
import openADR.OADRMsgInfo.MsgInfo_OADRCreatedEvent;
import openADR.OADRMsgInfo.MsgInfo_OADRDistributeEvent;
import openADR.OADRMsgInfo.OADRMsgInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semanticCore.MsgObj.ColibriMessage;
import semanticCore.MsgObj.ContentMsgObj.*;
import semanticCore.MsgObj.ContentType;
import semanticCore.MsgObj.Header;
import semanticCore.MsgObj.MsgType;
import semanticCore.WebSocketHandling.ServiceDataConfig;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.*;

/**
 * Created by georg on 04.07.16.
 * Objects from this class are used to bridge the colibri part with the openADR part.
 * This is only for the direction from colibri to openADR.
 */
public class ColibriToOpenADR {

    private Logger logger = LoggerFactory.getLogger(ColibriToOpenADR.class);

    /**
     * This method returns for a given colibri message the reply for the openADR and the colibri side.
     * This method is able to handle all colibri message types.
     *
     * @param msg given colibri message
     * @param bridge
     * @return a pair, where the first element is the colibri reply and the second element is the openADR reply.
     */
    public Pair<ColibriMessage, OADRMsgInfo> convertColibriMsg(ColibriMessage msg, OpenADRColibriBridge bridge){
        Pair<ColibriMessage, OADRMsgInfo> result = null;

        MsgType type = msg.getMsgType();

        switch (type){
            case PUT_DATA_VALUES:
                result = handle_PUT_DATA_VALUES(msg, bridge);
                break;
            case GET_DATA_VALUES:
                result = handle_GET_DATA_VALUES(msg, bridge);
                break;
            case QUERY_RESULT:
                result = handle_QUERY_RESULT(msg, bridge);
                break;
            default:
                result = new Pair<>(null, null);
                break;
        }

        return result;

    }

    /**
     * This method returns for a given colibri message the reply for the openADR and the colibri side.
     * This method is able to handle the colibri put messages.
     *
     * @param msg given colibri message
     * @param bridge
     * @return a pair, where the first element is the colibri reply and the second element is the openADR reply.
     */
    private Pair<ColibriMessage, OADRMsgInfo> handle_PUT_DATA_VALUES(ColibriMessage msg, OpenADRColibriBridge bridge){
        logger.info(">>>>>>>handle "+MsgType.PUT_DATA_VALUES + " message");

        StringReader contentReader = new StringReader(msg.getContent());

        MsgInfo_OADRCreatedEvent createdEvent = null;
        String statusCode = "200";

        try {
            Unmarshaller unmarshaller = bridge.getColClient().getJaxbUnmarshaller();
            PutMsg putMsg = (PutMsg)unmarshaller.unmarshal(contentReader);

            List<Pair<String, String>> dataValues = new ArrayList<>();
            Map<String, Pair<String, String>> parameter = new HashMap<>();

            for(Description description : putMsg.getDescriptions()){
                if(description.getHasValue().size() == 2){
                    dataValues.add(new Pair<String, String>(description.getHasValue().get(0).getResource(),
                            description.getHasValue().get(1).getResource().trim()));
                }

                if(description.getHasParameter().size() == 1){
                    parameter.put(description.getAbout(),
                            new Pair<String, String>(description.getHasParameter().get(0).getResource(),
                            description.getValue().getValue().trim()
                    ));
                }
            }

            Map<String, Boolean> eventStatus = new HashMap<>();

            for(Pair<String, String> dataValue : dataValues){
                String eventID;
                boolean status;

                String parameterValue1;
                String parameterValue2;

                String parameterURL1 = parameter.get(dataValue.getFst()).getFst();
                String parameterURL2 = parameter.get(dataValue.getSnd()).getFst();

                Boolean normalOrder = null;

                for(String serviceURL : bridge.getColClient().getServicesMap().keySet()){
                    ServiceDataConfig followServiceDataConfig = bridge.getColClient().getServicesMap().get(serviceURL).getServiceDataConfig().getFollowUpServiceDataConfig();
                    for(ServiceDataConfig.Parameter configParameter : followServiceDataConfig.getParameters()){
                        for(String type : configParameter.getTypes()){
                            if(type.equals("&colibri;InformationParameter")){
                                String currentMsgIDParameter = configParameter.getName();
                                if(parameterURL1.equals(currentMsgIDParameter)){
                                    normalOrder = true;
                                } else if(parameterURL2.equals(currentMsgIDParameter)){
                                    normalOrder = false;
                                }
                            }
                        }
                    }
                }

                parameterValue1 = parameter.get(dataValue.getFst()).getSnd();
                parameterValue2 = parameter.get(dataValue.getSnd()).getSnd();

                if(normalOrder){
                    eventID = parameterValue1;
                    status = Boolean.parseBoolean(parameterValue2);
                } else {
                    eventID = parameterValue2;
                    status = Boolean.parseBoolean(parameterValue1);
                }

                eventStatus.put(eventID, status);

            }

            createdEvent = new MsgInfo_OADRCreatedEvent();

            for(String key : eventStatus.keySet()){
                Pair<Pair<Date, Date>, MsgInfo_OADRDistributeEvent.Event> elem = bridge.getOpenADREvent(key);
                if( elem != null){
                    MsgInfo_OADRDistributeEvent.Event event = elem.getSnd();
                    logger.info("event: " + key + " status " + eventStatus.get(key));
                    MsgInfo_OADRCreatedEvent.EventResponse eventResponse = createdEvent.getNewEventResponse();
                    eventResponse.setEventID(key);
                    eventResponse.setOptType(eventStatus.get(key)? OptTypeType.OPT_IN:OptTypeType.OPT_OUT);
                    eventResponse.setModificationNumber(event.getModificationNumber());
                    eventResponse.setRequestID(event.getRequestID());
                    event.setCreatedEventToVTNSent(true);
                    createdEvent.getEventResponses().add(eventResponse);
                } else {
                    logger.error("wrong service url or eventID!");
                    statusCode = "500";
                }

            }

        } catch (JAXBException e) {
            logger.error("invalid syntax!");
            statusCode = "300";
        }

        ColibriMessage reply = bridge.getColClient().getGenSendMessage().gen_STATUS(statusCode, msg.getHeader().getMessageId());

        if(!createdEvent.getEventResponses().isEmpty()){
            return new Pair<ColibriMessage, OADRMsgInfo>(reply, createdEvent);
        } else {
            return new Pair<ColibriMessage, OADRMsgInfo>(reply, null);
        }


    }

    /**
     * This method returns for a given colibri message the reply for the openADR and the colibri side.
     * This method is able to handle the colibri get messages.
     *
     * @param msg given colibri message
     * @param bridge
     * @return a pair, where the first element is the colibri reply and the second element is the openADR reply.
     */
    private Pair<ColibriMessage, OADRMsgInfo> handle_GET_DATA_VALUES(ColibriMessage msg, OpenADRColibriBridge bridge){
        logger.info(">>>>>>>handle "+MsgType.GET_DATA_VALUES + " message");

        boolean onlyOneEventNeeded = false;

        if(!msg.getContent().matches("[^\\?]+\\??(\\&?(from=|to=)\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z){0,2}")){
            logger.error("malformed url: " + msg.getContent());
            ColibriMessage reply = bridge.getColClient().getGenSendMessage().gen_STATUS("300", msg.getHeader().getMessageId());
            return new Pair<ColibriMessage, OADRMsgInfo>(reply, null);
        }

        String[] serviceURLParts = msg.getContent().split("\\?");


        String serviceURL = serviceURLParts[0];
        Date fromDate = null;
        Date toDate = null;

        if(serviceURLParts.length == 2){
            String[] dates = serviceURLParts[1].split("&");

            for(String dateStr : dates){
                Date date = TimeDurationConverter.ical2Date(dateStr.split("=")[1]);

                if(dateStr.startsWith("from=")){
                    fromDate = date;
                }
                if(dateStr.startsWith("to=")){
                    toDate = date;
                }
            }
        }

        if(fromDate == null && toDate == null){
            fromDate= Main.testDate;
            onlyOneEventNeeded = true;
        }

        if(!bridge.getColClient().getServicesMap().keySet().contains(serviceURL)){
            logger.error("service URL " + serviceURL + " unknown");
            ColibriMessage reply = bridge.getColClient().getGenSendMessage().gen_STATUS("500", msg.getHeader().getMessageId());
            return new Pair<ColibriMessage, OADRMsgInfo>(reply, null);
        }

        List<MsgInfo_OADRDistributeEvent.Event> events= bridge.getOpenADREvents(serviceURL, new Pair<Date, Date>(fromDate, toDate));
        if(onlyOneEventNeeded && !events.isEmpty()){
            events = events.subList(0,1);
        }
        PutMsg putMsgContent = bridge.getOpenADRToColibri().convertOpenADREventsToColibriPUTContent(events, bridge);

        Header header = new Header();
        header.setDate(new Date());
        header.setContentType(ContentType.APPLICATION_RDF_XML);
        header.setMessageId(bridge.getColClient().getGenSendMessage().getUniqueMsgID());
        header.setReferenceId(msg.getHeader().getMessageId());

        String strContent;
        if(!putMsgContent.getDescriptions().isEmpty()){
            strContent = bridge.getColClient().getGenSendMessage().transformPOJOToXML(putMsgContent);
        } else {
            strContent = "";
        }

        ColibriMessage reply = new ColibriMessage(MsgType.PUT_DATA_VALUES, header, strContent);

        logger.info("reply PUT message with service URL " + serviceURL );
        return new Pair<>(reply, null);
    }

    /**
     * This method returns for a given colibri message the reply for the openADR and the colibri side.
     * This method is able to handle the colibri query result messages.
     *
     * @param msg given colibri message
     * @param bridge
     * @return a pair, where the first element is the colibri reply and the second element is the openADR reply.
     */
    private Pair<ColibriMessage, OADRMsgInfo> handle_QUERY_RESULT(ColibriMessage msg, OpenADRColibriBridge bridge){
        logger.info(">>>>>>>handle "+MsgType.QUERY_RESULT + " message");

        QueryResult queryResult = bridge.getColClient().getProcessMessage().convertQueryResult(msg, null);

        List<QueryResult> queryResults = bridge.getOpenADRReportData().get(msg.getOriginMessage().getContent());

        if(queryResults != null){
            queryResults.add(queryResult);
        }

        return new Pair<>(null, null);
    }

}
