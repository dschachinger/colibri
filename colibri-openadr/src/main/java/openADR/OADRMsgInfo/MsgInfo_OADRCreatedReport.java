package openADR.OADRMsgInfo;

/**
 * Created by georg on 07.06.16.
 * This class holds the important information for a oadrCreatedReport message.
 */
public class MsgInfo_OADRCreatedReport implements OADRMsgInfo {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return "oadrCreatedReport";
    }
}
