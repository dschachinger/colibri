package OADRMsgInfo;

import java.math.BigDecimal;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by georg on 07.06.16.
 * This class holds the important information for a oadrCanceledPartyRegistration message.
 */
public class MsgInfo_OADRRegisterReport implements OADRMsgInfo {
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
