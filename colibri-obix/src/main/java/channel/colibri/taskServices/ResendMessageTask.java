package channel.colibri.taskServices;

import channel.colibri.ColibriChannel;
import channel.message.colibriMessage.ColibriMessage;
import channel.message.messageObj.MessageIdentifier;
import channel.message.messageObj.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.Configurator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

/**
 * Represents Tasks for resending a {@link ColibriMessage} to the Colibri web socket endpoint
 * if no fitting response was received.
 * This resending is scheduled through timers in the {@link ColibriChannel}.
 */
public class ResendMessageTask extends TimerTask {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    private ColibriChannel channel;

    /**
     * The {@link ColibriMessage} which needs a fitting response from Colibri.
     */
    private ColibriMessage waitingForResponse;

    /**
     * A list of all received response {@link ColibriMessage} from colibri,
     * both fitting and unfitting messages included.
     */
    private List<ColibriMessage> responseMessages;

    /**
     * True, if no received {@link ColibriMessage} is fitting.
     */
    private boolean receivedIncorrect;
    private static final Logger logger = LoggerFactory.getLogger(ResendMessageTask.class);

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    public ResendMessageTask(ColibriChannel channel, ColibriMessage waitingForResponse) {
        this.channel = channel;
        this.waitingForResponse = waitingForResponse;
        this.responseMessages = Collections.synchronizedList(new ArrayList<>());
        this.receivedIncorrect = false;
    }

    /******************************************************************
     *                            Methods                             *
     ******************************************************************/

    @Override
    public void run() {

        if (responseMessages.size() == 0) {
            /*
              No messages received since last (re)send message.
             */
            receivedIncorrect = true;
        }

        if (countTimesSentThisMessage() == -1) {
            /*
              A fitting response from colibri was received in a task which was scheduled earlier.
             */
            return;
        }

        // Only send the message again if it wasn't sent to often.
        if ((countTimesSentThisMessage()) < Configurator.getInstance().getTimesToResendMessage()) {
            // Check if one of the received messages fits the sent message.
            for (ColibriMessage msg : responseMessages) {
                if (msg.getMsgType().equals(MessageIdentifier.STA)
                        && (msg.getHeader().getRefenceId().equals(waitingForResponse.getHeader().getId()))) {
                    /*
                      A fitting response from colibri was received.
                     */
                    return;
                } else {
                    receivedIncorrect = true;
                }
            }
            if(receivedIncorrect) {
                channel.resend(waitingForResponse);
            }
        } else {
            /*
              Message was resent more often than the configured times provided through the {@link Configurator}.
             */
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

    /**
     * Calculates, how often the tasks initial {@link ColibriMessage} was (re)sent without getting a response.
     *
     * @return  -1, if a fitting response was received. Otherwise the number of times the {@link ColibriMessage}
     * was resent without receiving a fitting response.
     */
    private int countTimesSentThisMessage() {
        int count = -1;
        for (ColibriMessage msg : channel.getMessagesWithoutResponse().values()) {
            if(waitingForResponse.getMsgType().equals(msg.getMsgType())) {
                /*
                 * For REG and DRE messages
                 */
                if(msg.getOptionalConnector() != null) {
                    if(waitingForResponse.getOptionalConnector() != null) {
                        if(waitingForResponse.getOptionalConnector().equals(msg.getOptionalConnector())) {
                            count++;
                        }
                    }
                }
                /*
                 * For ADD, DET, OBS, REM and GET messages
                 */
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

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public void addReceivedMessage(ColibriMessage receivedMessage) {
        this.responseMessages.add(receivedMessage);
    }

    public ColibriMessage getWaitingForResponse() {
        return waitingForResponse;
    }

}
