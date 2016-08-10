package openADR.OADRHandling;

import openADR.Utils.FollowUpMsg;

/**
 * Created by georg on 09.06.16.
 * This class is used to send an asynchronous follow-up reply message.
 * A seperate thread can be initiated to fulfill this task.
 */
public class AsyncSendFollowUpMsgWorker extends Thread {

    // party which wants to send this message
    private OADRParty party;
    // contains information how the follow-up message should look like
    private FollowUpMsg followUpMsg;

    /**
     * This instantiate an AsyncSendFollowUpMsgWorker object
     * @param party not null
     * @param followUpMsg not null
     */
    public AsyncSendFollowUpMsgWorker(OADRParty party, FollowUpMsg followUpMsg){
        this.party = party;
        this.followUpMsg = followUpMsg;
    }

    /**
     * This method is called by the created thread.
     * It will execute the task to send the message.
     */
    public void run(){
        try {
            // The delay prevents that the follow up message is transmitted before the first reply.
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        party.handleFollowUpMsg(followUpMsg);
    }
}
