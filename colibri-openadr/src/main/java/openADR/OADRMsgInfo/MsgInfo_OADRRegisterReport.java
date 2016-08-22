package openADR.OADRMsgInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georg on 07.06.16.
 * This class holds the important information for an oadrRegisterReport message.
 */
public class MsgInfo_OADRRegisterReport implements OADRMsgInfo {
    // This list contains all the report capabilities
    private List<Report> reports;

    public MsgInfo_OADRRegisterReport(){
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
        return "oadrRegisterReport";
    }



}
