package channel.colibri;

import channel.message.colibriMessage.ColibriMessage;
import channel.message.colibriMessage.MessageIdentifier;
import channel.message.colibriMessage.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.Configurator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

public class ResendMessageTask extends TimerTask {

    private ColibriChannel channel;
    private ColibriMessage waitingForResponse;
    private List<ColibriMessage> responseMessages;
    private static final Logger logger = LoggerFactory.getLogger(ResendMessageTask.class);
    private boolean sendIncorrect;

    public ResendMessageTask(ColibriChannel channel, ColibriMessage waitingForResponse) {
        this.channel = channel;
        this.waitingForResponse = waitingForResponse;
        this.responseMessages = Collections.synchronizedList(new ArrayList<>());
        this.sendIncorrect = false;
    }

    @Override
    public void run() {
        if (responseMessages.size() == 0) {
            sendIncorrect = true;
        }
        if (countTimesSendedThisMessage() == -1) {
            return;
        }
        if ((countTimesSendedThisMessage()) < Configurator.getInstance().getTimesToResendMessage()) {
            for (ColibriMessage msg : responseMessages) {
                if (msg.getMsgType().equals(MessageIdentifier.STA)
                        && (msg.getHeader().getRefenceId().equals(waitingForResponse.getHeader().getId()))) {
                    return;
                } else {
                    sendIncorrect = true;
                }
            }
            if(sendIncorrect) {
                channel.resend(waitingForResponse);
            }
        } else {
            ColibriMessage message = ColibriMessage.createStatusMessage(StatusCode.ERROR_CONNECTION,
                    "No response from colibri received for the " + waitingForResponse.getMsgType()
                            + " message with initial ID " + waitingForResponse.getHeader().getId(),
                    waitingForResponse.getHeader().getId());
            channel.send(message);
            logger.info(message.toString());
            channel.removeAccordingMessagesFromWaitingForResponse(waitingForResponse);
            channel.removeAccordingTasks(this);
        }
    }

    public void addReceivedMessage(ColibriMessage receivedMessage) {
        this.responseMessages.add(receivedMessage);
    }

    public ColibriMessage getWaitingForResponse() {
        return waitingForResponse;
    }

    public boolean isSendIncorrect() {
        return sendIncorrect;
    }


    private int countTimesSendedThisMessage() {
        int count = -1;
        for (ColibriMessage msg : channel.getMessagesWithoutResponse().values()) {
            if(waitingForResponse.getMsgType().equals(msg.getMsgType())) {
                if(msg.getOptionalConnector() != null) {
                    if(waitingForResponse.getOptionalConnector() != null) {
                        if(waitingForResponse.getOptionalConnector().equals(msg.getOptionalConnector())) {
                            count++;
                        }
                    }
                }
                if(msg.getOptionalObixObject() != null) {
                    if(waitingForResponse.getOptionalObixObject() != null) {
                        if(waitingForResponse.getOptionalObixObject().equals(msg.getOptionalObixObject())) {
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }
}
