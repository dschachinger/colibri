package CreatorSendMsg;

import OADRMsgInfo.MsgInfo_OADRUpdateReport;
import OADRMsgInfo.Report;
import OADRMsgInfo.Interval;
import OADRMsgInfo.OADRMsgInfo;
import Utils.OADRConInfo;
import Utils.OADRMsgObject;
import Utils.TimeDurationConverter;
import com.enernoc.open.oadr2.model.v20b.OadrReport;
import com.enernoc.open.oadr2.model.v20b.OadrReportPayloadType;
import com.enernoc.open.oadr2.model.v20b.OadrRequestEvent;
import com.enernoc.open.oadr2.model.v20b.OadrUpdateReport;
import com.enernoc.open.oadr2.model.v20b.ei.ObjectFactory;
import com.enernoc.open.oadr2.model.v20b.ei.PayloadFloatType;
import com.enernoc.open.oadr2.model.v20b.power.*;
import com.enernoc.open.oadr2.model.v20b.pyld.EiRequestEvent;
import com.enernoc.open.oadr2.model.v20b.strm.Intervals;
import com.enernoc.open.oadr2.model.v20b.xcal.*;

import javax.xml.bind.JAXBElement;
import java.util.HashMap;
import java.util.List;

/**
 * Created by georg on 07.06.16.
 * This class is used to create oadrRequestEvent messages.
 */
public class CreateMsg_OADRUpdateReport extends CreateSendMsg {

    /**
     * Creates a message object with an openADR payload OadrRequestEvent in it.
     * @param info message info: contains the needed information to create a openADR payload
     * @param receivedMsgMap contains all received messages
     * @return
     */
    @Override
    public OADRMsgObject genSendMsg(OADRMsgInfo info, HashMap<String, OADRMsgInfo> receivedMsgMap) {
        MsgInfo_OADRUpdateReport con_info = (MsgInfo_OADRUpdateReport) info;

        OadrUpdateReport msg = new OadrUpdateReport();
        String reqID = OADRConInfo.getUniqueRequestId();

        msg.setSchemaVersion("2.0b");
        msg.setRequestID(reqID);
        List<OadrReport> oadrReports = msg.getOadrReports();


        // TODO what if there are more reports in it? It is possible to add only one OADRMsgObject due to the return mechanism


        for(Report report : con_info.getReports()){
            OadrReport oadrReport = new OadrReport();
            DateTime dateTime = new DateTime();
            dateTime.setValue(TimeDurationConverter.date2Ical(report.getCreatedDateTime()));
            System.out.println("time " + TimeDurationConverter.date2Ical(report.getCreatedDateTime()));

            oadrReport.setReportRequestID("RR_65432");
            Dtstart dtstart = new Dtstart();
            dtstart.setDateTime(dateTime);
            oadrReport.setDtstart(dtstart);
            DurationValue durationValue = new DurationValue();
            durationValue.setValue(TimeDurationConverter.createXCalString(report.getDurationSec()));
            DurationPropType durationPropType = new DurationPropType();
            durationPropType.setDuration(durationValue);
            oadrReport.setDuration(durationPropType);


            Intervals oadrIntervals = new Intervals();
            for(Interval interval : report.getIntervals() ){
                com.enernoc.open.oadr2.model.v20b.ei.Interval oadrdInterval = new com.enernoc.open.oadr2.model.v20b.ei.Interval();
                durationValue = new DurationValue();
                durationValue.setValue(TimeDurationConverter.createXCalString(interval.getDurationSec()));
                durationPropType = new DurationPropType();
                durationPropType.setDuration(durationValue);
                oadrdInterval.setDuration(durationPropType);
                Uid uid = new Uid();
                uid.setText(interval.getUid());
                oadrdInterval.setUid(uid);

                OadrReportPayloadType oadrReportPayloadType = new OadrReportPayloadType();
                oadrReportPayloadType.setRID("123");
                oadrReportPayloadType.setConfidence(new Long(95));
                oadrReportPayloadType.setAccuracy(new Float(0));
                PayloadFloatType payloadFloatType = new PayloadFloatType();
                payloadFloatType.setValue(20);

                ObjectFactory eiObjectFactory = new ObjectFactory();
                JAXBElement<PayloadFloatType> payloadFloatTypeJAXBElement = eiObjectFactory.createPayloadFloat(payloadFloatType);


                oadrReportPayloadType.setPayloadBase(payloadFloatTypeJAXBElement);
                oadrReportPayloadType.setOadrDataQuality("Quality Good - Non Specific");
                oadrReportPayloadType.setAccuracy(new Float(0.2));

                com.enernoc.open.oadr2.model.v20b.ObjectFactory oadrObjectFactory = new com.enernoc.open.oadr2.model.v20b.ObjectFactory();
                JAXBElement<OadrReportPayloadType> oadrReportPayloadTypeJAXBElement = oadrObjectFactory.createOadrReportPayload(oadrReportPayloadType);
                oadrdInterval.getStreamPayloadBases().add(oadrReportPayloadTypeJAXBElement);

                oadrIntervals.getIntervals().add(oadrdInterval);

            }

            oadrReport.setReportName(report.getReportName());
            oadrReport.setReportRequestID(report.getReportRequestID());
            oadrReport.setReportSpecifierID(report.getReportSpecifierID());
            oadrReport.setEiReportID(report.getReportRequestID());
            oadrReport.setIntervals(oadrIntervals);
            oadrReport.setCreatedDateTime(dateTime);

            oadrReports.add(oadrReport);
        }

        OADRMsgObject obj = new OADRMsgObject(info.getMsgType(), reqID, msg);

        return obj;
    }

    /**
     * This method returns the message type name for an oadrRequestEvent message
     * @return supported messege type
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRUpdateReport().getMsgType();
    }

    /**
     * {@inheritDoc}
     */
    public boolean doSendMsgViolateMsgOrderAndUpdateRecMap(OADRMsgInfo info, HashMap<String, OADRMsgInfo> receivedMsgMap){
        if(OADRConInfo.getVENId() == null){
            return true;
        }
        return false;
    }
}
