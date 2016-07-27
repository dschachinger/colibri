package channel.colibri;

import channel.message.colibriMessage.ColibriMessage;
import channel.message.colibriMessage.MessageIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ResendMessageTask extends TimerTask {

    private ColibriChannel channel;
    private ColibriMessage waitingForResponse;
    private List<ColibriMessage> responseMessages;
    private static final Logger logger = LoggerFactory.getLogger(ResendMessageTask.class);
    private List<ResendMessageTask> earlierTasksForResending;

    public ResendMessageTask(ColibriChannel channel, ColibriMessage waitingForResponse, List<ResendMessageTask> earlierTasksForResending) {
        this.channel = channel;
        this.waitingForResponse = waitingForResponse;
        this.responseMessages = Collections.synchronizedList(new ArrayList<>());
        this.earlierTasksForResending = earlierTasksForResending;
    }

    @Override
    public void run() {
        boolean sendIncorrect = false;
        if (responseMessages.size() == 0) {
            sendIncorrect = true;
        }
        for (ColibriMessage msg : responseMessages) {
            if (msg.getMsgType().equals(MessageIdentifier.STA)
                    && (msg.getHeader().getRefenceId().equals(waitingForResponse.getHeader().getId()))) {
                return;
            } else {
                sendIncorrect = true;
            }
            for (ResendMessageTask task : earlierTasksForResending) {
                if (msg.getMsgType().equals(MessageIdentifier.STA)
                        && (msg.getHeader().getRefenceId().equals(task.getWaitingForResponse().getHeader().getId()))) {
                    return;
                } else {
                    sendIncorrect = true;
                }
            }
        }

        if (sendIncorrect) {
            waitingForResponse = channel.resend(waitingForResponse);
        }
    }

    public void addReceivedMessage(ColibriMessage receivedMessage) {
        this.responseMessages.add(receivedMessage);
    }

    public ColibriMessage getWaitingForResponse() {
        return waitingForResponse;
    }

}
