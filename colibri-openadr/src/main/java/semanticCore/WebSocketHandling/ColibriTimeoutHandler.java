package semanticCore.WebSocketHandling;

import Utils.TimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semanticCore.MsgObj.ColibriMessage;

import java.util.Map;

/**
 * Created by georg on 20.07.16.

 */
public class ColibriTimeoutHandler implements TimeoutHandler<ColibriClient, ColibriMessage> {
    private Logger logger = LoggerFactory.getLogger(ColibriTimeoutHandler.class);

    @Override
    public void handleTimeout(ColibriClient colClient, Map<String, ColibriMessage> monitoredMsg, String messageID) {
        ColibriMessage colMsg = new ColibriMessage(monitoredMsg.get(messageID));
        if(colMsg.getResendIteration() < 2){
            colMsg.getHeader().setMessageId(colClient.getGenSendMessage().getUniqueMsgID());
            colMsg.incResendIteration();
            colClient.sendColibriMsg(colMsg);
            logger.info("resend colibri msg: " + messageID);
        } else {
            logger.info("not resend colibri msg: " + messageID + ", transmit error status message instead");
            colClient.sendColibriMsg(colClient.getGenSendMessage().gen_STATUS("600", messageID));
        }
    }
}
