package OADRMsgInfo;

/**
 * Created by georg on 07.06.16.
 * This class holds the important information for a oadrCanceledPartyRegistration message.
 */
public class MsgInfo_OADRUpdatedReport implements OADRMsgInfo {

    MsgInfo_OADRCancelReport cancelReport;

    public MsgInfo_OADRCancelReport getCancelReport() {
        return cancelReport;
    }

    public void setCancelReport(MsgInfo_OADRCancelReport cancelReport) {
        this.cancelReport = cancelReport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return "oadrUpdatedReport";
    }
}
