package ProcessorReceivedMsg;

import OADRHandling.AsyncSendUpdateReportMsgWorker;
import OADRHandling.OADRParty;
import OADRMsgInfo.*;
import OADRMsgInfo.OADRMsgInfo;
import Utils.OADRConInfo;
import Utils.OADRMsgObject;
import Utils.TimeDurationConverter;
import com.enernoc.open.oadr2.model.v20b.*;
import com.enernoc.open.oadr2.model.v20b.ei.ReportSpecifier;
import com.enernoc.open.oadr2.model.v20b.ei.SpecifierPayload;

import java.util.HashMap;
import java.util.List;

/**
 * Created by georg on 07.06.16.
 * This class is used to handle the receipt of openADR message type oadrCanceledPartyRegistration.
 */
public class Process_OADRCreateReport extends ProcessorReceivedMsg {

    /**
     * This method generates the proper reply for a openADR message OadrCanceledPartyRegistration.
     * Return null, because there is no need to reply to this type of message.
     * @param obj generate reply for this message. The contained message type has to be OadrCanceledPartyRegistration.
     * @return proper reply
     */
    @Override
    public OADRMsgObject genResponse(OADRMsgObject obj) {
        OadrCreateReport recMsg = (OadrCreateReport)obj.getMsg();

        List<OadrReportRequest> reportRequests = recMsg.getOadrReportRequests();

        OadrCreatedReport response = new OadrCreatedReport();

        response.setEiResponse(genEiRespone(recMsg.getRequestID()));

        OadrPendingReports oadrPendingReports = new OadrPendingReports();


        for(String reportRequestID : OADRConInfo.getAllRegisteredReportRequestIDs()){
            oadrPendingReports.getReportRequestIDs().add(reportRequestID);
        }
        response.setOadrPendingReports(oadrPendingReports);

        // TODO “METADATA” as the reportSpecifierID is also allowed in an createReport message oadr spec page 37
        return new OADRMsgObject("oadrCreatedReport", null, response);
    }

    /**
     * This method returns an MsgInfo_OADRCanceledPartyRegistration object.
     * This object contains all needful information for a engery consumer from an OadrCanceledPartyRegistration message.
     * @param obj extract inforation out of this message object. The contained message type has to be OadrCanceledPartyRegistration.
     * @param party
     * @return  The OADRMsgInfo object contains all needful information for a engery consumer.
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


        // start new thread
        for(MsgInfo_OADRCreateReport.ReportRequest reportRequest : info.getReportRequests()){
            AsyncSendUpdateReportMsgWorker asyncSendUpdateReportMsgWorker = new AsyncSendUpdateReportMsgWorker(reportRequest, party);
            OADRConInfo.addUpdateReportMsgWorker(asyncSendUpdateReportMsgWorker);
            asyncSendUpdateReportMsgWorker.start();
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

        OadrCreateReport recMsg = (OadrCreateReport)obj.getMsg();

        // TODO checken, ob vorher report registrierung stattgefunden hat

        // TODO checken, ob richtiger nachrichtentyp es ist
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
        return new MsgInfo_OADRCreateReport().getMsgType();
    }
}
