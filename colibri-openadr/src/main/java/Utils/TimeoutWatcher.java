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

/**
 * Created by georg on 18.07.16.
 */
public class TimeoutWatcher<S, T> {
    private int timeoutMilliSec;
    private Map<String, T> monitoredMsg;
    private ExecutorService executor;
    private S medium;
    private TimeoutHandler<S, T> handler;

    private Logger logger = LoggerFactory.getLogger(TimeoutWatcher.class);

    private TimeoutWatcher(int timeoutMilliSec){
        this.executor = Executors.newCachedThreadPool();
        this.timeoutMilliSec = timeoutMilliSec;
    }

    public void addMonitoredMsg(String msgID){
        if(!this.monitoredMsg.containsKey(msgID)){
            throw new IllegalStateException("message must be in the given map");
        }
        executor.execute(new Watcher(msgID));
    }

    public static TimeoutWatcher<ColibriClient, ColibriMessage> initColibriTimeoutWatcher(int timeoutMilliSec, ColibriClient colClient){
        TimeoutWatcher<ColibriClient, ColibriMessage> watcher =
                new TimeoutWatcher<>(timeoutMilliSec);
        watcher.monitoredMsg = colClient.getSendedMsgToColCore();
        watcher.medium = colClient;
        watcher.handler = new ColibriTimeoutHandler();

        return watcher;
    }

    public static TimeoutWatcher<Channel, OADRMsgObject> initOpenADRTimeoutWatcher(int timeoutMilliSec, Channel channel){
        TimeoutWatcher<Channel, OADRMsgObject> watcher =
                new TimeoutWatcher<>(timeoutMilliSec);
        watcher.monitoredMsg = channel.getSendedMsgMap();
        watcher.medium = channel;
        watcher.handler = new OpenADRTimeoutHandler();

        return watcher;
    }

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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(monitoredMsg.containsKey(messageID)){
                logger.info("timeout for msg: " + messageID);
                // TODO insert handler.handleTimeout(medium, monitoredMsg, messageID);
            } else {
                logger.info("timing is okay for msg: " + messageID);
            }

            logger.info(Thread.currentThread().getName()+" End. MessageID = "+messageID + " threadID: " + Thread.currentThread().getId());
        }
    }
}
