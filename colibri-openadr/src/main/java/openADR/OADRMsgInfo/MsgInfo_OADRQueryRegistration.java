package openADR.OADRMsgInfo;

/**
 * Created by georg on 07.06.16.
 * This class holds the important information for an oadrQueryRegistration message.
 */
public class MsgInfo_OADRQueryRegistration implements OADRMsgInfo {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return "oadrQueryRegistration";
    }
}
