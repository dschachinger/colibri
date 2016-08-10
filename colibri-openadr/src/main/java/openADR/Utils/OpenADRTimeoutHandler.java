package openADR.Utils;

import Utils.TimeoutHandler;
import openADR.OADRHandling.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by georg on 20.07.16.
 * * This class defines how to react after a openADR message timeout.
 */
public class OpenADRTimeoutHandler implements TimeoutHandler<Channel, OADRMsgObject> {
    private Logger logger = LoggerFactory.getLogger(OpenADRTimeoutHandler.class);

    @Override
    public void handleTimeout(Channel channel, Map<String, OADRMsgObject> monitoredMsg, String messageID) {

        OADRMsgObject openADRMsg = monitoredMsg.get(messageID);
        if(openADRMsg.getResendIteration() < 2){
            // if needed generate a new ID colMsg.getHeader().setMessageId(colClient.getGenSendMessage().getUniqueMsgID());
            openADRMsg.incResendIteration();
            channel.sendMsgObj(openADRMsg);
            logger.info("resend openADR msg: " + messageID);
        } else {
            logger.info("not resend openADR msg: " + messageID);
        }
    }
}
