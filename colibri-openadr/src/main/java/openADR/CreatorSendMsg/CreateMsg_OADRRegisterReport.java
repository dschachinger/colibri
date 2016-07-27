package openADR.CreatorSendMsg;

import Utils.TimeDurationConverter;
import com.enernoc.open.oadr2.model.v20b.OadrRegisterReport;
import com.enernoc.open.oadr2.model.v20b.OadrReport;
import com.enernoc.open.oadr2.model.v20b.OadrReportDescription;
import com.enernoc.open.oadr2.model.v20b.OadrSamplingRate;
import com.enernoc.open.oadr2.model.v20b.ei.EiTarget;
import com.enernoc.open.oadr2.model.v20b.emix.MarketContext;
import com.enernoc.open.oadr2.model.v20b.power.MeterAsset;
import com.enernoc.open.oadr2.model.v20b.power.ObjectFactory;
import com.enernoc.open.oadr2.model.v20b.power.PowerAttributes;
import com.enernoc.open.oadr2.model.v20b.power.PowerRealType;
import com.enernoc.open.oadr2.model.v20b.xcal.DateTime;
import com.enernoc.open.oadr2.model.v20b.xcal.DurationValue;
import openADR.OADRMsgInfo.MsgInfo_OADRRegisterReport;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.OADRMsgInfo.Report;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by georg on 07.06.16.
 * This class is used to create oadrRegisterReport messages.
 */
public class CreateMsg_OADRRegisterReport extends CreateSendMsg {

    /**
     * Creates a message object with an openADR payload OadrRegisterReport in it.
     * @param info message info: contains the needed information to create a openADR payload
     * @param receivedMsgMap contains all received messages
     * @return
     */
    @Override
    public OADRMsgObject genSendMsg(OADRMsgInfo info, HashMap<String, OADRMsgInfo> receivedMsgMap) {
        MsgInfo_OADRRegisterReport con_info = (MsgInfo_OADRRegisterReport) info;

        OadrRegisterReport msg = new OadrRegisterReport();
        String reqID = OADRConInfo.getUniqueRequestId();

        msg.setSchemaVersion("2.0b");

        msg.setRequestID(reqID);

        msg.getOadrReports().addAll(listAllOadrReportCapabilities(con_info.getReports()));

        OADRMsgObject obj = new OADRMsgObject(info.getMsgType(), reqID, msg);

        return obj;
    }

    public static List<OadrReport> listAllOadrReportCapabilities(List<Report> reports){
        List<OadrReport> oadrReports = new ArrayList<>();
        for(Report report : reports){
            OadrReport oadrReport = new OadrReport();

            DateTime dateTime = new DateTime();
            dateTime.setValue(TimeDurationConverter.date2Ical(report.getCreatedDateTime()));
            oadrReport.setCreatedDateTime(dateTime);

            oadrReport.setReportRequestID(report.getReportRequestID());
            oadrReport.setReportSpecifierID(report.getReportSpecifierID());
            oadrReport.setReportName(report.getReportName());

            for(Report.ReportDescription reportDescription : report.getReportDescriptions()){
                OadrReportDescription oadrReportDescription = new OadrReportDescription();
                MarketContext marketContext = new MarketContext();
                marketContext.setValue(reportDescription.getMarketContext());
                oadrReportDescription.setMarketContext(marketContext);

                oadrReportDescription.setRID(reportDescription.getrID());

                EiTarget eiTarget = new EiTarget();
                MeterAsset meterAsset = new MeterAsset();
                meterAsset.setMrid(reportDescription.getReportDataSource());
                eiTarget.getMeterAssets().add(meterAsset);
                oadrReportDescription.setReportDataSource(eiTarget);

                oadrReportDescription.setReportType(reportDescription.getReportType());

                oadrReportDescription.setReadingType(reportDescription.getReadingType());

                OadrSamplingRate oadrSamplingRate = new OadrSamplingRate();
                DurationValue minPeriodDuration = new DurationValue();
                minPeriodDuration.setValue(TimeDurationConverter.createXCalString(reportDescription.getSamplingRate().getMinPeriondSec()));
                oadrSamplingRate.setOadrMinPeriod(minPeriodDuration);
                DurationValue maxPeriodDuration = new DurationValue();
                maxPeriodDuration.setValue(TimeDurationConverter.createXCalString(reportDescription.getSamplingRate().getMaxPeriondSec()));
                oadrSamplingRate.setOadrMaxPeriod(maxPeriodDuration);
                oadrSamplingRate.setOadrOnChange(reportDescription.getSamplingRate().isOnChange());

                oadrReportDescription.setOadrSamplingRate(oadrSamplingRate);

                PowerRealType powerRealType = new PowerRealType();
                powerRealType.setItemDescription(reportDescription.getPowerReal().getItemDescription());
                powerRealType.setItemUnits(reportDescription.getPowerReal().getItemUnits());
                powerRealType.setSiScaleCode(reportDescription.getPowerReal().getSiScaleCode());
                PowerAttributes powerAttributes = new PowerAttributes();
                powerAttributes.setAc(reportDescription.getPowerReal().isPowerAttributesAC());
                powerAttributes.setHertz(reportDescription.getPowerReal().getPowerAttributesHertz());
                powerAttributes.setVoltage(reportDescription.getPowerReal().getPowerAttributesVoltage());

                com.enernoc.open.oadr2.model.v20b.power.ObjectFactory objectFactory = new ObjectFactory();
                JAXBElement<PowerRealType> powerItemTypeJAXBElement = objectFactory.createPowerReal(powerRealType);
                oadrReportDescription.setItemBase(powerItemTypeJAXBElement);

                oadrReport.getOadrReportDescriptions().add(oadrReportDescription);
            }


            oadrReports.add(oadrReport);

        }

        return oadrReports;
    }

    /**
     * This method returns the message type name for an oadrRegisterReport message
     * @return supported messege type
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRRegisterReport().getMsgType();
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
