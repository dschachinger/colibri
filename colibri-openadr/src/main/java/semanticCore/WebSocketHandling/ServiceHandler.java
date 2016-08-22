package semanticCore.WebSocketHandling;

import Bridge.OpenADRColibriBridge;
import Utils.TimeDurationConverter;
import openADR.OADRMsgInfo.MsgInfo_OADRDistributeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semanticCore.MsgObj.ColibriMessage;
import semanticCore.MsgObj.ContentMsgObj.PutMsg;
import semanticCore.MsgObj.ContentType;
import semanticCore.MsgObj.Header;
import semanticCore.MsgObj.MsgType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by georg on 26.07.16.
 * Objects from this class hold various information about a service. e.g. if the core knows/observes a certain service, name of the follow-up service.
 * It is also responsible to inform the colibri core about new values from an observed service.
 */
public class ServiceHandler implements Runnable {
    /* If the service is observed and the core wants to get the information daily,
        then this date indicates at which time the put message should be sent.
        Otherwise it is null. */
    private Date sendTime;
    /* If the service is observed and the core wants to get the information cyclically,
        then this variable indicates at which time interval the put message should be sent.
        Otherwise it is null. */
    private Long intervalDurationSec;
    // Colibri client which uses the handler
    private ColibriClient colClient;
    // This objects holds all the configuration data of a service.
    private ServiceDataConfig serviceDataConfig;
    // This thread is responsible to transmit the put messages of an observed service.
    private Thread serviceThread;
    // Does the core know about this service by an add message?
    private boolean serviceAdded;
    // Is this service observed?
    private boolean serviceObserved;
    // This queue contains all the events which occur within two put messages.
    private LinkedBlockingQueue<MsgInfo_OADRDistributeEvent.Event> bufferedEvents;

    private Logger logger = LoggerFactory.getLogger(ServiceHandler.class);

    public ServiceHandler(ServiceDataConfig serviceDataConfig, ColibriClient colibriClient){
        this.sendTime = null;
        this.intervalDurationSec = null;
        this.bufferedEvents = new LinkedBlockingQueue<>();
        this.serviceDataConfig = serviceDataConfig;
        this.colClient = colibriClient;
    }

    /**
     * This method adds a new event to the service.
     * @param event
     */
    public void addEvent(MsgInfo_OADRDistributeEvent.Event event){
        bufferedEvents.add(event);
    }

    public String getFollowService() {
        return serviceDataConfig.getFollowUpServiceDataConfig().getServiceName();
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public long getIntervalDurationSec() {
        return intervalDurationSec;
    }

    public void setIntervalDurationSec(long intervalDurationSec) {
        this.intervalDurationSec = intervalDurationSec;
    }

    public boolean isServiceAdded() {
        return serviceAdded;
    }

    public void setServiceAdded(boolean serviceAdded) {
        this.serviceAdded = serviceAdded;
    }

    public boolean isServiceObserved() {
        return serviceObserved;
    }

    /**
     * This method changes the observed status of a service.
     * It is only allowed to change the status from observed to not observed and vice-vera,
     * but it is not allowed to change the observed status two times in a row to true/false.
     * @param serviceObserved true...service will be observed, otherwise...it will be detached
     */
    public void changeServiceObservedStatus(boolean serviceObserved) {
        this.serviceObserved = serviceObserved;
        if(this.serviceObserved){
            if(serviceThread == null){
                serviceThread = new Thread(this,"serviceHandler"+serviceDataConfig.getEventType());
                serviceThread.start();
            } else{
                logger.error("service " + serviceDataConfig.getServiceName() + " already running");
            }

        } else {
            if(serviceThread != null){
                serviceThread.interrupt();
                serviceThread = null;
            } else {
                logger.error("service " + serviceDataConfig.getServiceName() + " was not running");
            }

        }
    }

    public ServiceDataConfig getServiceDataConfig() {
        return serviceDataConfig;
    }

    @Override
    public void run() {
        logger.info("new thread running");
        while (true) {
            if (sendTime != null) {
                sendEventsDaily();
            } else if (intervalDurationSec != null) {
                sendEventsCyclic();
            } else {
                sendEventsImmediately();
            }

            if(!colClient.getServicesMap().get(serviceDataConfig.getServiceName()).isServiceObserved()||
                    Thread.currentThread().isInterrupted()){
                break;
            }


            sendEvents();
        }

        logger.info("no further service messages for " + serviceDataConfig.getServiceName());
    }

    /**
     * This method is used to pause the service thread for an daily observed service.
     */
    private void sendEventsDaily(){

        Date curTime = new Date(new Date().getTime() % 86400000);
        logger.info("cur date: " + curTime.getTime() + " " + curTime);
        logger.info("send date: " + sendTime.getTime()+ " " + sendTime);
        long delta = TimeDurationConverter.getDateDiff(curTime, sendTime, TimeUnit.MILLISECONDS);

        // when the delta value is negative this means the next message will be transmitted tomorrow.
        if(delta < 0){
            delta += 86400000;
        }

        logger.info("daily service delta in milli seconds: " + delta);

        try {
            Thread.sleep(delta);
        } catch (InterruptedException e) {
            logger.info("daily service interrupted");
        }
    }

    /**
     * This method is used to pause the service thread for a cyclic observed service.
     */
    private void sendEventsCyclic(){
        logger.info("cyclic interval in seconds: " + intervalDurationSec);

        try {
            Thread.sleep(intervalDurationSec*1000);
        } catch (InterruptedException e) {
            logger.info("cyclic service interrupted");
        }
    }

    /**
     * This method is used to pause the service thread for an immediately observed service.
     * It waits until a new event occurs.
     */
    private void sendEventsImmediately(){
        logger.info("send immediately");
        try {
            MsgInfo_OADRDistributeEvent.Event event = bufferedEvents.take();
            bufferedEvents.put(event);
        } catch (InterruptedException e) {
            logger.info("immediately service interrupted");
        }
    }

    /**
     * This method sends the buffered event information to the colibri core.
     */
    private void sendEvents(){
        List<MsgInfo_OADRDistributeEvent.Event> events = new ArrayList<>();
        OpenADRColibriBridge bridge = colClient.getBridge();

        while (true) {
            // condition to stop the loop
            if (bufferedEvents.peek() == null) {
                break;
            }
            events.add(bufferedEvents.poll());
        }

        PutMsg putMsgContent = bridge.getOpenADRToColibri().convertOpenADREventsToColibriPUTContent(events, bridge);

        Header header = new Header();
        header.setDate(new Date());
        header.setContentType(ContentType.APPLICATION_RDF_XML);
        header.setMessageId(bridge.getColClient().getGenSendMessage().getUniqueMsgID());

        String strContent;
        if(!putMsgContent.getDescriptions().isEmpty()){
            strContent = bridge.getColClient().getGenSendMessage().transformPOJOToXML(putMsgContent);
        } else {
            strContent = "";
        }

        ColibriMessage msg = new ColibriMessage(MsgType.PUT_DATA_VALUES, header, strContent);

        colClient.sendColibriMsg(msg);

        logger.info("PUT message with service URL" + serviceDataConfig.getServiceName() );


    }
}
