package openADR.OADRMsgInfo;

/**
 * Created by georg on 07.06.16.
 * This class holds the important information for a oadrCreatePartyRegistration message.
 */
public class MsgInfo_OADRCreatePartyRegistration implements OADRMsgInfo {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return "oadrCreatePartyRegistration";
    }
}
