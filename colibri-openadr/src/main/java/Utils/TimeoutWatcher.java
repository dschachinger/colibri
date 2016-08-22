package Utils;

import openADR.OADRHandling.Channel;
import openADR.Utils.OADRMsgObject;
import openADR.Utils.OpenADRTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semanticCore.MsgObj.ColibriMessage;
import semanticCore.WebSocketHandling.ColibriClient;
import semanticCore.WebSocketHandling.ColibriTimeoutHandler;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by georg on 18.07.16.
 * Objects from this class are used to monitor if a reply is received within a needed time.
 * This class is used for the colibri side and the openADR side.
 */
public class TimeoutWatcher<S, T> {
    // timeout in milliseconds
    private int timeoutMilliSec;
    /* Map which contains all the monitored sent messages.
        If the proper reply is received the entry needs to be removed from the object which handles the message exchange. */
    private Map<String, T> monitoredMsg;
    // A thread pool is used for monitoring to keep ressource usage low.
    private ExecutorService executor;
    private S medium;
    // This objects defines how to react on a timeout.
    private TimeoutHandler<S, T> handler;

    private Logger logger = LoggerFactory.getLogger(TimeoutWatcher.class);

    /**
     *
     * @param timeoutMilliSec time limit for receiving a reply
     */
    private TimeoutWatcher(int timeoutMilliSec){
        this.executor = Executors.newCachedThreadPool();
        this.timeoutMilliSec = timeoutMilliSec;
    }

    /**
     * This method adds a message id to the watch list. The message id is the id from a sent message
     * @param msgID
     */
    public void addMonitoredMsg(String msgID){
        if(!this.monitoredMsg.containsKey(msgID)){
            throw new IllegalStateException("message must be in the given map");
        }
        executor.execute(new Watcher(msgID));
    }

    /**
     * This method initializes a colibri timeout watcher.
     * @param timeoutMilliSec time limit for receiving a reply
     * @param colClient colibri client
     * @return timeout watcher for a given colibri client
     */
    public static TimeoutWatcher<ColibriClient, ColibriMessage> initColibriTimeoutWatcher(int timeoutMilliSec, ColibriClient colClient){
        TimeoutWatcher<ColibriClient, ColibriMessage> watcher =
                new TimeoutWatcher<>(timeoutMilliSec);
        watcher.monitoredMsg = colClient.getSentMsgToColCore();
        watcher.medium = colClient;
        watcher.handler = new ColibriTimeoutHandler();

        return watcher;
    }

    /**
     * This method initializes an openADR timeout watcher.
     * @param timeoutMilliSec time limit for receiving a reply
     * @param channel openADR channel to transmit/receive messages
     * @return timeout watcher for a given openADR channel
     */
    public static TimeoutWatcher<Channel, OADRMsgObject> initOpenADRTimeoutWatcher(int timeoutMilliSec, Channel channel){
        TimeoutWatcher<Channel, OADRMsgObject> watcher =
                new TimeoutWatcher<>(timeoutMilliSec);
        watcher.monitoredMsg = channel.getSentMsgMap();
        watcher.medium = channel;
        watcher.handler = new OpenADRTimeoutHandler();

        return watcher;
    }

    public void shutdown(){
        executor.shutdownNow();
    }

    /**
     * Each object is responsible to monitor the reply for one message
     */
    class Watcher implements Runnable{
        String messageID;

        public Watcher(String messageID){
            this.messageID = messageID;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("timeoutWatcher "+messageID);
            logger.info(Thread.currentThread().getName()+" Start. MessageID = "+messageID + " threadID: " + Thread.currentThread().getId());

            try {
                Thread.sleep(timeoutMilliSec);

                if(monitoredMsg.containsKey(messageID)){
                    logger.info("timeout for msg: " + messageID);
                    handler.handleTimeout(medium, monitoredMsg, messageID);
                } else {
                    logger.info("timing is okay for msg: " + messageID);
                }

            } catch (InterruptedException e) {
                logger.info("thread interrupted --> can not wait until timeout");
            }



            logger.info(Thread.currentThread().getName()+" End. MessageID = "+messageID + " threadID: " + Thread.currentThread().getId());
        }
    }
}
