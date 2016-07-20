package semanticCore.WebSocketHandling;

import Utils.Pair;
import semanticCore.MsgObj.ColibriMessage;
import semanticCore.MsgObj.ContentMsgObj.AddMsg;
import semanticCore.MsgObj.ContentMsgObj.ServiceDescription;
import semanticCore.MsgObj.MsgType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georg on 02.07.16.
 * Objects from this class are used to handle incoming colibri messages.
 */
public class ProcessReceivedMsg {
    ColibriClient colClient;

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
                System.out.println("unkown reference id");
                return new Pair<>(false, null);
            }

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
        System.out.println(">>>>>>>handle "+MsgType.DEREGISTER + " message");
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
        System.out.println(">>>>>>>handle "+MsgType.ADD_SERVICE + " message");

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
        System.out.println(">>>>>>>handle "+MsgType.REMOVE_SERVICE + " message");

        String removeService = msg.getContent();
        System.out.println("remove " + removeService);
        if(colClient.getKnownServicesHashMap().remove(removeService) == null){
            System.out.println("service ID " + removeService + " unknown");
        } else {
            System.out.println("service ID " + removeService + " successful deleted");
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
        System.out.println(">>>>>>>handle "+MsgType.OBSERVE_SERVICE + " message");

        String observeService = msg.getContent();
        String replyStatusCode;
        System.out.println("observe " + observeService);
        if(!colClient.getKnownServicesHashMap().keySet().contains(observeService)){
            System.out.println("service ID " + observeService + " unknown");
            replyStatusCode = "500";
        } else {
            System.out.println("service ID " + observeService + " observation request processed successfully ");
            replyStatusCode = "200";
        }

        List<ColibriMessage> replies = new ArrayList<>();
        replies.add(colClient.getGenSendMessage().gen_STATUS(replyStatusCode, msg.getHeader().getMessageId()));

        if(replyStatusCode.equals("200")){
            replies.add(colClient.getGenSendMessage().gen_OBSERVE_SERVICE(colClient.getKnownServicesHashMap().get(observeService)));
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
        System.out.println(">>>>>>>handle "+MsgType.DETACH_OBSERVATION + " message");

        String detachObserveService = msg.getContent();
        String replyStatusCode;
        System.out.println("detach observe " + detachObserveService);
        if(!colClient.getObservedConnectorToColibriServices().remove(detachObserveService)){
            System.out.println("service ID " + detachObserveService + " not observed");
            replyStatusCode = "500";
        } else {
            System.out.println("service ID " + detachObserveService + " observation detached successfully ");
            replyStatusCode = "200";
        }

        List<ColibriMessage> replies = new ArrayList<>();
        replies.add(colClient.getGenSendMessage().gen_STATUS(replyStatusCode, msg.getHeader().getMessageId()));

        if(replyStatusCode.equals("200")){
            replies.add(colClient.getGenSendMessage().gen_DETACH_OBSERVATION(colClient.getKnownServicesHashMap().get(detachObserveService)));
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
        System.out.println(">>>>>>>handle "+MsgType.PUT_DATA_VALUES + " message");


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
        System.out.println(">>>>>>>handle "+MsgType.GET_DATA_VALUES + " message");

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
        System.out.println(">>>>>>>handle "+MsgType.QUERY_RESULT + " message");


        return new Pair<>(true, null);
    }

    /**
     * This method processes how to react on a given status colibri message.
     * It returns a pair. The first element indicates if the openADR part should be informed about this message.
     * The second element holds the messages which should be replied to the semantic core.
     * @param msg given status colibri message
     * @return pair object
     */
    private Pair<Boolean, List<ColibriMessage>> handle_STATUS(ColibriMessage msg){
        System.out.println(">>>>>>>handle "+MsgType.STATUS + " message");

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
                        if(colClient.getKnownServicesHashMap().get(service).equals(acceptServiceNameCoreToClient)){
                            observeServiceClientToCore = service;
                            break;
                        }
                    }

                    colClient.getObservedConnectorToColibriServices().add(observeServiceClientToCore);
                    System.out.println("service ID " + observeServiceClientToCore + " observation + follow service added successfully ");
                }
                break;
            case ADD_SERVICE:
                if(responseOK){
                    String serviceName = null;
                    String acceptServiceName = null;

                    for(ServiceDescription serviceDescription : ((AddMsg)msg.getOriginMessage().getContentObj()).getServiceDescriptions()){
                        if(serviceDescription.getAbout().contains("accept")){
                            acceptServiceName = serviceDescription.getAbout();
                        } else {
                            serviceName = serviceDescription.getAbout();
                        }
                    }

                    colClient.getKnownServicesHashMap().put(serviceName, acceptServiceName);
                }
                return new Pair<>(false, null);
        }

        return new Pair<>(false, null);
    }
}
