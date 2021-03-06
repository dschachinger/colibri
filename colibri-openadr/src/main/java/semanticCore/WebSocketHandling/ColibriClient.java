package semanticCore.WebSocketHandling;

import Bridge.OpenADRColibriBridge;
import Utils.*;
import org.atmosphere.wasync.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semanticCore.MsgObj.ColibriMessage;
import semanticCore.MsgObj.ContentMsgObj.AddMsg;
import semanticCore.MsgObj.ContentMsgObj.PutMsg;
import semanticCore.MsgObj.ContentMsgObj.RegisterMsg;
import semanticCore.MsgObj.MsgType;

import javax.xml.bind.*;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by georg on 02.07.16.
 */
public class ColibriClient {
    // This is the actual socket which is responsible for the message exchange.
    private Socket socket;
    // Used to generate send colibri messages
    private GenerateSendMessage genSendMessage;
    // Used to process received colibri messages
    private ProcessReceivedMsg processMessage;
    // This object bridges the colibri part with the openADR part and vice-versa.
    private OpenADRColibriBridge bridge;
    // This HaspMap stores all the sent messages as long as no reply is received
    private Map<String, ColibriMessage> sentMsgToColCore;
    private JAXBContext jaxbContext;
    private Marshaller jaxbMarshaller;
    private Unmarshaller jaxbUnmarshaller;
    // This object monitors the timing of the received messages.
    private TimeoutWatcher timeoutWatcher;

    private Logger logger = LoggerFactory.getLogger(ColibriClient.class);

    /* This HashMap contains all possible services.
        The key is the colibri core service and the value is the service handler.
        This handler stores if the core knows/observes a certain service, name of the follow service and
        it is also responsible to inform the colibri core about new values from an observed service. */
    private HashMap<String, ServiceHandler> servicesMap;

    // true...when the connector is proper registered, false...otherwise
    private boolean registered = false;
    private MyLock terminateLock;
    private AtomicBoolean terminateTimeout;

    // This string represents the base URL for all services
    private String serviceBaseURL;

    // This url defines the web socket address to the colibri core
    String colibriCoreURL;

    public ColibriClient(OpenADRColibriBridge bridge, String serviceBaseURL, String colibriCoreURL, int timeoutSec){
        try {
            jaxbContext = JAXBContext.newInstance(RegisterMsg.class, AddMsg.class, PutMsg.class);
            jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.setProperty("com.sun.xml.internal.bind.characterEscapeHandler", new DummyEscapeHandler());

            jaxbMarshaller.setProperty("com.sun.xml.internal.bind.xmlHeaders",
                    "<!DOCTYPE rdf:RDF [\n" +
                            "<!ENTITY xsd \"http://www.w3.org/2001/XMLSchema#\" >\n" +
                            "<!ENTITY colibri \"https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#\">]>");
        } catch (PropertyException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        this.serviceBaseURL = serviceBaseURL;
        this.colibriCoreURL = colibriCoreURL;

        servicesMap = new HashMap<>();

        for(EventType eventType : EventType.values()){
            ServiceDataConfig serviceDataConfig = ServiceDataConfig.initService(eventType, serviceBaseURL);
            ServiceHandler serviceHandler = new ServiceHandler(serviceDataConfig, this);
            servicesMap.put(serviceDataConfig.getServiceName(), serviceHandler);
        }

        try {
            socket = InitWebsocket.initWebSocket(this);
        } catch (ConnectException e) {
            logger.error("Exception: Connection establishment to colibri server refused. Maybe it is not running");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        genSendMessage = new GenerateSendMessage(jaxbMarshaller);
        sentMsgToColCore = Collections.synchronizedMap(new HashMap<String, ColibriMessage>());
        timeoutWatcher = TimeoutWatcher.initColibriTimeoutWatcher(timeoutSec*1000, this);
        this.processMessage = new ProcessReceivedMsg(this);
        this.bridge = bridge;
        this.terminateLock = new MyLock();
        terminateTimeout = new AtomicBoolean(true);
    }

    public GenerateSendMessage getGenSendMessage() {
        return genSendMessage;
    }

    /**
     * @return a map of all services handlers.
     */
    public HashMap<String, ServiceHandler> getServicesMap() {
        return servicesMap;
    }

    public boolean isRegistered() {
        return registered;
    }

    /**
     * This method is responsible to transmit a given colibri message to the colibri core.
     * @param sendMsg given colibri message
     */
    public void sendColibriMsg(ColibriMessage sendMsg){
        if(!sendMsg.getMsgType().equals(MsgType.STATUS)){
            sentMsgToColCore.put(sendMsg.getHeader().getMessageId(), sendMsg);
            timeoutWatcher.addMonitoredMsg(sendMsg.getHeader().getMessageId());
        }
        try {
            socket.fire(new Message(sendMsg.toMsgString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is responsible to process a given received colibri message from the colibri core.
     * @param msg given colibri message
     */
    public void processReceivedMsg(ColibriMessage msg){
        Pair<Boolean, List<ColibriMessage>> result = processMessage.processColMsg(msg);

        boolean sendToOpenADR = result.getFst();

        List<ColibriMessage> replies = result.getSnd();
        if(replies != null){
            for(ColibriMessage reply : replies){
                sendColibriMsg(reply);
            }
        }

        if(sendToOpenADR){
            bridge.informationFlowFromColibriToOpenADR(msg);
        }

    }

    public void sendRegisterMessage(){
        if(!registered) {
            sendColibriMsg(genSendMessage.gen_REGISTER());
        }
        else {
            logger.info("connector is already registered");
        }
    }

    public void sendDeregisterMessage(){
        if(registered) {
            sendColibriMsg(genSendMessage.gen_DEREGISTER());
        }
        else {
            logger.info("connector is already deregistered");
        }
    }

    public void sendAddService(EventType eventType){
        if(!registered) {
            logger.info("connector is not registered");
            return;
        }

        ServiceDataConfig serviceDataConfig = null;

        for(String serviceURL : servicesMap.keySet()){
            ServiceDataConfig buffer = servicesMap.get(serviceURL).getServiceDataConfig();

            if(buffer.getEventType().equals(eventType)){
                serviceDataConfig = buffer;
            }
        }

        if(serviceDataConfig==null){
            logger.error("no supported add message for eventType: " + eventType);
            return;
        }

        sendColibriMsg(genSendMessage.gen_ADD_SERVICE(serviceDataConfig, serviceBaseURL));
    }

    public void sendQueryMessage(String query){
        if(!registered){
            logger.info("connector is not registered");
            return;
        }

        sendColibriMsg(genSendMessage.gen_QUERY(query));
    }

    public void sendUpdateMessage(String sparqlUpdate){
        if(!registered){
            logger.info("connector is not registered");
            return;
        }

        sendColibriMsg(genSendMessage.gen_UPDATE(sparqlUpdate));
    }

    public Map<String, ColibriMessage> getSentMsgToColCore() {
        return sentMsgToColCore;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public String getServiceBaseURL() {
        return serviceBaseURL;
    }

    public Unmarshaller getJaxbUnmarshaller() {
        return jaxbUnmarshaller;
    }

    public OpenADRColibriBridge getBridge() {
        return bridge;
    }

    public String getColibriCoreURL() {
        return colibriCoreURL;
    }

    public ProcessReceivedMsg getProcessMessage() {
        return processMessage;
    }

    /**
     * This method terminates the client.
     * It is not guaranteed that this client still works afterwards.
     * It is only allowed to call this method if the party was successfully started beforehand.
     */
    public Thread terminate(){
        Thread terminateThread = new DeregisterThread();
        terminateThread.start();
        return terminateThread;
    }

    //--------------------------- inner class DeregisterThread ---------------------------------//

    /**
     * This class is used to start a new thread to shut down the colibri client.
     * A new thread is started because you have to deregister the client at the server side and
     * you have to wait until the server responses with an okay status message.
     */
    public class DeregisterThread extends Thread{

        @Override
        public void run() {
            if(registered) {
                sendDeregisterMessage();
                logger.info("waiting for proper deregistration on the colibri core side");
                waitForDeregistration();
                logger.info("go on with termination");
            }

            timeoutWatcher.shutdown();

            logger.info("close websocket");
            socket.close();
            logger.info("finished close websocket");
        }
    }

    public void successfulDeregisteredGoOnWithTermination(){
        synchronized (terminateLock){
            terminateTimeout.set(false);
            terminateLock.notifyAll();

        }
    }

    private void waitForDeregistration(){
        synchronized (terminateLock){
            try {
                // 5 seconds timeout
                terminateLock.wait(50000);
                if(terminateTimeout.compareAndSet(true,false)) {
                    registered = false;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
