package semanticCore.WebSocketHandling;

import Utils.TimeoutHandler;
import semanticCore.MsgObj.ColibriMessage;

import java.util.Map;

/**
 * Created by georg on 20.07.16.
 */
public class ColibriTimeoutHandler implements TimeoutHandler<ColibriClient, ColibriMessage> {
    @Override
    public void handleTimeout(ColibriClient colClient, Map<String, ColibriMessage> monitoredMsg, String messageID) {
        ColibriMessage colMsg = new ColibriMessage(monitoredMsg.get(messageID));
        if(colMsg.getResendIteration() < 2){
            colMsg.getHeader().setMessageId(colClient.getGenSendMessage().getUniqueMsgID());
            colMsg.incResendIteration();
            colClient.sendColibriMsg(colMsg);
            System.out.println("resend colibri msg: " + messageID);
        } else {
            System.out.println("not resend colibri msg: " + messageID);
        }
    }
}
