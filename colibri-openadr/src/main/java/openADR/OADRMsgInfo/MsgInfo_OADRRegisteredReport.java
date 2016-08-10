package openADR.OADRMsgInfo;

/**
 * Created by georg on 07.06.16.
 * This class holds the important information for an oadrRegisteredReport message.
 */
public class MsgInfo_OADRRegisteredReport implements OADRMsgInfo {
    /* An oadrRegisteredReport can contain oadrReportRequest elements optionally. These are used to request reports.
       Therefore the MsgInfo_OADRCreateReport is used because it is actually the message to request reports.  */
    MsgInfo_OADRCreateReport oadrCreateReport;

    public MsgInfo_OADRCreateReport getOadrCreateReport() {
        return oadrCreateReport;
    }

    public void setOadrCreateReport(MsgInfo_OADRCreateReport oadrCreateReport) {
        this.oadrCreateReport = oadrCreateReport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return "oadrRegisteredReport";
    }
}
