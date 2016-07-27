package openADR.OADRHandling;

import Utils.TimeDurationConverter;
import openADR.OADRMsgInfo.Interval;
import openADR.OADRMsgInfo.MsgInfo_OADRCreateReport;
import openADR.OADRMsgInfo.MsgInfo_OADRUpdateReport;
import openADR.OADRMsgInfo.Report;
import openADR.Utils.OADRConInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by georg on 09.06.16.
 * This class is used to gather report data and transmit the report.
 * A seperate thread can be initiated to fulfil this task.
 */
public class AsyncSendUpdateReportMsgWorker extends Thread {

    // OpenADR party which generates the reports
    private OADRParty party;

    // Used as an index to identify intervals. Unique Identifier
    private int uid;

    /* corresponding report request
        This object contains detailed information how the report should look like.
     */
    private MsgInfo_OADRCreateReport.ReportRequest reportRequest;

    // 0...not canceled, 1...canceled, 2...canceled and send last follow up report
    private AtomicInteger isReportCanceled;

    private Logger logger = LoggerFactory.getLogger(AsyncSendUpdateReportMsgWorker.class);

    /**
     * This instantiate a AsyncSendFollowUpMsgWorker object
     * @param reportRequest not null
     */
    public AsyncSendUpdateReportMsgWorker(MsgInfo_OADRCreateReport.ReportRequest reportRequest, OADRParty party){
        this.reportRequest = reportRequest;
        this.party = party;
        isReportCanceled = new AtomicInteger(0);
        uid = 0;
    }

    /**
     * This method is called by the created thread.
     * It gathers the reprot information and sends the report to the opposite party
     */
    public void run(){
        if(reportRequest.getReportSpecifierID().equals("METADATA")){
            MsgInfo_OADRUpdateReport sendInfo = new MsgInfo_OADRUpdateReport();
            sendInfo.setMetareport(true);
            sendInfo.getReports().addAll(OADRConInfo.getAllReportPossibilities());
            party.getChannel().sendMsg(sendInfo);
            logger.info("send METADATA report");
            return;
        }

        // TODO implement later change to current time. The current state is only for convenient testing.
        long startTimeDiffMilli = TimeDurationConverter.getDateDiff(reportRequest.getReportIntervalStart(), new Date(reportRequest.getReportIntervalStart().getTime()+6000), TimeUnit.MILLISECONDS);
        long amountMesurementsPerReport = reportRequest.getReportBackDurationSec() / reportRequest.getGranularitySec();
        // remaining time between last measurment and transmission of the report

        long deltaTimeMilli = (reportRequest.getReportBackDurationSec() - (amountMesurementsPerReport * reportRequest.getGranularitySec()))*1000;
        long amountSendReports = reportRequest.getReportIntervalDuration() / reportRequest.getReportBackDurationSec();
        boolean unlimitedSendReports = reportRequest.getReportIntervalDuration() == 0;

        logger.info("received start date: " + reportRequest.getReportIntervalStart());
        logger.info("New Thread started wait for "+startTimeDiffMilli);

        threadSleep(startTimeDiffMilli);

        for(long amountSendedReports = 0; (amountSendedReports<amountSendReports) || unlimitedSendReports; amountSendedReports++){
            for(long amountMeasuredDataPerReport = 0; amountMeasuredDataPerReport<amountMesurementsPerReport; amountMeasuredDataPerReport++) {
                // TODO implement later get in contact with colibri and query the needed information
                logger.info("Query colibri for information "+reportRequest.getReportRequestID());

                threadSleep(reportRequest.getGranularitySec()*1000);
            }

            threadSleep(deltaTimeMilli);

            if(isReportCanceled.intValue() != 1){
                if(isReportCanceled.intValue() == 2){
                    logger.info("Report "+reportRequest.getReportRequestID() + " gen follow up report");
                }

                isReportCanceled.compareAndSet(2,1);
                // TODO implement later use it that way party.handleUpdateReportMsg(reportRequest);
                logger.info("Send Data to VTN! "+reportRequest.getReportRequestID());
                party.getChannel().sendMsg(genUpdateReport());
            } else {
                logger.info("Report "+reportRequest.getReportRequestID() + " canceled");
                return;
            }
        }
        logger.info("Report "+reportRequest.getReportRequestID() + " finished");

    }

    private MsgInfo_OADRUpdateReport genUpdateReport(){
        MsgInfo_OADRUpdateReport info = new MsgInfo_OADRUpdateReport();

        Report report = new Report(OADRConInfo.getReport(reportRequest.getReportSpecifierID()));

        report.setCreatedDateTime(new Date());
        report.setDurationSec(960);

        report.setReportRequestID(reportRequest.getReportRequestID());

        Interval interval = new Interval();
        interval.setDurationSec(961+uid);
        interval.setSignalValue(88+uid);
        interval.setUid(uid+"");
        uid++;
        report.getIntervals().add(interval);

        info.getReports().add(report);

        return info;
    }

    public String getReportRequestID(){
        return reportRequest.getReportRequestID();
    }

    public void cancelReport(boolean followUpReportNeeded){
        isReportCanceled.set(followUpReportNeeded ? 2:1);
    }

    private void threadSleep(long milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
