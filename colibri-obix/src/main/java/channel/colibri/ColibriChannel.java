package channel.colibri;

import channel.message.AtmosphereMessage;
import channel.message.colibriMessage.ColibriMessage;
import channel.message.colibriMessage.ColibriMessageMapper;
import channel.message.colibriMessage.MessageIdentifier;
import channel.message.colibriMessage.StatusCode;
import channel.message.messageObj.ColibriMessageContentCreator;
import channel.message.messageObj.PutMessageContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.obix.ObixObject;
import org.atmosphere.wasync.*;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.Configurator;
import service.TimeDurationConverter;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ColibriChannel {

    private final static Logger logger = LoggerFactory.getLogger(ColibriChannel.class);
    private final static ObjectMapper mapper = new ObjectMapper();
    private AtmosphereClient client;
    private Socket socket;
    private String connectorName;
    private String host;
    private int port;
    private Boolean registered;
    private Map<String, ColibriMessage> messagesWithoutResponse;
    private Map<String, ObixObject> observeAbleObjectsMap;
    private Map<String, ObixObject> observedObjectsMap;
    private String lastMessageReceived = "No messages from Colibri SC received.";
    private Map<String, ObixObject> observedColibriActionsMap;

    public ColibriChannel(String connectorName, String host, int port) {
        this.connectorName = connectorName;
        this.host = host;
        this.port = port;
        this.registered = false;
        client = ClientFactory.getDefault().newClient(AtmosphereClient.class);
        this.messagesWithoutResponse = new HashMap<>();
        this.observeAbleObjectsMap = new HashMap<>();
        this.observedObjectsMap = new HashMap<>();
        this.observedColibriActionsMap = new HashMap<>();
    }

    public void run() throws IOException {
        RequestBuilder request = client.newRequestBuilder()
                .method(Request.METHOD.GET)
                .uri("http://" + host + ":" + port + "/chat")
                .trackMessageLength(true)
                .encoder(new Encoder<AtmosphereMessage, String>() {
                    public String encode(AtmosphereMessage data) {
                        try {
                            return mapper.writeValueAsString(data);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .decoder(new Decoder<String, AtmosphereMessage>() {
                    public AtmosphereMessage decode(Event type, String data) {

                        data = data.trim();

                        // Padding
                        if (data.length() == 0) {
                            return null;
                        }

                        if (type.equals(Event.MESSAGE)) {
                            try {
                                return mapper.readValue(data, AtmosphereMessage.class);
                            } catch (IOException e) {
                                logger.info("Invalid message {}", data);
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                })
                .transport(Request.TRANSPORT.WEBSOCKET)
                .transport(Request.TRANSPORT.LONG_POLLING);

        socket = client.create();
        socket.on("message", new Function<AtmosphereMessage>() {
            public void on(AtmosphereMessage t) {
                if (!t.getAuthor().equals(connectorName)) {
                    try {
                        messageReceived(ColibriMessageMapper.msgToPOJO(t.getMessage()));
                    } catch (IllegalArgumentException e) {
                        lastMessageReceived = e.getMessage();
                    }
                    lastMessageReceived = t.getMessage();
                }
            }
        }).on(new Function<Throwable>() {

            public void on(Throwable t) {
                t.printStackTrace();
            }

        }).on(Event.CLOSE.name(), new Function<String>() {
            public void on(String t) {
                logger.info("Connection closed");
            }
        }).on(Event.OPEN.name(), new Function<String>() {
            public void on(String t) {
                logger.info("Connection opened");
            }
        }).open(request.build());
    }

    public void send(ColibriMessage msg) {
        System.out.println(msg);
        String tempTail = "<br> SENTMSG";
        try {
            if (!alreadySent(msg)) {
                socket.fire(new AtmosphereMessage(connectorName, msg.toString()));
                this.addMessageWithoutResponse(msg);
                //TODO: remove this line, only for testing with FAKE
                handleStatusMessagesFAKE(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        socket.close();
    }

    public Boolean getRegistered() {
        return registered;
    }

    public void setRegistered(Boolean registered) {
        this.registered = registered;
    }

    public Map<String, ColibriMessage> addMessageWithoutResponse(ColibriMessage message) {
        messagesWithoutResponse.put(message.getHeader().getId(), message);
        return messagesWithoutResponse;
    }

    public Map<String, ColibriMessage> getMessagesWithoutResponse() {
        return messagesWithoutResponse;
    }

    public void messageReceived(ColibriMessage message) {
        if (message.getMsgType().equals(MessageIdentifier.STA)) {
            handleStatusMessages(message);
        } else if (message.getMsgType().equals(MessageIdentifier.DRE)) {
            send(handleDeregisterMessage(message));
        } else if (message.getMsgType().equals(MessageIdentifier.OBS)) {
            send(handleObserveMessage(message));
        } else if (message.getMsgType().equals(MessageIdentifier.DET)) {
            send(handleDetachMessage(message));
        } else if (message.getMsgType().equals(MessageIdentifier.REM)) {
            send(handleRemoveMessage(message));
        } else if (message.getMsgType().equals(MessageIdentifier.PUT)) {
            send(handlePutMessage(message));
        }

    }

    private void handleStatusMessages(ColibriMessage message) {
        ColibriMessage requestMsg = messagesWithoutResponse.get(message.getHeader().getRefenceId());
        if (requestMsg != null) {
            if (message.getContent().getContent().contains(StatusCode.OK.toString())) {
                if (requestMsg.getMsgType().equals(MessageIdentifier.REG)) {
                    this.registered = true;
                    messagesWithoutResponse.remove(message.getHeader().getRefenceId());
                } else if (requestMsg.getMsgType().equals(MessageIdentifier.DRE)) {
                    this.registered = false;
                    messagesWithoutResponse.remove(message.getHeader().getRefenceId());
                } else if (requestMsg.getMsgType().equals(MessageIdentifier.ADD)) {
                    requestMsg.getOptionalObixObject().setAddedAsService(true);
                    messagesWithoutResponse.remove(message.getHeader().getRefenceId());
                    observeAbleObjectsMap.put(requestMsg.getOptionalObixObject().getServiceUri(),
                            requestMsg.getOptionalObixObject());
                } else if (requestMsg.getMsgType().equals(MessageIdentifier.REM)) {
                    requestMsg.getOptionalObixObject().setAddedAsService(false);
                    observeAbleObjectsMap.remove(requestMsg.getOptionalObixObject().getServiceUri());
                    messagesWithoutResponse.remove(message.getHeader().getRefenceId());
                } else if (requestMsg.getMsgType().equals(MessageIdentifier.OBS)) {
                    requestMsg.getOptionalObixObject().setObservesColibriActions(true);
                    messagesWithoutResponse.remove(message.getHeader().getRefenceId());
                    observedColibriActionsMap.put(requestMsg.getOptionalObixObject().getServiceUri(),
                            requestMsg.getOptionalObixObject());
                } else if (requestMsg.getMsgType().equals(MessageIdentifier.DET)) {
                    requestMsg.getOptionalObixObject().setObservesColibriActions(false);
                    messagesWithoutResponse.remove(message.getHeader().getRefenceId());
                    observedColibriActionsMap.remove(requestMsg.getOptionalObixObject().getServiceUri(),
                            requestMsg.getOptionalObixObject());
                }
            }
        }
    }

    //TODO: ONLY FOR TESTING PURPOSES


    private void handleStatusMessagesFAKE(ColibriMessage requestMsg) {
        if (requestMsg.getMsgType().equals(MessageIdentifier.REG)) {
            this.registered = true;
            messagesWithoutResponse.remove(requestMsg.getHeader().getRefenceId());
        } else if (requestMsg.getMsgType().equals(MessageIdentifier.DRE)) {
            this.registered = false;
            messagesWithoutResponse.remove(requestMsg.getHeader().getRefenceId());
        } else if (requestMsg.getMsgType().equals(MessageIdentifier.ADD)) {
            requestMsg.getOptionalObixObject().setAddedAsService(true);
            messagesWithoutResponse.remove(requestMsg.getHeader().getRefenceId());
            observeAbleObjectsMap.put(requestMsg.getOptionalObixObject().getServiceUri(),
                    requestMsg.getOptionalObixObject());
        } else if (requestMsg.getMsgType().equals(MessageIdentifier.REM)) {
            requestMsg.getOptionalObixObject().setAddedAsService(false);
            observeAbleObjectsMap.remove(requestMsg.getOptionalObixObject().getServiceUri());
            messagesWithoutResponse.remove(requestMsg.getHeader().getRefenceId());
        }
    }


    private ColibriMessage handleDeregisterMessage(ColibriMessage message) {
        if (message.getContent().getContentWithoutBreaksAndWhiteSpace().equals(new Configurator().getConnectorAddress())
                && this.getRegistered()) {
            for (ObixObject o : observeAbleObjectsMap.values()) {
                o.setObservedByColibri(false);
                o.setAddedAsService(false);
            }
            this.registered = false;
            return ColibriMessage.createStatusMessage(StatusCode.OK, "", message.getHeader().getId());
        }
        return ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "Connector is not registered", message.getHeader().getId());
    }

    private ColibriMessage handleObserveMessage(ColibriMessage message) {
        ObixObject temp = observeAbleObjectsMap.get(message.getContent().getContentWithoutBreaksAndWhiteSpace());
        if (temp != null) {
            temp.setObservedByColibri(true);
            observedObjectsMap.put(temp.getServiceUri(), temp);
            return ColibriMessage.createStatusMessage(StatusCode.OK, "", message.getHeader().getId());
        }
        return ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "Service cannot be observed", message.getHeader().getId());
    }

    private ColibriMessage handleDetachMessage(ColibriMessage message) {
        ObixObject temp = observedObjectsMap.get(message.getContent().getContentWithoutBreaksAndWhiteSpace());
        if (temp != null) {
            temp.setObservedByColibri(false);
            observedObjectsMap.remove(temp.getServiceUri());
            return ColibriMessage.createStatusMessage(StatusCode.OK, "", message.getHeader().getId());
        }
        return ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "Service is not observed", message.getHeader().getId());
    }

    private void handleAddMessage(ColibriMessage message) {
        //Nothing to handle for now
    }

    private ColibriMessage handleRemoveMessage(ColibriMessage message) {
        ObixObject temp = observeAbleObjectsMap.get(message.getContent().getContentWithoutBreaksAndWhiteSpace());
        if (temp != null) {
            temp.setObservedByColibri(false);
            temp.setAddedAsService(false);
            observedObjectsMap.remove(temp.getServiceUri());
            observeAbleObjectsMap.remove(temp.getServiceUri());
            return ColibriMessage.createStatusMessage(StatusCode.OK, "", message.getHeader().getId());
        }
        return ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "Service is not added and therefore cannot be removed", message.getHeader().getId());
    }

    private ColibriMessage handlePutMessage(ColibriMessage message) {
        try {
            PutMessageContent content = ColibriMessageContentCreator.getPutMessageContent(message);
            ObixObject serviceObject = observedColibriActionsMap.get(content.getServiceUri());
            boolean setParam1 = false;
            boolean setParam2 = false;
            if (serviceObject != null) {
                if (content.getDataValueUri().equals(serviceObject.getDataValueUri())) {
                    System.out.println(content.getValue1HasParameterUri());
                    System.out.println(serviceObject.getParameter1().getParameterUri());
                    if (content.getValue1HasParameterUri().equals(serviceObject.getParameter1().getParameterUri())) {
                        if (content.getValue1Uri().equals(serviceObject.getParameter1().getValueUri())) {
                            serviceObject.setValueParameter1(content.getValue1().getValue());
                            setParam1 = true;
                        }
                    }
                    if (content.getValue1HasParameterUri().equals(serviceObject.getParameter2().getParameterUri())) {
                        if (content.getValue1Uri().equals(serviceObject.getParameter2().getValueUri())) {
                            serviceObject.setValueParameter2(TimeDurationConverter.ical2Date(content.getValue2().getValue()));
                            setParam2 = true;
                        }
                    }
                    if (content.getValue2HasParameterUri().equals(serviceObject.getParameter1().getParameterUri())) {
                        if (content.getValue2Uri().equals(serviceObject.getParameter1().getValueUri())) {
                            serviceObject.setValueParameter1(content.getValue1().getValue());
                            setParam1 = true;
                        }
                    }
                    if (content.getValue2HasParameterUri().equals(serviceObject.getParameter2().getParameterUri())) {
                        if (content.getValue2Uri().equals(serviceObject.getParameter2().getValueUri())) {
                            serviceObject.setValueParameter2(TimeDurationConverter.ical2Date(content.getValue2().getValue()));
                            setParam2 = true;
                        }
                    }
                }
            }
            if (setParam1 && setParam2) {
                synchronized (serviceObject) {
                    serviceObject.setSetByColibri(true);
                    serviceObject.notify();
                }
                return ColibriMessage.createStatusMessage(StatusCode.OK, "", message.getHeader().getId());
            } else {
                return ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "PUT message to the service with this address is not possible." +
                        "Please check if the service address is correct.", message.getHeader().getId());
            }
        } catch (JAXBException e) {
            return ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "Unmarshalling PUT message failed!", message.getHeader().getId());
        }
    }


    private Boolean alreadySent(ColibriMessage message) {
        for (ColibriMessage msg : messagesWithoutResponse.values()) {
            if (msg.getMsgType().equals(message.getMsgType())) {
                if (msg.getMsgType().equals(MessageIdentifier.REG)
                        || msg.getMsgType().equals(MessageIdentifier.DRE)) {
                    return true;
                }
                if (message.getOptionalObixObject() != null && msg.getOptionalObixObject() != null) {
                    if (message.getOptionalObixObject().getServiceUri().equals(msg.getOptionalObixObject().getServiceUri())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getLastMessageReceived() {
        return lastMessageReceived;
    }

    public void setLastMessageReceived(String lastMessageReceived) {
        this.lastMessageReceived = lastMessageReceived;
    }
}