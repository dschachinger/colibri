package openADR.ProcessorReceivedMsg;

import Utils.TimeDurationConverter;
import com.enernoc.open.oadr2.model.v20b.OadrCreatedReport;
import com.enernoc.open.oadr2.model.v20b.OadrPendingReports;
import com.enernoc.open.oadr2.model.v20b.OadrRegisteredReport;
import com.enernoc.open.oadr2.model.v20b.OadrReportRequest;
import com.enernoc.open.oadr2.model.v20b.ei.ReportSpecifier;
import com.enernoc.open.oadr2.model.v20b.ei.SpecifierPayload;
import openADR.OADRHandling.OADRParty;
import openADR.OADRMsgInfo.MsgInfo_OADRCreateReport;
import openADR.OADRMsgInfo.MsgInfo_OADRRegisteredReport;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrRegisteredReport.
 */
public class Process_OADRRegisteredReport extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrRegisteredReport.
     * Return null, because there is no need to reply to this type of message.
     * @param obj generate reply for this message. The contained message type has to be OadrRegisteredReport.
     * @param responseCode
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj, String responseCode) {
        OadrRegisteredReport recMsg = (OadrRegisteredReport)obj.getMsg();

        List<OadrReportRequest> reportRequests = recMsg.getOadrReportRequests();
        if(reportRequests.isEmpty()){
            /*
                RETURN no message if the received message does not contain report requests
            */
            return null;
        }

        OadrCreatedReport response = new OadrCreatedReport();

        OadrPendingReports oadrPendingReports = new OadrPendingReports();

        for(String reportRequestID : OADRConInfo.getAllRegisteredReportRequestIDs()){
            oadrPendingReports.getReportRequestIDs().add(reportRequestID);
        }
        response.setOadrPendingReports(oadrPendingReports);

        return new OADRMsgObject("oadrCreatedReport", null, response);
    }

    /**
     * This method returns an MsgInfo_OADRRegisteredReport object.
     * This object contains all needful information for a engery consumer from an OadrRegisteredReport message.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrRegisteredReport.
     * @param party
     * @return  The openADR.OADRMsgInfo object contains all needful information for a engery consumer.
     */
    @Override
    public OADRMsgInfo extractInfo(OADRMsgObject obj, OADRParty party) {
        OadrRegisteredReport msg = (OadrRegisteredReport)obj.getMsg();
        MsgInfo_OADRRegisteredReport info = new MsgInfo_OADRRegisteredReport();

        MsgInfo_OADRCreateReport oadrCreateReport = new MsgInfo_OADRCreateReport();

        OADRConInfo.setVTNReceivesReportCapabilities(true);

        List<OadrReportRequest> reportRequests = msg.getOadrReportRequests();

        if(!reportRequests.isEmpty()){
            info.setOadrCreateReport(oadrCreateReport);
        }

        for(OadrReportRequest oadrReportRequest : reportRequests){
            MsgInfo_OADRCreateReport.ReportRequest reportRequest = oadrCreateReport.getNewReportRequest();


            reportRequest.setReportRequestID(oadrReportRequest.getReportRequestID());

            ReportSpecifier reportSpecifier = oadrReportRequest.getReportSpecifier();
            reportRequest.setReportSpecifierID(reportSpecifier.getReportSpecifierID());
            reportRequest.setGranularitySec(TimeDurationConverter.xCal2Seconds(reportSpecifier.getGranularity().getDuration().getValue()));
            reportRequest.setReportBackDurationSec(TimeDurationConverter.xCal2Seconds(reportSpecifier.getReportBackDuration().getDuration().getValue()));
            reportRequest.setReportIntervalStart(TimeDurationConverter.ical2Date(reportSpecifier.getReportInterval().getProperties().getDtstart().getDateTime().getValue().toString()));
            reportRequest.setReportIntervalDuration(TimeDurationConverter.xCal2Seconds(reportSpecifier.getReportInterval().getProperties().getDuration().getDuration().getValue()));

            for(SpecifierPayload oadrSpecifierPayload : reportSpecifier.getSpecifierPayloads()){
                MsgInfo_OADRCreateReport.SpecifierPayload specifierPayload = oadrCreateReport.getNewSpecifierPayload();

                specifierPayload.setrID(oadrSpecifierPayload.getRID());
                specifierPayload.setReadingType(oadrSpecifierPayload.getReadingType());

                reportRequest.getSpecifierPayloads().add(specifierPayload);
            }



            oadrCreateReport.getReportRequests().add(reportRequest);


            party.generateAsyncSendUpdateReportMsgWorker(oadrCreateReport.getReportRequests());
        }



        return info;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doRecMsgViolateConstraintsAndUpdateSendMap(OADRMsgObject obj, HashMap<String, OADRMsgObject> sendedMsgMap){
        if(OADRConInfo.getVENId() == null){
            return true;
        }

        OadrRegisteredReport recMsg = (OadrRegisteredReport)obj.getMsg();
        if(sendedMsgMap.get(recMsg.getEiResponse().getRequestID()) == null){
            return true;
        }

        OADRMsgObject originMsg = sendedMsgMap.get(recMsg.getEiResponse().getRequestID());
        if(!originMsg.getMsgType().equals("oadrRegisterReport")){
            return true;
        }

        sendedMsgMap.remove(recMsg.getEiResponse().getRequestID());


        if(!recMsg.getVenID().equals(OADRConInfo.getVENId())){
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRRegisteredReport().getMsgType();
    }
}
