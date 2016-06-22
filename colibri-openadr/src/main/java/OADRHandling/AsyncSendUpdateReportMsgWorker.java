package OADRHandling;

import OADRMsgInfo.Interval;
import OADRMsgInfo.MsgInfo_OADRCreateReport;
import OADRMsgInfo.MsgInfo_OADRUpdateReport;
import OADRMsgInfo.Report;
import Utils.FollowUpMsg;
import Utils.OADRConInfo;
import Utils.TimeDurationConverter;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by georg on 09.06.16.
 * This class is used to send an asynchronous follow-up reply message.
 * A seperate thread can be initiated to fulfil this task.
 */
public class AsyncSendUpdateReportMsgWorker extends Thread {

    private OADRParty party;

    private int uid;

    private MsgInfo_OADRCreateReport.ReportRequest reportRequest;

    // 0...not canceled, 1...canceled, 2...canceled and send last follow up report
    private AtomicInteger isReportCanceled;

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
     * It will execute the task to send the message.
     */
    public void run(){
        // TODO change to current time. The current state is only for convenient testing.
        long startTimeDiffMilli = TimeDurationConverter.getDateDiff(reportRequest.getReportIntervalStart(), new Date(reportRequest.getReportIntervalStart().getTime()+6000), TimeUnit.MILLISECONDS);
        long amountMesurementsPerReport = reportRequest.getReportBackDurationSec() / reportRequest.getGranularitySec();
        // remaining time between last measurment and transmission of the report

        long deltaTimeMilli = (reportRequest.getReportBackDurationSec() - (amountMesurementsPerReport * reportRequest.getGranularitySec()))*1000;
        // TODO if reportRequest.getReportIntervalDuration() == 0 then it means forever
        long amountSendReports = reportRequest.getReportIntervalDuration() / reportRequest.getReportBackDurationSec();

        System.out.println("received start date: " + reportRequest.getReportIntervalStart());
        System.out.println("New Thread started wait for "+startTimeDiffMilli);

        threadSleep(startTimeDiffMilli);

        for(long amountSendedReports = 0; amountSendedReports<amountSendReports; amountSendedReports++){
            for(long amountMeasuredDataPerReport = 0; amountMeasuredDataPerReport<amountMesurementsPerReport; amountMeasuredDataPerReport++) {
                // TODO get in contact with colibri and query the needed information
                System.out.println("Query colibri for information "+reportRequest.getReportRequestID());

                threadSleep(reportRequest.getGranularitySec()*1000);
            }

            threadSleep(deltaTimeMilli);

            if(isReportCanceled.intValue() != 1){
                if(isReportCanceled.intValue() == 2){
                    System.out.println("Report "+reportRequest.getReportRequestID() + " gen follow up report");
                }

                isReportCanceled.compareAndSet(2,1);
                // TODO use it that way party.handleUpdateReportMsg(reportRequest);
                System.out.println("Send Data to VTN! "+reportRequest.getReportRequestID());
                party.getChannel().sendMsg(genUpdateReport());
            } else {
                System.out.println("Report "+reportRequest.getReportRequestID() + " canceled");
                return;
            }
        }
        System.out.println("Report "+reportRequest.getReportRequestID() + " finished");

    }

    private MsgInfo_OADRUpdateReport genUpdateReport(){
        MsgInfo_OADRUpdateReport info = new MsgInfo_OADRUpdateReport();

        Report report = new Report(OADRConInfo.getReport(reportRequest.getReportSpecifierID()));

        // TODO other meaning as it is in a registerReportMsg?
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
