package semanticCore.WebSocketHandling;

import Utils.Pair;
import Utils.TimeDurationConverter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
            if (originMsg == null){
                logger.error("unkown reference id");
                return new Pair<>(false, null);
            }

            if(!(msg.getMsgType().equals(MsgType.STATUS) &&
                    (originMsg.getMsgType().equals(MsgType.GET_DATA_VALUES) ||
                    originMsg.getMsgType().equals(MsgType.QUERY)))){
                colClient.getSendedMsgToColCore().remove(msg.getHeader().getReferenceId());
            }

            msg.setOriginMessage(originMsg);
        }

        Pair<Boolean, List<ColibriMessage>> result = new Pair<>(false, null);
        
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
            default:
                logger.error(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\nunsupported message received\n" + msg.toMsgString());
                logger.error("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
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
        if(colClient.getServicesMap().remove(removeService) == null){
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
            boolean noParameter = parts.length == 1;

            logger.info("observe " + observeService + (noParameter?"":"parameter: " + parts[1]));

            if(!colClient.getServicesMap().keySet().contains(observeService)){
                logger.error("service ID " + observeService + " unknown");
                replyStatusCode = "500";
            } else {
                logger.info("service ID " + observeService + " observation request processed successfully ");
                ServiceHandler serviceHandler = colClient.getServicesMap().get(observeService);

                if(!noParameter){
                    String parameter = parts[1].split("=")[1];
                    if(parameter.matches("P((\\d+)Y)?((\\d+)M)?((\\d+)D)?T?((\\d+)H)?((\\d+)M)?((\\d+)S)?")){
                        serviceHandler.setIntervalDurationSec(TimeDurationConverter.xCal2Seconds(parameter));
                        logger.info("parsed interval length: " + serviceHandler.getIntervalDurationSec());
                    } else if(parameter.matches("\\d{2}:\\d{2}:\\d{2}Z")){
                        serviceHandler.setSendTime(TimeDurationConverter.xmlTimeToDateObj(parameter));
                        logger.info("parsed sending time: " + serviceHandler.getSendTime());
                    }
                }

                replyStatusCode = "200";
            }
        }

        List<ColibriMessage> replies = new ArrayList<>();
        replies.add(colClient.getGenSendMessage().gen_STATUS(replyStatusCode, msg.getHeader().getMessageId()));

        if(replyStatusCode.equals("200")){
            replies.add(colClient.getGenSendMessage().gen_OBSERVE_SERVICE(colClient.getServicesMap().get(observeService).getFollowService()));
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
        if(colClient.getServicesMap().get(detachObserveService) == null ||
                !colClient.getServicesMap().get(detachObserveService).isServiceObserved()){
            logger.error("service ID " + detachObserveService + " not observed");
            replyStatusCode = "500";
        } else {
            colClient.getServicesMap().get(detachObserveService).changeServiceObservedStatus(false);
            logger.info("service ID " + detachObserveService + " observation detached successfully ");
            replyStatusCode = "200";
        }

        List<ColibriMessage> replies = new ArrayList<>();
        replies.add(colClient.getGenSendMessage().gen_STATUS(replyStatusCode, msg.getHeader().getMessageId()));

        if(replyStatusCode.equals("200")){
            replies.add(colClient.getGenSendMessage().gen_DETACH_OBSERVATION(colClient.getServicesMap().get(detachObserveService).getFollowService()));
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

        if(!msg.getOriginMessage().getMsgType().equals(MsgType.QUERY)){
            logger.error("wrong origin message type for query result message");
            return new Pair<>(false, null);
        }


        List<String> reqProperties = new ArrayList<>();

        String originContent = msg.getOriginMessage().getContent().replace("\n", " ").replace("\r", " ");

        int index = originContent.toLowerCase().indexOf("select") + "select".length();
        int maxIndex = originContent.toLowerCase().indexOf("where");
        while (index != -1 && maxIndex != -1 && index < maxIndex){
             index++;

            switch (originContent.charAt(index)){
                case '?': {
                    index = originContent.indexOf("?", index);
                    if (index < 0) {
                        break;
                    }
                    // shift index to beginning of the property name
                    index++;

                    int endIndex = originContent.indexOf(" ", index);

                    if (index < maxIndex) {
                        String property = originContent.substring(index, endIndex);
                        index = index + property.length();
                        reqProperties.add(property.trim());
                    }
                }
                    break;
                case '(': {
                    String part = originContent.substring(index,maxIndex);
                    int endIndex = endOuterBracketIndex(part);
                    if(endIndex > 0){
                        String[] result = part.substring(0,endIndex).split("\\s*[\\(\\)]+\\s*");
                        String lastElem = result[result.length-1];
                        String property = lastElem.substring(lastElem.lastIndexOf("?")+1).trim();
                        reqProperties.add(property);
                        index = index+endIndex;
                    } else {
                        index = -1;
                    }
                }
                    break;
                default:
                    break;
            }
        }

        logger.info("parsed properties: " + reqProperties);

        Pair<Boolean, List<ColibriMessage>> reactMessages = new Pair<>(true, null);
        QueryResult queryResult = convertQueryResult(msg, reactMessages);

        // check if the received query result contains all the requested properties
        List<String> recProp = queryResult.getProperties();
        boolean error = false;
        if(recProp.size() == reqProperties.size()){
            for(String reqProp : reqProperties){
                if(!recProp.contains(reqProp)){
                    error = true;
                    break;
                }
            }
        } else {
            error = true;
        }

        if(error){
            logger.error("result set does not contain all requested properties");
        } else {
            logger.info("result set contains all requested properties");
        }


        // TODO how to react on it depends on the purpose of the query
        return reactMessages;
    }

    private int endOuterBracketIndex(String string){
        int count = 0;
        for(int i = 0; i < string.length(); i++){
            char sign = string.charAt(i);
            if(sign == '('){
                count++;
            }
            if(sign == ')'){
                count--;
            }
            if(count == 0){
                return i;
            }
        }
        return -1;
    }

    public QueryResult convertQueryResult(ColibriMessage msg, Pair<Boolean, List<ColibriMessage>> reactMessages){
        QueryResult queryResult = null;
        switch (msg.getHeader().getContentType()){
            case "application/sparql-result+json":
                queryResult = handle_JSON_QUERY_RESULT(msg, reactMessages);
                break;
            case "application/sparql-result+xml":
                queryResult = handle_XML_QUERY_RESULT(msg, reactMessages);
                break;

        }
        return queryResult;
    }

    private QueryResult handle_JSON_QUERY_RESULT(ColibriMessage msg, Pair<Boolean, List<ColibriMessage>> reactMessages){
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

        if(queryResult != null){
            logger.info(queryResult.toString());
        }

        return queryResult;
    }

    private QueryResult handle_XML_QUERY_RESULT(ColibriMessage msg, Pair<Boolean, List<ColibriMessage>> reactMessages){
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

        if(queryResult != null){
            logger.info(queryResult.toString());
        }

        return queryResult;
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

                    for(String service : colClient.getServicesMap().keySet()){
                        if(colClient.getServicesMap().get(service).getFollowService().equals(acceptServiceNameCoreToClient)){
                            observeServiceClientToCore = service;
                            break;
                        }
                    }

                    colClient.getServicesMap().get(observeServiceClientToCore).changeServiceObservedStatus(true);
                    logger.info("service ID " + observeServiceClientToCore + " observation + follow service added successfully ");
                }
                break;
            case ADD_SERVICE:
                if(responseOK){
                    AddMsg addMsg = ((AddMsg)msg.getOriginMessage().getContentObj());

                    String normalServiceName = addMsg.getNormalServiceDescriptions().getAbout();
                    String acceptServiceName = addMsg.getAcceptServiceDescriptions().getAbout();

                    colClient.getServicesMap().get(normalServiceName).setServiceAdded(true);
                }
                break;
            case UPDATE:
                logger.info("colibri core successful received and processed update message " + msg.getOriginMessage().getHeader().getMessageId());
                break;
        }

        return new Pair<>(false, null);
    }
}
