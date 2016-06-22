package OADRMsgInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georg on 07.06.16.
 * This class holds the important information for a oadrCanceledPartyRegistration message.
 */
public class MsgInfo_OADRCancelReport implements OADRMsgInfo {

    private List<String> reportRequestIDs;

    private boolean reportToFollow;

    public MsgInfo_OADRCancelReport(){
        this.reportRequestIDs = new ArrayList<>();
    }

    public boolean isReportToFollow() {
        return reportToFollow;
    }

    public void setReportToFollow(boolean reportToFollow) {
        this.reportToFollow = reportToFollow;
    }

    public List<String> getReportRequestIDs() {
        return reportRequestIDs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return "oadrCancelReport";
    }
}
