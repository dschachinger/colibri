package OADRMsgInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georg on 07.06.16.
 * This class holds the important information for a oadrCanceledPartyRegistration message.
 */
public class MsgInfo_OADRUpdateReport implements OADRMsgInfo {

    private List<Report> reports;

    public MsgInfo_OADRUpdateReport(){
        reports = new ArrayList<>();
    }

    public List<Report> getReports() {
        return reports;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return "oadrUpdateReport";
    }
}
