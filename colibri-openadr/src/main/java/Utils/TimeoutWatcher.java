package Utils;

import openADR.OADRHandling.Channel;
import openADR.Utils.OADRMsgObject;
import semanticCore.MsgObj.ColibriMessage;
import semanticCore.WebSocketHandling.ColibriClient;
import semanticCore.WebSocketHandling.ColibriTimeoutHandler;
import openADR.Utils.OpenADRTimeoutHandler;

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
            System.out.println(Thread.currentThread().getName()+" Start. MessageID = "+messageID + "threadID: " + Thread.currentThread().getId());

            try {
                Thread.sleep(timeoutMilliSec);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(monitoredMsg.containsKey(messageID)){
                System.out.println("timeout for msg: " + messageID);
                handler.handleTimeout(medium, monitoredMsg, messageID);
            } else {
                System.out.println("timing is okay for msg: " + messageID);
            }

            System.out.println(Thread.currentThread().getName()+" End. MessageID = "+messageID + "threadID: " + Thread.currentThread().getId());
        }
    }
}
