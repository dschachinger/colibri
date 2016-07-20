package openADR.OADRMsgInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georg on 07.06.16.
 * This class holds the important information for a oadrUpdateReport message.
 */
public class MsgInfo_OADRUpdateReport implements OADRMsgInfo {

    private boolean isMetareport = false;

    // contains the data points and information about it (e.g. quality)
    private List<Report> reports;

    public MsgInfo_OADRUpdateReport(){
        reports = new ArrayList<>();
    }

    public List<Report> getReports() {
        return reports;
    }

    public boolean isMetareport() {
        return isMetareport;
    }

    public void setMetareport(boolean metareport) {
        isMetareport = metareport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return "oadrUpdateReport";
    }
}
