import OADRHandling.OADR2VEN;
import OADRMsgInfo.Report;
import Utils.OADRConInfo;
import Utils.XMPPConInfo;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by georg on 28.05.16.
 */
public class Main {
    public static void main(String args[]){
        initXMPPConfInfo();
        addExampleReportPossibility();

        Scanner reader = new Scanner(System.in);  // Reading from System.in

        OADR2VEN ven = new OADR2VEN();

        int n;
        loop : while (true){
            System.out.println("Enter a action number: ");
            n = reader.nextInt();
            switch (n){
                case 1: ven.sendExampleOadrQueryRegistration();
                    break;
                case 2: ven.sendExampleOadrCreatePartyRegistration();
                    break;
                case 3: ven.sendExampleOadrCancelPartyRegistration();
                    break;
                case 4: ven.sendExampleOadrRequestEvent();
                    break;
                case 5:
                    System.out.println("registraionID: " + OADRConInfo.getRegistrationId());
                    break;
                case 6:
                    ven.sendExampleOadrRegisterReport();
                    break;
                case 7:
                    ven.sendExampleOadrUpdateReport();
                    break;
                default:
                    break loop;
            }
        }

        ven.terminate();
    }

    private static void initXMPPConfInfo(){
        Properties prop = new Properties();
        String fileName = "openADRConfig.properties";
        InputStream is = null;

        is = Main.class.getClassLoader().getResourceAsStream(fileName);
        try {
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("test: " + prop.getProperty("VTN.Username"));

        XMPPConInfo.VTNUsername = prop.getProperty("VTN.Username");
        XMPPConInfo.VTNPassword = prop.getProperty("VTN.Password");
        XMPPConInfo.VTNServiceName = prop.getProperty("VTN.ServiceName");
        XMPPConInfo.VTNRessourceeName = prop.getProperty("VTN.RessourceeName");
        XMPPConInfo.VENUsername = prop.getProperty("VEN.Username");
        XMPPConInfo.VENPassword = prop.getProperty("VEN.Password");
        XMPPConInfo.VENServiceName = prop.getProperty("VEN.ServiceName");
        XMPPConInfo.VENRessourceeName = prop.getProperty("VEN.RessourceeName");
    }

    private static void addExampleReportPossibility(){
        Report report = new Report();

        report.setReportSpecifierID("RS_12345");
        report.setReportName("TELEMETRY_USAGE");
        report.setDurationSec(1800);
        report.setCreatedDateTime(new Date());

        Report.ReportDescription reportDescription = report.getNewReportDescription();

        reportDescription.setrID("123");
        reportDescription.setReportType("usage");
        reportDescription.setReadingType("Direct Read");
        reportDescription.setMarketContext("http://www.myprogram.com");

        Report.PowerReal powerReal = report.getNewPowerReal();
        powerReal.setItemDescription("RealPower");
        powerReal.setItemUnits("W");
        powerReal.setPowerAttributesAC(true);
        powerReal.setPowerAttributesHertz(new BigDecimal(50));
        powerReal.setPowerAttributesVoltage(new BigDecimal(230));
        powerReal.setSiScaleCode("k");

        reportDescription.setPowerReal(powerReal);

        Report.SamplingRate samplingRate = report.getNewSamplingRate();

        samplingRate.setMinPeriondSec(900);
        samplingRate.setMaxPeriondSec(900);
        samplingRate.setOnChange(false);

        reportDescription.setSamplingRate(samplingRate);

        report.getReportDescriptions().add(reportDescription);

        OADRConInfo.addReportPossibility(report);

    }
}
