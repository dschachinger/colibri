package openADR.Utils;

import openADR.OADRMsgInfo.OADRMsgInfo;

/**
 * Created by georg on 09.06.16.
 * Objects from this class are used to transmit a second asynchronous follow-up message.
 */
public class FollowUpMsg {
    // These are the possible types of follow-up messages
    public enum FollowUpMsgType {oadrCreatePartyRegistration, oadrCreatedEvent};
    // This object contains the received origin message
    private OADRMsgInfo originMsgInfo;
    // this object signalize the follow-up message type
    private FollowUpMsgType followUpMsgType;

    public FollowUpMsg(OADRMsgInfo originMsgInfo, FollowUpMsgType followUpMsgType) {
        this.originMsgInfo = originMsgInfo;
        this.followUpMsgType = followUpMsgType;
    }

    public FollowUpMsgType getFollowUpMsgType() {
        return followUpMsgType;
    }

    public OADRMsgInfo getOriginMsgInfo() {
        return originMsgInfo;
    }
}
