package channel.colibri;

import channel.colibri.taskServices.PutMessageToColibriTask;
import channel.colibri.taskServices.ResendMessageTask;
import channel.message.AtmosphereMessage;
import channel.message.Message;
import channel.message.colibriMessage.ColibriMessage;
import channel.message.colibriMessage.ColibriMessageMapper;
import channel.message.messageObj.MessageIdentifier;
import channel.message.messageObj.PutMessageContent;
import channel.message.messageObj.StatusCode;
import channel.message.service.ColibriMessageContentCreator;
import channel.message.service.SparqlMsgService;
import channel.obix.PutToObixTask;
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

/**
 * This class represents a channel for sending and receiving {@link ColibriMessage} to and from the
 * Colibri semantic core configured by the {@link Configurator}.
 */
public class ColibriChannel {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    private final static ObjectMapper mapper = new ObjectMapper();
    private Client client;
    private Socket socket;
    private String connectorName;
    private String host;
    private int port;
    private Boolean registered;
    private ExecutorService executor;

    /**
     * The complete URL of the web socket endpoint to communicate with, for example:
     * http://127.0.0.1:6789/chat
     */
    private String fullUrl;
    /**
     * A Map with the service URI of a {@link ObixObject}. The Values are the executor tasks for this object.
     * The are used to send PUT messages to obix. This tasks can be scheduled.
     */
    private Map<String, PutToObixTask> putToObixExecutors;

    /**
     * A map with the message-ID as key. The values are {@link ColibriMessage} which expect a response from the colibri semantic core
     * but haven't received one.
     */
    private Map<String, ColibriMessage> messagesWithoutResponse;

    /**
     * A map with the service URI of an {@link ObixObject} as key. The values are {@link ObixObject} which are added as services to the
     * colibri semantic core and can therefore be observed by colibri.
     */
    private Map<String, ObixObject> observeAbleObjectsMap;

    /**
     * A map with the service URI of an {@link ObixObject} as key. The values are {@link ObixObject} which are currently observed by
     * the colibri semantic core.
     */
    private Map<String, ObixObject> observedObjectsMap;

    /**
     * A map with the service URI of an {@link ObixObject} as key. The values are {@link ObixObject} for which a GET message was sent
     * to the colibri semantic core.
     */
    private Map<String, ObixObject> requestedGetMessageMap;

    private String lastMessageReceived = "No messages from Colibri Semantic Core received.";

    /**
     * A map with the service URI of an {@link ObixObject} as key. The values are {@link ObixObject} which observe actions performed
     * by the colibri semantic core.
     */
    private Map<String, ObixObject> observedMessagesOfColibriMap;

    /**
     * A list of tasks which are scheduled to resend {@link ColibriMessage} if no fitting response was received to the initial or
     * resent messages.
     */
    private List<ResendMessageTask> resendMessagesTasks;

    /**
     * A map with a random ID as key. The values are times to send {@link ColibriMessage} on a specific time to the colibri semantic
     * core, for example scheduled through a parameter in a received OBS message.
     */
    private Map<String, Timer> runningTimers;

    /**
     * A map with the QUE message-ID as key. The values are QUE {@link ColibriMessage} which expect a response from the colibri
     * semantic core but haven't received one.
     */
    private Map<String, ColibriMessage> queryMessagesWithoutResponse;

    /**
     * True, if the Atmosphere Web Socket Chat is used as endpoint for testing purposes, otherwise false.
     */
    private boolean usingAtmosphereTestWebSocket;

    private final static Logger logger = LoggerFactory.getLogger(ColibriChannel.class);

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    public ColibriChannel(String connectorName, String host, int port, String fullUrl) {
        this.connectorName = connectorName;
        this.host = host;
        this.port = port;
        this.fullUrl = fullUrl;
        this.usingAtmosphereTestWebSocket = fullUrl.equals("http://127.0.0.1:6789/chat");
        this.registered = false;
        this.messagesWithoutResponse = Collections.synchronizedMap(new HashMap<>());
        this.observeAbleObjectsMap = Collections.synchronizedMap(new HashMap<>());
        this.observedObjectsMap = Collections.synchronizedMap(new HashMap<>());
        this.observedMessagesOfColibriMap = Collections.synchronizedMap(new HashMap<>());
        this.requestedGetMessageMap = Collections.synchronizedMap(new HashMap<>());
        this.resendMessagesTasks = Collections.synchronizedList(new ArrayList<>());
        this.queryMessagesWithoutResponse = Collections.synchronizedMap(new HashMap<>());
        this.runningTimers = new HashMap<>();
        this.putToObixExecutors = Collections.synchronizedMap(new HashMap<>());
        this.executor = Executors.newCachedThreadPool();
    }

    /******************************************************************
     *                            Methods                             *
     ******************************************************************/

    public void run() throws IOException {
        /**
         * Using the Atmosphere Chat Web Socket endpoint for testing purposes
         * For communication with the Atmosphere Chat Web Socket, the {@link AtmosphereMessage} class is used for
         * sending and receiving messages.
         */
        RequestBuilder request;
        if (usingAtmosphereTestWebSocket) {
            this.client = ClientFactory.getDefault().newClient(AtmosphereClient.class);
            request = ((AtmosphereClient)client).newRequestBuilder()
                    .method(Request.METHOD.GET)
                    .uri(fullUrl)
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
        }
        /**
         * Using the a different web socket endpoint, for example Colibris websocket.
         * For this communication, the {@link Message} class is used for
         * sending and receiving messages.
         */
        else {
            this.client = ClientFactory.getDefault().newClient();
            request = client.newRequestBuilder()
                    .method(Request.METHOD.GET)
                    .uri(fullUrl)
                    .encoder(new Encoder<Message, String>() {
                        public String encode(Message data) {
                                return data.getMessage();
                        }
                    })
                    .decoder(new Decoder<String, Message>() {
                        public Message decode(Event type, String data) {
                            return new Message(data);
                        }
                    })
                    .transport(Request.TRANSPORT.WEBSOCKET)
                    .transport(Request.TRANSPORT.LONG_POLLING);
        }
            socket = client.create();
            socket.on(Event.MESSAGE, new Function<Message>() {
                public void on(Message t) {
                        try {
                            messageReceived(ColibriMessageMapper.msgToPOJO(t.getMessage()));
                        } catch (IllegalArgumentException e) {
                            lastMessageReceived = e.getMessage();
                        }
                        lastMessageReceived = t.getMessage();
                }
            }).on(Event.MESSAGE, new Function<AtmosphereMessage>() {
            public void on(AtmosphereMessage t) {
                try {
                    if(!t.getAuthor().equals(connectorName)) {
                        messageReceived(ColibriMessageMapper.msgToPOJO(t.getMessage()));
                    }
                } catch (IllegalArgumentException e) {
                    lastMessageReceived = e.getMessage();
                }
            }
        }).on(new Function<Throwable>() {
                @Override
                public void on(Throwable t) {
                    logger.info("Cannot interact with Colibri, connection is faulty.");
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

    /**
     * Method for sending messages to the Ccolibri web socket endpoint configured through the {@link Configurator}.
     *
     * @param msg   The message which should be sent.
     */
    public void send(ColibriMessage msg) {
        logger.info("Send: \n" + msg.toString());
        try {
            if(usingAtmosphereTestWebSocket) {
                socket.fire(new AtmosphereMessage(connectorName, msg.toString()));
            } else {
                socket.fire(new Message(msg.toString()));
            }
            if (msg.getMsgType().equals(MessageIdentifier.GET)) {
                requestedGetMessageMap.put(msg.getOptionalObixObject().getServiceUri(),
                        msg.getOptionalObixObject());
            }
            else if (msg.getMsgType().equals(MessageIdentifier.QUE)) {
                queryMessagesWithoutResponse.put(msg.getHeader().getId(), msg);
            }
            else if (!msg.getMsgType().equals(MessageIdentifier.STA) && !msg.getMsgType().equals(MessageIdentifier.PUT)) {
                //Add message to list for resending messages, if no response is received.
                //Only done for REG, DRE, OBS, DET and UPD messages.
                this.addMessageWithoutResponse(msg);
                int count = 0;
                Configurator conf = Configurator.getInstance();
                messagesWithoutResponse.put(msg.getHeader().getId(), msg);
                while (count <= conf.getTimesToResendMessage()) {
                    //Set up tasks for resending messages if no fitting response was received.
                    count++;
                    ResendMessageTask task = new ResendMessageTask(this, msg);
                    Timer timer = new Timer();
                    long timing = conf.getTimeWaitingForResponseInMilliseconds();
                    //Schedule the task for resending multiple messages times as configured in the config.properties file.
                    timer.schedule(task, timing * count);
                    resendMessagesTasks.add(task);
                    runningTimers.put(UUID.randomUUID().toString(), timer);
                }
            }
        } catch (IOException e) {
            logger.info("Cannot interact with Colibri, connection is faulty.");
        }
    }

    /**
     * Method for resending messages to the Colibri web socket endpoint configured through the {@link Configurator}.
     *
     * @param msg   The message which should be resent.
     * @return      The resent message.
     */
    public ColibriMessage resend(ColibriMessage msg) {
        logger.info("Resend: \n" + msg.toString());
        ColibriMessage resendMsg = ColibriMessage.createMessageWithNewId(msg);
        try {
            messagesWithoutResponse.put(resendMsg.getHeader().getId(), resendMsg);
            if(usingAtmosphereTestWebSocket) {
                socket.fire(new AtmosphereMessage(connectorName, msg.toString()));
            } else {
                socket.fire(new Message(msg.toString()));
            }
        } catch (IOException e) {
            logger.info("Cannot interact with colibri, connection is faulty.");
        }
        return resendMsg;
    }

    /**
     * Method for closing the Colibri channel.
     */
    public void close() {
        runningTimers.values().forEach(Timer::cancel);
        executor.shutdownNow();
        socket.close();
    }

    /**
     * Method for receiving messages and starting according handler methods.
     * Depending on the {@link MessageIdentifier}, different response-handler methods are executed.
     *
     * @param message   The message which was received from Colibri.
     */
    private void messageReceived(ColibriMessage message) {
        for (ResendMessageTask task : resendMessagesTasks) {
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
        } else if (message.getMsgType().equals(MessageIdentifier.QRE)) {
            send(handleQreMessage(message));
        }
    }

    /******************************************************************
     *                Handlers for received messages                  *
     ******************************************************************/

    /**
     * This method handles a received {@link ColibriMessage} with
     * {@link MessageIdentifier} STA.
     *
     * @param message   The {@link ColibriMessage} with {@link MessageIdentifier} STA.
     */
    private void handleStatusMessages(ColibriMessage message) {
        ColibriMessage requestMsg = messagesWithoutResponse.get(message.getHeader().getRefenceId());
        if (requestMsg != null) {
            /**
             * The STA message is handled differently, depending on the {@link MessageIdentifier} of the message the
             * STA message is referring to.
              */
            if (message.getContent().getContent().contains(StatusCode.OK.toString())) {
                if (requestMsg.getMsgType().equals(MessageIdentifier.REG)) {
                    this.registered = true;
                    removeAccordingMessagesFromWaitingForResponse(message);
                } else if (requestMsg.getMsgType().equals(MessageIdentifier.DRE)) {
                    this.registered = false;
                    removeAccordingMessagesFromWaitingForResponse(message);
                } else if (requestMsg.getMsgType().equals(MessageIdentifier.ADD)) {
                    requestMsg.getOptionalObixObject().setAddedAsService(true);
                    removeAccordingMessagesFromWaitingForResponse(message);
                    observeAbleObjectsMap.put(requestMsg.getOptionalObixObject().getServiceUri(),
                            requestMsg.getOptionalObixObject());
                } else if (requestMsg.getMsgType().equals(MessageIdentifier.REM)) {
                    requestMsg.getOptionalObixObject().setAddedAsService(false);
                    observeAbleObjectsMap.remove(requestMsg.getOptionalObixObject().getServiceUri());
                    removeAccordingMessagesFromWaitingForResponse(message);
                } else if (requestMsg.getMsgType().equals(MessageIdentifier.OBS)) {
                    requestMsg.getOptionalObixObject().setObservesColibriActions(true);
                    removeAccordingMessagesFromWaitingForResponse(message);
                    observedMessagesOfColibriMap.put(requestMsg.getOptionalObixObject().getServiceUri(),
                            requestMsg.getOptionalObixObject());
                } else if (requestMsg.getMsgType().equals(MessageIdentifier.DET)) {
                    requestMsg.getOptionalObixObject().setObservesColibriActions(false);
                    removeAccordingMessagesFromWaitingForResponse(message);
                    observedMessagesOfColibriMap.remove(requestMsg.getOptionalObixObject().getServiceUri(),
                            requestMsg.getOptionalObixObject());
                } else if (requestMsg.getMsgType().equals(MessageIdentifier.UPD)) {
                    removeAccordingMessagesFromWaitingForResponse(message);
                }
            }
        }
    }

    /**
     * This method handles a received {@link ColibriMessage} with
     * {@link MessageIdentifier} DRE.
     *
     * @param message   The {@link ColibriMessage} with {@link MessageIdentifier} DRE.
     * @return          The response {@link ColibriMessage} with {@link StatusCode} 200 if the service was
     *                  successfully deregistered, otherwise with {@link StatusCode} 500.
     */
    private ColibriMessage handleDeregisterMessage(ColibriMessage message) {
        if (message.getContent().getContentWithoutBreaksAndWhiteSpace().equals(Configurator.getInstance().getConnectorAddress())
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

    /**
     * This method handles a received {@link ColibriMessage} with
     * {@link MessageIdentifier} OBS.
     *
     * @param message   The {@link ColibriMessage} with {@link MessageIdentifier} OBS.
     * @return          The response {@link ColibriMessage} with {@link StatusCode} 200 if the service is
     *                  successfully observed, otherwise with {@link StatusCode} 500.
     */
    private ColibriMessage handleObserveMessage(ColibriMessage message) {
        String msgContent = message.getContent().getContentWithoutBreaksAndWhiteSpace();
        String serviceUri;
        ObixObject obj;
        boolean successful = true;
        if (msgContent.contains("?freq=")) {
            /**
             * If present, handle timing parameters in the OBS message.
             */
            String[] content = msgContent.split("\\?freq=");
            serviceUri = content[0];
            obj = observeAbleObjectsMap.get(serviceUri);
            PutMessageToColibriTask executionTask = new PutMessageToColibriTask(obj, this, message.getHeader().getId());
            if (obj == null) {
                successful = false;
            } else {
                /*
                 * If present, set timers for scheduled sending of PUT messages to Colibri.
                 */
                Date dateNow = new Date();
                String icalTemp = TimeDurationConverter.date2Ical(dateNow).toString();
                Timer timer = new Timer();
                try {
                    Date d = (TimeDurationConverter.ical2Date(icalTemp.split("T")[0] + "T" + content[1]));
                    /*
                     * If the time for execution is already passed on this day, increase it by one day
                     */
                    if (d.before(new Date())) {
                        d = new Date(d.getTime() + 24 * 60 * 60 * 1000);
                    }
                    executionTask.setScheduled(true);
                    /*
                     * Send Put once a day at the specified time
                     * 24*60*60*1000 equals one day in milliseconds
                     */
                    timer.schedule(executionTask, d, 24 * 60 * 60 * 1000);
                    obj.setPutMessageToColibriTask(executionTask);
                } catch (ParseException e) {
                    try {
                        java.time.Duration duration = java.time.Duration.parse(content[1]);
                        executionTask.setScheduled(true);
                        /**
                          * If a duration is present in OBS message, send scheduled PUT messages to Colibri
                          * with the specified duration
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
            /**
             * No time parameters are present in the OBS message, send PUT messages unscheduled.
             */
            serviceUri = msgContent;
            obj = observeAbleObjectsMap.get(serviceUri);
            if (obj == null) {
                successful = false;
            } else {
                PutMessageToColibriTask executionTask = new PutMessageToColibriTask(obj, this, message.getHeader().getId());
                obj.setPutMessageToColibriTask(executionTask);
                //indicates the the task is not scheduled
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

    /**
     * This method handles a received {@link ColibriMessage} with
     * {@link MessageIdentifier} DET.
     *
     * @param message   The {@link ColibriMessage} with {@link MessageIdentifier} DET.
     * @return          The response {@link ColibriMessage} with {@link StatusCode} 200 if the service is
     *                  successfully detached, otherwise with {@link StatusCode} 500.
     */
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

    /**
     * This method handles a received {@link ColibriMessage} with
     * {@link MessageIdentifier} REM.
     *
     * @param message   The {@link ColibriMessage} with {@link MessageIdentifier} REM.
     * @return          The response {@link ColibriMessage} with {@link StatusCode} 200 if the service is
     *                  successfully removed, otherwise with {@link StatusCode} 500.
     */
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

    /**
     * This method handles a received {@link ColibriMessage} with
     * {@link MessageIdentifier} PUT.
     *
     * @param message   The {@link ColibriMessage} with {@link MessageIdentifier} PUT.
     */
    private void handlePutMessage(ColibriMessage message) {
        ColibriMessage errorMsg = ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC,
                "Unmarshalling PUT message failed!", message.getHeader().getId());
        try {
            PutMessageContent content = ColibriMessageContentCreator.getPutMessageContent(message);
            ObixObject serviceObject = observedMessagesOfColibriMap.get(content.getServiceUri());
            if (serviceObject == null) {
                serviceObject = requestedGetMessageMap.get(content.getServiceUri());
            }
            if(serviceObject == null) {
                this.send(errorMsg);
            }
            // Schedule PUT to OBIX
            setTimerTask(serviceObject, message, content);
        } catch (JAXBException e) {
            this.send(errorMsg);
        }
    }

    /**
     * This method handles a received {@link ColibriMessage} with
     * {@link MessageIdentifier} GET.
     *
     * @param message   The {@link ColibriMessage} with {@link MessageIdentifier} GET.
     * @return          The response {@link ColibriMessage} with {@link StatusCode} 200 if the service is
     *                  successfully handled the GET message, otherwise with {@link StatusCode} 500.
     */
    private ColibriMessage handleGetMessage(ColibriMessage message) {
        ObixObject temp = observeAbleObjectsMap.get(message.getContent().getContentWithoutBreaksAndWhiteSpace());
        if (temp != null) {
            List<ObixObject> putObjectList = Collections.synchronizedList(new ArrayList<>());
            putObjectList.add(temp);
            return ColibriMessage.createPutMessage(putObjectList, message.getHeader().getId());
        }
        return ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "Service is not existing and therefore cannot handle GET message", message.getHeader().getId());
    }

    /**
     * This method handles a received {@link ColibriMessage} with
     * {@link MessageIdentifier} QRE.
     *
     * @param message   The {@link ColibriMessage} with {@link MessageIdentifier} QRE.
     * @return          The response {@link ColibriMessage} with {@link StatusCode} 200 if the service is
     *                  successfully handled the QRE message, otherwise with {@link StatusCode} 500 or 700.
     */
    private ColibriMessage handleQreMessage(ColibriMessage message) {
        try {
            if (message.getHeader().getRefenceId() == null) {
                return ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "QUR message does not contain a reference ID and does therefore not match to" +
                        "any sent QUE message. QRE-id: " + message.getHeader().getId());
            }
            ColibriMessage tempMsg = queryMessagesWithoutResponse.get(message.getHeader().getRefenceId());
            if (tempMsg == null) {
                return ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "QRE referenceId is not matching any of the" +
                        "sent QUE message ids. QRE-id: " + message.getHeader().getId());
            } else {
                SparqlMsgService.processSparqlResultSetfromColibriMessage(message, tempMsg.getOptionalExpectedVars());
            }
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            return ColibriMessage.createStatusMessage(StatusCode.ERROR_PROCESSING, e.getMessage());
        }
        return ColibriMessage.createStatusMessage(StatusCode.OK, "Received QRE message", message.getHeader().getId());
    }

    /******************************************************************
     *                            Helpers                             *
     ******************************************************************/

    /**
     * This method instantiates tasks which are scheduled by a timer for updating the value of a {@link ObixObject}
     * parameter.
     *
     * @param serviceObject     The {@link ObixObject} of which the parameter is updated at the scheduled time.
     * @param content           The {@link PutMessageContent} of the received PUT {@link ColibriMessage}.
     * @param message           The {@link ColibriMessage} which contains the updated value and the timing schedule.
     */
    private void setTimerTask(ObixObject serviceObject, ColibriMessage message, PutMessageContent content) {
        PutToObixTask executionTask = putToObixExecutors.get(serviceObject.getServiceUri());
        if(executionTask == null) {
            logger.error("There is no obix service with the URI " + serviceObject.getServiceUri() + " available " +
                    "which can receive a CoAP-PUT message");
            return;
        } else {
            executionTask = new PutToObixTask(executionTask);
        }
        java.util.Date paramDate;
        try {
            /**
             * Read an optional time parameter in PUT message from Colibri. Then schedule PUT messages to OBIX according
             * to this time parameter.
             */
            if(content.getValue1Uri().equals(serviceObject.getParameter1().getValueUri())
                    && serviceObject.getParameter1().isTimer()) {
                paramDate = TimeDurationConverter.ical2Date(content.getValue1().getValue());
            } else if(content.getValue2Uri().equals(serviceObject.getParameter1().getValueUri())
                    && serviceObject.getParameter1().isTimer()) {
                paramDate = TimeDurationConverter.ical2Date(content.getValue2().getValue());
            } else if(content.getValue1Uri().equals(serviceObject.getParameter2().getValueUri())
                    && serviceObject.getParameter2().isTimer()) {
                paramDate = TimeDurationConverter.ical2Date(content.getValue1().getValue());
            } else if(content.getValue2Uri().equals(serviceObject.getParameter2().getValueUri())
                    && serviceObject.getParameter2().isTimer()) {
                paramDate = TimeDurationConverter.ical2Date(content.getValue2().getValue());
            } else {
                /**
                 * Send the PUT message to OBIX immediately if no time parameter is provided.
                 */
                paramDate = new Date();
            }
        } catch (ParseException e) {
            paramDate = new Date();
        }
        Timer timer = new Timer();
        executionTask.setPutMessage(message);
        executionTask.setObj(serviceObject);
        timer.schedule(executionTask, paramDate);
        requestedGetMessageMap.remove(serviceObject.getServiceUri());
    }

    /**
     * This method removes all messages from {@link #messagesWithoutResponse} which are similar
     * to the given {@link ColibriMessage}. Similar means, that the {@link MessageIdentifier} is the same
     * and the optionalObixObject, the optionalConnector or the Reference-ID and the ID of the message are
     * equal.
     *
     * @param message   The {@link ColibriMessage} of which similar messages are removed from
     *                  {@link #messagesWithoutResponse}
     */
    public void removeAccordingMessagesFromWaitingForResponse(ColibriMessage message) {
        ColibriMessage toRemove = messagesWithoutResponse.remove(message.getHeader().getRefenceId());
        if (toRemove == null) {
            toRemove = message;
        }
        List<ColibriMessage> remList = Collections.synchronizedList(new ArrayList<>());
        for (ColibriMessage msg : messagesWithoutResponse.values()) {
            if (toRemove.getMsgType().equals(msg.getMsgType())) {
                if (msg.getOptionalConnector() != null) {
                    if (toRemove.getOptionalConnector() != null) {
                        if (toRemove.getOptionalConnector().equals(msg.getOptionalConnector())) {
                            remList.add(msg);
                        }
                    }
                }
                if (msg.getOptionalObixObject() != null) {
                    if (toRemove.getOptionalObixObject() != null) {
                        if (toRemove.getOptionalObixObject().equals(msg.getOptionalObixObject())) {
                            remList.add(msg);
                        }
                    }
                }
            }
        }
        for (ColibriMessage msg : remList) {
            messagesWithoutResponse.remove(msg.getHeader().getId());
        }
    }

    /**
     * Removes all {@link ResendMessageTask} from {@link #resendMessagesTasks} which are equal to the given task.
     *
     * @param task      The task which is used to specify the tasks to delete from {@link #resendMessagesTasks}.
     */
    public void removeAccordingTasks(ResendMessageTask task) {
        List<ResendMessageTask> remList = Collections.synchronizedList(new ArrayList<>());
        for (ResendMessageTask t : resendMessagesTasks) {
            if (t.getWaitingForResponse().equals(task.getWaitingForResponse())) {
                remList.add(t);
            }
        }
        for (ResendMessageTask t : remList) {
            resendMessagesTasks.remove(t);
        }
    }

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public Boolean getRegistered() {
        return registered;
    }

    public Map<String, ColibriMessage> addMessageWithoutResponse(ColibriMessage message) {
        messagesWithoutResponse.put(message.getHeader().getId(), message);
        return messagesWithoutResponse;
    }

    public Map<String, ColibriMessage> getMessagesWithoutResponse() {
        return messagesWithoutResponse;
    }

    public String getLastMessageReceived() {
        return lastMessageReceived;
    }

    public void addPutToObixTask(String serviceUri, PutToObixTask putToObixTask) {
        this.putToObixExecutors.put(serviceUri, putToObixTask);
    }
}