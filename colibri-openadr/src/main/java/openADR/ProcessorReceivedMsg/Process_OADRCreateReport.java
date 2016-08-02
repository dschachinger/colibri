package openADR.ProcessorReceivedMsg;

import Utils.TimeDurationConverter;
import com.enernoc.open.oadr2.model.v20b.*;
import com.enernoc.open.oadr2.model.v20b.ei.ReportSpecifier;
import com.enernoc.open.oadr2.model.v20b.ei.SpecifierPayload;
import openADR.OADRHandling.OADRParty;
import openADR.OADRMsgInfo.MsgInfo_OADRCreateReport;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrCreateReport.
 */
public class Process_OADRCreateReport extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrCreateReport.
     * Return null, because there is no need to reply to this type of message.
     * @param obj generate reply for this message. The contained message type has to be OadrCreateReport.
     * @param responseCode
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj, String responseCode) {
        OadrCreateReport recMsg = (OadrCreateReport)obj.getMsg();

        List<OadrReportRequest> reportRequests = recMsg.getOadrReportRequests();

        OadrCreatedReport response = new OadrCreatedReport();

        response.setEiResponse(genEiRespone(recMsg.getRequestID(), responseCode));

        OadrPendingReports oadrPendingReports = new OadrPendingReports();


        for(String reportRequestID : OADRConInfo.getAllRegisteredReportRequestIDs()){
            oadrPendingReports.getReportRequestIDs().add(reportRequestID);
        }
        response.setOadrPendingReports(oadrPendingReports);

        return new OADRMsgObject("oadrCreatedReport", null, response);
    }

    /**
     * This method returns an MsgInfo_OADRCreateReport object.
     * This object contains all needful information for a engery consumer from an OadrCreateReport message.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrCreateReport.
     * @param party
     * @return  The openADR.OADRMsgInfo object contains all needful information for a engery consumer.
     */
    @Override
    public OADRMsgInfo extractInfo(OADRMsgObject obj, OADRParty party) {
        OadrCreateReport msg = (OadrCreateReport)obj.getMsg();
        MsgInfo_OADRCreateReport info = new MsgInfo_OADRCreateReport();

        List<OadrReportRequest> reportRequests = msg.getOadrReportRequests();

        for(OadrReportRequest oadrReportRequest : reportRequests){
            MsgInfo_OADRCreateReport.ReportRequest reportRequest = info.getNewReportRequest();


            reportRequest.setReportRequestID(oadrReportRequest.getReportRequestID());

            ReportSpecifier reportSpecifier = oadrReportRequest.getReportSpecifier();
            reportRequest.setReportSpecifierID(reportSpecifier.getReportSpecifierID());
            reportRequest.setGranularitySec(TimeDurationConverter.xCal2Seconds(reportSpecifier.getGranularity().getDuration().getValue()));
            reportRequest.setReportBackDurationSec(TimeDurationConverter.xCal2Seconds(reportSpecifier.getReportBackDuration().getDuration().getValue()));
            reportRequest.setReportIntervalStart(TimeDurationConverter.ical2Date(reportSpecifier.getReportInterval().getProperties().getDtstart().getDateTime().getValue().toString()));
            reportRequest.setReportIntervalDuration(TimeDurationConverter.xCal2Seconds(reportSpecifier.getReportInterval().getProperties().getDuration().getDuration().getValue()));
            
            for(SpecifierPayload oadrSpecifierPayload : reportSpecifier.getSpecifierPayloads()){
                MsgInfo_OADRCreateReport.SpecifierPayload specifierPayload = info.getNewSpecifierPayload();

                specifierPayload.setrID(oadrSpecifierPayload.getRID());
                specifierPayload.setReadingType(oadrSpecifierPayload.getReadingType());

                reportRequest.getSpecifierPayloads().add(specifierPayload);
            }
            
            

            info.getReportRequests().add(reportRequest);
        }

        party.generateAsyncSendUpdateReportMsgWorker(info.getReportRequests());

        return info;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doRecMsgViolateConstraints(OADRMsgObject obj, HashMap<String, OADRMsgObject> sendedMsgMap){
        OadrCreateReport recMsg = (OadrCreateReport)obj.getMsg();
        String venID = recMsg.getVenID();

        if(!OADRConInfo.getVTNReceivesReportCapabilities()){
            return "450";
        } else {
            return checkConstraints(sendedMsgMap, true, null,
                    null, venID, null);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateSendedMsgMap(OADRMsgObject obj, HashMap<String, OADRMsgObject> sendedMsgMap) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRCreateReport().getMsgType();
    }
}
