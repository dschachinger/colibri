package openADR.Utils;

import com.enernoc.open.oadr2.model.v20b.OadrCancelReport;
import openADR.OADRHandling.AsyncSendUpdateReportMsgWorker;
import openADR.OADRMsgInfo.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by georg on 09.06.16.
 * This class holds all necessary openADR communication information.
 */
public class OADRConInfo {
    private static final String VENName = "colibriVen";

    // The assigned ven ID by the VTN
    private static String VENId;
    // The assigned vtn ID by the VTN
    private static String VTNId;
    // The assigned registrationId by the VTN
    private static String registrationId;
    // Counter to generate a unique requestID
    private static int requestCounter = 0;

    /* This map contains all active workers which gathers the data from colibri and
            transmits cyclically the update report message to the opposite side.
            The key is the reportRequestID. */
    private static HashMap<String, AsyncSendUpdateReportMsgWorker> asyncSendUpdateReportMsgWorkers;

    // true...the vtn receives the report possibilities from the ven
    private static boolean VTNReceivesReportCapabilities = false;

    /* This map contains all reports which can be generated.
            The key ist the reportSpecifierID. */
    private static HashMap<String, Report> reportPossibilities;

    private static Logger logger = LoggerFactory.getLogger(OADRConInfo.class);

    static {
        asyncSendUpdateReportMsgWorkers = new HashMap<>();
        reportPossibilities = new HashMap<>();
    }

    /**
     * This method returns the next unique requestID
     * @return unique request id
     */
    public static String getUniqueRequestId() {
        if(requestCounter == Integer.MAX_VALUE){
            requestCounter = 0;
        }

        requestCounter++;

        return "request_"+requestCounter;
    }

    public static void addReportPossibility(Report report){
        reportPossibilities.put(report.getReportSpecifierID(), report);
    }

    public static Report getReport(String reportSpecifierID){
        return reportPossibilities.get(reportSpecifierID);
    }

    public static List<Report> getAllReportPossibilities(){
        List<Report> reportList = new ArrayList<>();

        for(String reportSpecifierID : reportPossibilities.keySet()){
            reportList.add(reportPossibilities.get(reportSpecifierID));
        }

        return reportList;
    }

    public static boolean getVTNReceivesReportCapabilities() {
        return VTNReceivesReportCapabilities;
    }

    public static void setVTNReceivesReportCapabilities(boolean VTNReceivesReportCapabilities) {
        OADRConInfo.VTNReceivesReportCapabilities = VTNReceivesReportCapabilities;
    }

    public static void deleteConnectionInfo(){
        setVENId(null);
        setRegistrationId(null);
        setVTNReceivesReportCapabilities(false);

        for(String requestID : asyncSendUpdateReportMsgWorkers.keySet()){
            AsyncSendUpdateReportMsgWorker worker = asyncSendUpdateReportMsgWorkers.remove(requestID);
            worker.cancelReport(false);
            logger.info("Cancel report with ID: " + requestID + " !");
        }
    }

    public static void addUpdateReportMsgWorker(AsyncSendUpdateReportMsgWorker worker){
        asyncSendUpdateReportMsgWorkers.put(worker.getReportRequestID(), worker);
    }

    public static boolean deleteUpdateReportMsgWorkers(OadrCancelReport msg){
        boolean wrongIDOccur = false;

        for(String reportRequestID : msg.getReportRequestIDs()){

            AsyncSendUpdateReportMsgWorker worker =
                    asyncSendUpdateReportMsgWorkers.remove(reportRequestID);

            if(worker != null){
                worker.cancelReport(msg.isReportToFollow());
            } else {
                logger.info("Given reportRequestID " + reportRequestID + " not allocated!");
                wrongIDOccur = true;
            }

        }

        return !wrongIDOccur;
    }

    public static Set<String> getAllRegisteredReportRequestIDs(){
        return asyncSendUpdateReportMsgWorkers.keySet();
    }

    public static String getRegistrationId() {
        return registrationId;
    }

    public static void setRegistrationId(String registrationId) {
        OADRConInfo.registrationId = registrationId;
    }

    public static String getVENId() {
        return OADRConInfo.VENId;
    }

    public static void setVENId(String VENId) {
        OADRConInfo.VENId = VENId;
    }

    public static String getVENName() {
        return VENName;
    }

    public static String getVTNId() {
        return VTNId;
    }

    public static void setVTNId(String VTNId) {
        OADRConInfo.VTNId = VTNId;
    }
}
