package OADRHandling;

import Utils.FollowUpMsg;

/**
 * Created by georg on 09.06.16.
 * This class is used to send an asynchronous follow-up reply message.
 * A seperate thread can be initiated to fulfil this task.
 */
public class AsyncSendFollowUpMsgWorker extends Thread {

    // party which wants to send this message
    private OADRParty party;
    // contains information how the follow-up message should look like
    private FollowUpMsg followUpMsg;

    /**
     * This instantiate a AsyncSendFollowUpMsgWorker object
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
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        party.handleFollowUpMsg(followUpMsg);
    }
}
