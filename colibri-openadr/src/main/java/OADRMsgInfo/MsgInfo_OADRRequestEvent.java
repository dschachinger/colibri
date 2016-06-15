package OADRMsgInfo;

/**
 * Created by georg on 07.06.16.
 * This class holds the important information for a oadrRequestEvent message.
 */
public class MsgInfo_OADRRequestEvent implements OADRMsgInfo {

    // The highest amount of events which VTN will return in his oadrDistributeEvent message
    private Long replyLimit;

    public Long getReplyLimit() {
        return replyLimit;
    }

    public void setReplyLimit(Long replyLimit) {
        this.replyLimit = replyLimit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return "oadrRequestEvent";
    }
}
