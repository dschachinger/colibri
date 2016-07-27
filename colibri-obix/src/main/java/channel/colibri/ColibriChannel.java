package channel.colibri;

import channel.message.AtmosphereMessage;
import channel.message.colibriMessage.ColibriMessage;
import channel.message.colibriMessage.ColibriMessageMapper;
import channel.message.colibriMessage.MessageIdentifier;
import channel.message.colibriMessage.StatusCode;
import channel.message.messageObj.ColibriMessageContentCreator;
import channel.message.messageObj.PutMessageContent;
import channel.obix.PutExecutionTask;
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
import java.text.ParseException;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private Map<String, ObixObject> requestedGetMessageMap;
    private String lastMessageReceived = "No messages from Colibri Semantic Core received.";
    private Map<String, ObixObject> observedColibriActionsMap;
    private ExecutorService executor;
    private List<ResendMessageTask> waitingForStatusMessagesTasks;
    private Map<String, Timer> runningTimers;

    public ColibriChannel(String connectorName, String host, int port) {
        this.connectorName = connectorName;
        this.host = host;
        this.port = port;
        this.registered = false;
        this.client = ClientFactory.getDefault().newClient(AtmosphereClient.class);
        this.messagesWithoutResponse = Collections.synchronizedMap(new HashMap<>());
        this.observeAbleObjectsMap = Collections.synchronizedMap(new HashMap<>());
        this.observedObjectsMap = Collections.synchronizedMap(new HashMap<>());
        this.observedColibriActionsMap = Collections.synchronizedMap(new HashMap<>());
        this.requestedGetMessageMap = Collections.synchronizedMap(new HashMap<>());
        this.waitingForStatusMessagesTasks = Collections.synchronizedList(new ArrayList<>());
        this.runningTimers = new HashMap<>();
        this.executor = Executors.newCachedThreadPool();
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
            @Override
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
        logger.info("Send:" + msg.toString());
        try {
          //  if (!alreadySent(msg)) {
                socket.fire(new AtmosphereMessage(connectorName, msg.toString()));
                if (msg.getMsgType().equals(MessageIdentifier.GET)) {
                    requestedGetMessageMap.put(msg.getOptionalObixObject().getServiceUri(),
                            msg.getOptionalObixObject());
                }
                if (!msg.getMsgType().equals(MessageIdentifier.STA) && !msg.getMsgType().equals(MessageIdentifier.PUT)) {
                    this.addMessageWithoutResponse(msg);
                    int count = 0;
                    Configurator conf = new Configurator();
                    List<ResendMessageTask> tasksForResending = Collections.synchronizedList(new ArrayList<>());
                    while (count <= conf.getTimesToResendMessage()) {
                        count++;
                        ResendMessageTask task = new ResendMessageTask(this, msg, tasksForResending);
                        tasksForResending.add(task);
                        Timer timer = new Timer();
                        long timing = conf.getTimeWaitingForStatusResponseInMilliseconds();
                        timer.schedule(task, timing * count);
                        waitingForStatusMessagesTasks.add(task);
                        runningTimers.put(UUID.randomUUID().toString(), timer);
                    }
                }
                //TODO: remove this line, only for testing with FAKE
                //   handleStatusMessagesFAKE(msg);
          //  }
        } catch (IOException e) {
            logger.info("Colibri Channel closed");
        }
    }

    public ColibriMessage resend(ColibriMessage msg) {
        logger.info("Resend: " + msg.toString());
        ColibriMessage resendMsg = ColibriMessage.createMessageWithNewId(msg);
        try {
            if (msg.getMsgType().equals(MessageIdentifier.GET)) {
                requestedGetMessageMap.remove(msg.getHeader().getId());
                requestedGetMessageMap.put(resendMsg.getHeader().getId(), resendMsg.getOptionalObixObject());
            } else {
                messagesWithoutResponse.remove(msg.getHeader().getId());
                messagesWithoutResponse.put(resendMsg.getHeader().getId(), resendMsg);
            }
            socket.fire(new AtmosphereMessage(connectorName, resendMsg.toString()));
        } catch (IOException e) {
            this.close();
        }
        return resendMsg;
    }

    public void close() {
        runningTimers.values().forEach(Timer::cancel);
        executor.shutdownNow();
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
        for (ResendMessageTask task : waitingForStatusMessagesTasks) {
            task.addReceivedMessage(message);
        }
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
            handlePutMessage(message);
        } else if (message.getMsgType().equals(MessageIdentifier.GET)) {
            send(handleGetMessage(message));
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
        String msgContent = message.getContent().getContentWithoutBreaksAndWhiteSpace();
        String serviceUri = "";
        ObixObject obj;
        boolean successful = true;
        if (msgContent.contains("?freq=")) {
            String[] content = msgContent.split("\\?freq=");
            serviceUri = content[0];
            obj = observeAbleObjectsMap.get(serviceUri);
            PutMessageToColibriTask executionTask = new PutMessageToColibriTask(obj, this, message.getHeader().getId());
            if (obj == null) {
                successful = false;
            } else {
                Date dateNow = new Date();
                String icalTemp = TimeDurationConverter.date2Ical(dateNow).toString();
                Timer timer = new Timer();
                try {
                    Date d = (TimeDurationConverter.ical2Date(icalTemp.split("T")[0] + "T" + content[1]));
                    executionTask.setScheduled(true);
                    /**
                     * Send Put once a day at the specified time
                     */
                    timer.schedule(executionTask, d, 24 * 60 * 60 * 1000);
                    obj.setPutMessageToColibriTask(executionTask);
                } catch (ParseException e) {
                    try {
                        java.time.Duration duration = java.time.Duration.parse(content[1]);
                        executionTask.setScheduled(true);
                        /**
                         * Send Put with the specified duration
                         */
                        timer.schedule(executionTask, duration.toMillis(), duration.toMillis());

                        obj.setPutMessageToColibriTask(executionTask);
                    } catch (DateTimeParseException ex) {
                        successful = false;
                    }
                }
                runningTimers.put(serviceUri, timer);
            }
        } else {
            serviceUri = msgContent;
            obj = observeAbleObjectsMap.get(serviceUri);
            if (obj == null) {
                successful = false;
            } else {
                PutMessageToColibriTask executionTask = new PutMessageToColibriTask(obj, this, message.getHeader().getId());
                obj.setPutMessageToColibriTask(executionTask);
                executionTask.setScheduled(false);
            }
        }
        if (successful) {
            obj.setObservedByColibri(true);
            observedObjectsMap.put(obj.getServiceUri(), obj);
            return ColibriMessage.createStatusMessage(StatusCode.OK, "", message.getHeader().getId());
        }
        return ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "Service cannot be observed", message.getHeader().getId());
    }

    private ColibriMessage handleDetachMessage(ColibriMessage message) {
        ObixObject temp = observedObjectsMap.get(message.getContent().getContentWithoutBreaksAndWhiteSpace());
        if (temp != null) {
            temp.setObservedByColibri(false);
            observedObjectsMap.remove(temp.getServiceUri());
            runningTimers.remove(temp.getServiceUri()).cancel();
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

    private void handlePutMessage(ColibriMessage message) {
        try {
            PutMessageContent content = ColibriMessageContentCreator.getPutMessageContent(message);
            ObixObject serviceObject = observedColibriActionsMap.get(content.getServiceUri());
            if (serviceObject == null) {
                serviceObject = requestedGetMessageMap.get(content.getServiceUri());
            }
            boolean setParam1 = false;
            boolean setParam2 = false;
            if (serviceObject != null) {
                logger.info(content.getValue1HasParameterUri());
                logger.info(serviceObject.getParameter1().getParameterUri());
                if (content.getValue1HasParameterUri().equals(serviceObject.getParameter1().getParameterUri())) {
                    if (content.getValue1Uri().equals(serviceObject.getParameter1().getValueUri())) {
                        serviceObject.setValueParameter1(content.getValue1());
                        setParam1 = true;
                    }
                }
                if (content.getValue1HasParameterUri().equals(serviceObject.getParameter2().getParameterUri())) {
                    if (content.getValue1Uri().equals(serviceObject.getParameter2().getValueUri())) {
                        serviceObject.setValueParameter2(content.getValue1());
                        setParam2 = true;
                    }
                }
                if (content.getValue2HasParameterUri().equals(serviceObject.getParameter1().getParameterUri())) {
                    if (content.getValue2Uri().equals(serviceObject.getParameter1().getValueUri())) {
                        serviceObject.setValueParameter1(content.getValue2());
                        setParam1 = true;
                    }
                }
                if (content.getValue2HasParameterUri().equals(serviceObject.getParameter2().getParameterUri())) {
                    if (content.getValue2Uri().equals(serviceObject.getParameter2().getValueUri())) {
                        serviceObject.setValueParameter2(content.getValue2());
                        setParam2 = true;
                    }
                }
            }
            if (setParam1 && setParam2) {

                //read optional timing for Put on Obix from parameter 2
                PutExecutionTask executionTask = new PutExecutionTask(serviceObject, this, message.getHeader().getId());
                java.util.Date param2Date;
                try {
                    param2Date = TimeDurationConverter.ical2Date(serviceObject.getParameter2().getValue().getValue());
                } catch (ParseException e) {
                    param2Date = new Date();
                }
                Timer timer = new Timer();
                timer.schedule(executionTask, param2Date);
                requestedGetMessageMap.remove(content.getServiceUri());
            } else {
                this.send(ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "PUT message to the service with this address is not possible." +
                        "Please check if the service address is correct.", message.getHeader().getId()));
            }
        } catch (JAXBException e) {
            this.send(ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "Unmarshalling PUT message failed!", message.getHeader().getId()));
        }
    }

    private ColibriMessage handleGetMessage(ColibriMessage message) {
        ObixObject temp = observeAbleObjectsMap.get(message.getContent().getContentWithoutBreaksAndWhiteSpace());
        if (temp != null) {
            List<ObixObject> putObjectList = Collections.synchronizedList(new ArrayList<>());
            putObjectList.add(temp);
            return ColibriMessage.createPutMessage(putObjectList, message.getHeader().getId());
        }
        return ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "Service is not existing and therefore cannot handle GET message", message.getHeader().getId());
    }
/*
    private Boolean alreadySent(ColibriMessage message) {
        for (ColibriMessage msg : messagesWithoutResponse.values()) {
            if (msg.getMsgType().equals(message.getMsgType())) {
                if (message.getOptionalObixObject() != null && msg.getOptionalObixObject() != null
                        && !message.getMsgType().equals(MessageIdentifier.GET)) {
                    if (message.getOptionalObixObject().getServiceUri().equals(msg.getOptionalObixObject().getServiceUri())) {
                        return true;
                    }
                }
            }
        }
        return false;
    } */

    public String getLastMessageReceived() {
        return lastMessageReceived;
    }

    public void setLastMessageReceived(String lastMessageReceived) {
        this.lastMessageReceived = lastMessageReceived;
    }

    public Map<String, ObixObject> getRequestedGetMessageMap() {
        return requestedGetMessageMap;
    }

    public void setRequestedGetMessageMap(Map<String, ObixObject> requestedGetMessageMap) {
        this.requestedGetMessageMap = requestedGetMessageMap;
    }

    public void removeWaitingForStatusMessagesThread(ResendMessageTask thread) {
        waitingForStatusMessagesTasks.remove(thread);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}