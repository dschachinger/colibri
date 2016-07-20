package openADR.Utils;

import Utils.TimeoutHandler;
import openADR.OADRHandling.Channel;
import openADR.OADRHandling.OADRParty;
import openADR.Utils.OADRMsgObject;

import java.util.Map;

/**
 * Created by georg on 20.07.16.
 */
public class OpenADRTimeoutHandler implements TimeoutHandler<Channel, OADRMsgObject> {
    @Override
    public void handleTimeout(Channel channel, Map<String, OADRMsgObject> monitoredMsg, String messageID) {

        OADRMsgObject openADRMsg = monitoredMsg.get(messageID);
        if(openADRMsg.getResendIteration() < 2){
            // neue ID colMsg.getHeader().setMessageId(colClient.getGenSendMessage().getUniqueMsgID());
            openADRMsg.incResendIteration();
            channel.sendMsgObj(openADRMsg);
            System.out.println("resend openADR msg: " + messageID);
        } else {
            System.out.println("not resend openADR msg: " + messageID);
        }
    }
}
