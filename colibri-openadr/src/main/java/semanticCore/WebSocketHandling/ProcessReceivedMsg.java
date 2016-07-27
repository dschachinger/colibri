package semanticCore.WebSocketHandling;

import Utils.Pair;
import Utils.TimeDurationConverter;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import semanticCore.MsgObj.ColibriMessage;
import semanticCore.MsgObj.ContentMsgObj.AddMsg;
import semanticCore.MsgObj.ContentMsgObj.QueryResult;
import semanticCore.MsgObj.ContentMsgObj.Result;
import semanticCore.MsgObj.ContentMsgObj.ServiceDescription;
import semanticCore.MsgObj.MsgType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by georg on 02.07.16.
 * Objects from this class are used to handle incoming colibri messages.
 */
public class ProcessReceivedMsg {
    private ColibriClient colClient;

    private Logger logger = LoggerFactory.getLogger(ProcessReceivedMsg.class);

    public ProcessReceivedMsg(ColibriClient colClient){
        this.colClient = colClient;
    }

    /**
     * This method is called to process all incoming messages independent of the message type.
     * The return value is a pair. The first element indicates if the openADR part should be informed about this message.
     * The second element holds the messages which should be replied to the semantic core.
     * @param msg
     * @return
     */
    public Pair<Boolean, List<ColibriMessage>> processColMsg(ColibriMessage msg){
        MsgType type = msg.getMsgType();
        boolean sendToOpenADR = false;

        ColibriMessage originMsg = null;
        if(msg.getHeader().getReferenceId() != null){
            originMsg = colClient.getSendedMsgToColCore().get(msg.getHeader().getReferenceId());
            /* TODO uncomment if (originMsg == null){
                logger.error("unkown reference id");
                return new Pair<>(false, null);
            }
            */

            if(!(msg.getMsgType().equals(MsgType.STATUS) &&
                    (originMsg.getMsgType().equals(MsgType.GET_DATA_VALUES) ||
                    originMsg.getMsgType().equals(MsgType.QUERY)))){
                colClient.getSendedMsgToColCore().remove(msg.getHeader().getReferenceId());
            }

            msg.setOriginMessage(originMsg);
        }

        Pair<Boolean, List<ColibriMessage>> result = null;
        
        switch (type){
            case DEREGISTER:
                result = handle_DEREGISTER(msg);
                break;
            case ADD_SERVICE:
                result = handle_ADD_SERVICE(msg);
                break;
            case REMOVE_SERVICE:
                result = handle_REMOVE_SERVICE(msg);
                break;
            case OBSERVE_SERVICE:
                result = handle_OBSERVE_SERVICE(msg);
                break;
            case DETACH_OBSERVATION:
                result = handle_DETACH_OBSERVATION(msg);
                break;
            case PUT_DATA_VALUES:
                result = handle_PUT_DATA_VALUES(msg);
                break;
            case GET_DATA_VALUES:
                result = handle_GET_DATA_VALUES(msg);
                break;
            case QUERY_RESULT:
                result = handle_QUERY_RESULT(msg);
                break;
            case STATUS:
                result = handle_STATUS(msg);
                break;
        }

        return result;

    }

    /**
     * This method processes how to react on a given deregister colibri message.
     * It returns a pair. The first element indicates if the openADR part should be informed about this message.
     * The second element holds the messages which should be replied to the semantic core.
     * @param msg given deregister colibri message
     * @return pair object
     */
    private Pair<Boolean, List<ColibriMessage>> handle_DEREGISTER(ColibriMessage msg){
        logger.info("handle "+MsgType.DEREGISTER + " message");
        String replyStatusCode;
        String receivedRegisteredID = msg.getContent();

        if(WebSocketConInfo.getRegRegisteredDescriptionAbout().equals(receivedRegisteredID)){
            replyStatusCode = "200";
        } else {
            replyStatusCode = "500";
        }
        WebSocketConInfo.getRegRegisteredDescriptionAbout();

        colClient.setRegistered(false);

        List<ColibriMessage> replies = new ArrayList<>();
        replies.add(colClient.getGenSendMessage().gen_STATUS(replyStatusCode, msg.getHeader().getMessageId()));

        return new Pair<>(true, replies);
    }

    /**
     * This method processes how to react on a given add service colibri message.
     * It returns a pair. The first element indicates if the openADR part should be informed about this message.
     * The second element holds the messages which should be replied to the semantic core.
     * @param msg given add service colibri message
     * @return pair object
     */
    private Pair<Boolean, List<ColibriMessage>> handle_ADD_SERVICE(ColibriMessage msg){
        logger.info("handle "+MsgType.ADD_SERVICE + " message");

        return new Pair<>(true, null);
    }

    /**
     * This method processes how to react on a given remove service colibri message.
     * It returns a pair. The first element indicates if the openADR part should be informed about this message.
     * The second element holds the messages which should be replied to the semantic core.
     * @param msg given remove service colibri message
     * @return pair object
     */
    private Pair<Boolean, List<ColibriMessage>> handle_REMOVE_SERVICE(ColibriMessage msg){
        logger.info("handle "+MsgType.REMOVE_SERVICE + " message");

        String removeService = msg.getContent();
        logger.info("remove service " + removeService);
        if(colClient.getKnownServicesHashMap().remove(removeService) == null){
            logger.error("service ID " + removeService + " unknown");
        } else {
            logger.info("service ID " + removeService + " successful deleted");
        }

        return new Pair<>(true, null);
    }

    /**
     * This method processes how to react on a given observe service colibri message.
     * It returns a pair. The first element indicates if the openADR part should be informed about this message.
     * The second element holds the messages which should be replied to the semantic core.
     * @param msg given observe service colibri message
     * @return pair object
     */
    private Pair<Boolean, List<ColibriMessage>> handle_OBSERVE_SERVICE(ColibriMessage msg){
        logger.info("handle "+MsgType.OBSERVE_SERVICE + " message");


        String replyStatusCode;
        String observeService = "";

        if(!msg.getContent().matches("[^\\?]+\\??(freq=(\\d{2}:\\d{2}:\\d{2}Z|P((\\d+)Y)?((\\d+)M)?((\\d+)D)?T?((\\d+)H)?((\\d+)M)?((\\d+)S)?))?")){
            logger.error("malformed url: " + msg.getContent());
            replyStatusCode = "300";
        } else {
            String[] parts = msg.getContent().split("\\?");
            observeService = parts[0];
            String parameter = parts[1].split("=")[1];

            logger.info("observe " + observeService + "parameter: " + parameter);

            if(!colClient.getKnownServicesHashMap().keySet().contains(observeService)){
                logger.error("service ID " + observeService + " unknown");
                replyStatusCode = "500";
            } else {
                logger.info("service ID " + observeService + " observation request processed successfully ");
                ServiceHandler serviceHandler = colClient.getKnownServicesHashMap().get(observeService);

                if(parameter.matches("P((\\d+)Y)?((\\d+)M)?((\\d+)D)?T?((\\d+)H)?((\\d+)M)?((\\d+)S)?")){
                    serviceHandler.setIntervalDurationSec(TimeDurationConverter.xCal2Seconds(parameter));
                    logger.info("parsed interval length: " + serviceHandler.getIntervalDurationSec());
                } else if(parameter.matches("\\d{2}:\\d{2}:\\d{2}Z")){
                    serviceHandler.setSendTime(TimeDurationConverter.xmlTimeToDateObj(parameter));
                    logger.info("parsed sending time: " + serviceHandler.getSendTime());
                }
                replyStatusCode = "200";
            }
        }

        List<ColibriMessage> replies = new ArrayList<>();
        replies.add(colClient.getGenSendMessage().gen_STATUS(replyStatusCode, msg.getHeader().getMessageId()));

        if(replyStatusCode.equals("200")){
            replies.add(colClient.getGenSendMessage().gen_OBSERVE_SERVICE(colClient.getKnownServicesHashMap().get(observeService).getFollowService()));
        }
        return new Pair<>(true, replies);
    }

    /**
     * This method processes how to react on a given detach observation colibri message.
     * It returns a pair. The first element indicates if the openADR part should be informed about this message.
     * The second element holds the messages which should be replied to the semantic core.
     * @param msg given detach observation colibri message
     * @return pair object
     */
    private Pair<Boolean, List<ColibriMessage>> handle_DETACH_OBSERVATION(ColibriMessage msg){
        logger.info("handle "+MsgType.DETACH_OBSERVATION + " message");

        String detachObserveService = msg.getContent();
        String replyStatusCode;
        logger.info("detach observe " + detachObserveService);
        if(colClient.getKnownServicesHashMap().get(detachObserveService) == null ||
                !colClient.getKnownServicesHashMap().get(detachObserveService).isServiceObserved()){
            logger.error("service ID " + detachObserveService + " not observed");
            replyStatusCode = "500";
        } else {
            colClient.getKnownServicesHashMap().get(detachObserveService).setServiceObserved(false);
            logger.info("service ID " + detachObserveService + " observation detached successfully ");
            replyStatusCode = "200";
        }

        List<ColibriMessage> replies = new ArrayList<>();
        replies.add(colClient.getGenSendMessage().gen_STATUS(replyStatusCode, msg.getHeader().getMessageId()));

        if(replyStatusCode.equals("200")){
            replies.add(colClient.getGenSendMessage().gen_DETACH_OBSERVATION(colClient.getKnownServicesHashMap().get(detachObserveService).getFollowService()));
        }

        return new Pair<>(true, replies);
    }

    /**
     * This method processes how to react on a given put colibri message.
     * It returns a pair. The first element indicates if the openADR part should be informed about this message.
     * The second element holds the messages which should be replied to the semantic core.
     * @param msg given put colibri message
     * @return pair object
     */
    private Pair<Boolean, List<ColibriMessage>> handle_PUT_DATA_VALUES(ColibriMessage msg){
        logger.info("handle "+MsgType.PUT_DATA_VALUES + " message");


        return new Pair<>(true, null);
    }

    /**
     * This method processes how to react on a given get colibri message.
     * It returns a pair. The first element indicates if the openADR part should be informed about this message.
     * The second element holds the messages which should be replied to the semantic core.
     * @param msg given get colibri message
     * @return pair object
     */
    private Pair<Boolean, List<ColibriMessage>> handle_GET_DATA_VALUES(ColibriMessage msg){
        logger.info("handle "+MsgType.GET_DATA_VALUES + " message");

        return new Pair<>(true, null);
    }

    /**
     * This method processes how to react on a given query result colibri message.
     * It returns a pair. The first element indicates if the openADR part should be informed about this message.
     * The second element holds the messages which should be replied to the semantic core.
     * @param msg given query result colibri message
     * @return pair object
     */
    private Pair<Boolean, List<ColibriMessage>> handle_QUERY_RESULT(ColibriMessage msg){
        logger.info("handle "+MsgType.QUERY_RESULT + " message");

/* TODO    insert    if(!msg.getOriginMessage().getMsgType().equals(MsgType.QUERY)){
            logger.error("wrong origin message type for query result message");
            return new Pair<>(false, null);
        }
*/
        Pair<Boolean, List<ColibriMessage>> result;

        switch (msg.getHeader().getContentType()){
            case "application/sparql-result+json":
                result = handle_JSON_QUERY_RESULT(msg);
                break;
            case "application/sparql-result+xml":
                result = handle_XML_QUERY_RESULT(msg);
                break;

        }




        return new Pair<>(true, null);
    }

    private Pair<Boolean, List<ColibriMessage>> handle_JSON_QUERY_RESULT(ColibriMessage msg){
        logger.info("handle json");

        JsonElement jsonElement = new JsonParser().parse(msg.getContent());
        JsonObject jsonObject = jsonElement.getAsJsonObject();


        QueryResult queryResult = null;

        if(jsonObject.has("results")){
            JsonObject head = jsonObject.getAsJsonObject("head");
            JsonArray vars = head.getAsJsonArray("vars");


            queryResult = new QueryResult(false);

            for(JsonElement var : vars){
                String varStr = var.getAsString();
                queryResult.addProperty(varStr);
            }

            for(JsonElement jResult : jsonObject.getAsJsonObject("results").getAsJsonArray("bindings")){
                JsonObject tupelObj = jResult.getAsJsonObject();
                Result result = new Result();
                for(String property : queryResult.getProperties()){
                    JsonObject tripel = tupelObj.getAsJsonObject(property);
                    String propValue = tripel.getAsJsonPrimitive("value").getAsString();
                    String propType = tripel.getAsJsonPrimitive("type").getAsString();

                    result.addBinding(property, propValue, propType);

                }
                queryResult.addTupel(result);
            }



        } else if(jsonObject.has("boolean")){
            queryResult = new QueryResult(true);
            boolean value = jsonObject.getAsJsonPrimitive("boolean").getAsBoolean();
            queryResult.setASKQueryResult(value);
        }

        // TODO think about how to react on it
        if(queryResult != null){
            logger.info(queryResult.toString());
        }

        return new Pair<>(false, null);
    }

    private Pair<Boolean, List<ColibriMessage>> handle_XML_QUERY_RESULT(ColibriMessage msg){
        logger.info("handle xml");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document doc = null;
        try {
            doc = db.parse(new InputSource(new StringReader(msg.getContent())));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        doc.getDocumentElement().normalize();


        QueryResult queryResult = new QueryResult(false);


        NodeList xmlPropList = doc.getElementsByTagName("variable");

        for (int i = 0; i < xmlPropList.getLength(); i++) {
            Node nProperty = xmlPropList.item(i);
            Element eElement = (Element) nProperty;
            String property = eElement.getAttribute("name");
            queryResult.addProperty(property);
        }

        NodeList xmlResults = doc.getElementsByTagName("result");

        for (int i = 0; i < xmlResults.getLength(); i++) {
            Node nResult = xmlResults.item(i);
            Element eResult = (Element) nResult;

            Result result = new Result();

            NodeList xmlBindings = eResult.getElementsByTagName("binding");

            for (int j = 0; j < xmlBindings.getLength(); j++) {
                Node nBinding = xmlBindings.item(j);
                Element eBinding = (Element) nBinding;
                NodeList nBindingChildNodes = nBinding.getChildNodes();
                for (int k = 0; k < nBindingChildNodes.getLength(); k++) {
                    Node node = nBindingChildNodes.item(k);

                    //element nodes
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        NodeList nodeList = node.getChildNodes();
                        if (nodeList != null) {
                            for (int l = 0; l < nodeList.getLength(); l++) {
                                Node a = nodeList.item(l);
                                //text nodes
                                if (a.getNodeType() == Node.TEXT_NODE && !a.getTextContent().trim().isEmpty()) {
                                    Element element = (Element) node;
                                    String property =  eBinding.getAttribute("name");
                                    String type = node.getNodeName();
                                    // if needed
                                    String dataType = element.getAttribute("datatype");
                                    String value = a.getTextContent().trim();

                                    result.addBinding(property, value, type);
                                }
                            }
                        }
                    }
                }
            }

            queryResult.addTupel(result);

        }

        // TODO think about how to react on it
        if(queryResult != null){
            logger.info(queryResult.toString());
        }

        return new Pair<>(false, null);
    }

    /**
     * This method processes how to react on a given status colibri message.
     * It returns a pair. The first element indicates if the openADR part should be informed about this message.
     * The second element holds the messages which should be replied to the semantic core.
     * @param msg given status colibri message
     * @return pair object
     */
    private Pair<Boolean, List<ColibriMessage>> handle_STATUS(ColibriMessage msg){
        logger.info("handle "+MsgType.STATUS + " message");

        boolean responseOK = msg.getContent().startsWith("200");

        switch (msg.getOriginMessage().getMsgType()){
            case REGISTER:
                colClient.setRegistered(responseOK);
                return new Pair<>(false, null);
            case DEREGISTER:
                colClient.setRegistered(!responseOK);
                colClient.successfulDeregisteredGoOnWithTermination();
                return new Pair<>(false, null);
            case OBSERVE_SERVICE:
                if(responseOK){
                    String observeServiceClientToCore = null;
                    String acceptServiceNameCoreToClient = msg.getOriginMessage().getContent();

                    for(String service : colClient.getKnownServicesHashMap().keySet()){
                        if(colClient.getKnownServicesHashMap().get(service).getFollowService().equals(acceptServiceNameCoreToClient)){
                            observeServiceClientToCore = service;
                            break;
                        }
                    }

                    colClient.getKnownServicesHashMap().get(observeServiceClientToCore).setServiceObserved(true);
                    colClient.getKnownServicesHashMap().get(observeServiceClientToCore).start();
                    logger.info("service ID " + observeServiceClientToCore + " observation + follow service added successfully ");
                }
                break;
            case ADD_SERVICE:
                if(responseOK){
                    String serviceName = null;
                    String acceptServiceName = null;

                    for(ServiceDescription serviceDescription : ((AddMsg)msg.getOriginMessage().getContentObj()).getServiceDescriptions()){
                        // TODO besser checken mit serviceConfig
                        if(serviceDescription.getAbout().contains("accept")){
                            acceptServiceName = serviceDescription.getAbout();
                        } else {
                            serviceName = serviceDescription.getAbout();
                        }
                    }

                    colClient.getKnownServicesHashMap().get(serviceName).setServiceAdded(true);
                }
                return new Pair<>(false, null);
        }

        return new Pair<>(false, null);
    }
}
