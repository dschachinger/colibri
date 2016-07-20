package openADR.OADRMsgInfo;

/**
 * Created by georg on 07.06.16.
 * This class holds the important information for a oadrUpdatedReport message.
 */
public class MsgInfo_OADRUpdatedReport implements OADRMsgInfo {
    /* A oadrUpdatedReport can contain oadrCancelReport elements optionally. These are used to cancel reports.
       Therefore the MsgInfo_OADRCancelReport is used because it is actual the message to cancel reports.  */
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
