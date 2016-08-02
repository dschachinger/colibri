package Utils;

import Bridge.OpenADRColibriBridge;
import com.google.gson.Gson;
import openADR.OADRHandling.OADR2VEN;
import openADR.OADRMsgInfo.MsgInfo_OADRDistributeEvent;
import openADR.OADRMsgInfo.Report;
import openADR.Utils.OADRConInfo;
import openADR.Utils.XMPPConInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semanticCore.WebSocketHandling.ColibriClient;
import semanticCore.WebSocketHandling.WebSocketConInfo;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by georg on 28.05.16.
 */
public class Main {
    static public Date testDate;

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String args[]){
        addExampleReportPossibility();

        OpenADRColibriBridge bridge = new OpenADRColibriBridge();

        OADR2VEN ven = initOpenADRVEN(bridge);;
        bridge.setOadrVEN(ven);

        ColibriClient colClient = initColibriService(bridge);
        bridge.setColClient(colClient);

        Scanner reader = new Scanner(System.in);  // Reading from System.in

        int n;
        loop : while (true){
            logger.info("Enter a action number: ");
            n = reader.nextInt();
            switch (n){
                case 1: logger.info("websocket registered: " + colClient.isRegistered());
                    break;
                case 2:
                    logger.info("colibri: send registration");
                    colClient.sendRegisterMessage();
                    break;
                case 3:
                    logger.info("ven: terminate");
                    ven.terminate();
                    break;
                case 4:
                    logger.info("colibri: terminate (can not used anymore)");
                    colClient.terminate();
                    break;
                case 5:
                    logger.info("colibri: send deregistration");
                    colClient.sendDeregisterMessage();
                    break;
                case 6:
                    logger.info("colibri: add price service");
                    colClient.sendAddService(EventType.PRICE);
                    break;
                case 7:
                    logger.info("colibri: add load service");
                    colClient.sendAddService(EventType.LOAD);
                    break;
                case 8:
                    logger.info("added services:");
                    for(String service : colClient.getServicesMap().keySet()){
                        if(colClient.getServicesMap().get(service).isServiceAdded()){
                            logger.info("\t"+service);
                        }
                    }
                    break;
                case 9:
                    logger.info("observed services:");
                    for(String serviceURL : colClient.getServicesMap().keySet()){
                        if(colClient.getServicesMap().get(serviceURL).isServiceObserved()){
                            logger.info("\t"+serviceURL);
                        }
                    }
                    break;
                case 10:
                    logger.info("colibri: query message");
                    logger.info("\tEnter the query (end query input with \"?!?\"):");
                    {
                        String in = "";
                        String buffer= reader.nextLine();
                        while (!buffer.equals("?!?")){
                            in +=buffer;
                            buffer = reader.nextLine();
                        }

                        colClient.sendQueryMessage(in);
                    }
                    break;
                case 11:
                    logger.info("colibri: update message");
                    logger.info("\tEnter the sparql-update (end sparql-update input with \"?!?\"):");
                    {
                        String in = "";
                        String buffer= reader.nextLine();
                        while (!buffer.equals("?!?")){
                            in +=buffer;
                            buffer = reader.nextLine();
                        }

                        colClient.sendUpdateMessage(in);
                    }
                break;
                case 51: ven.sendExampleOadrQueryRegistration();
                    break;
                case 52: ven.sendExampleOadrCreatePartyRegistration();
                    break;
                case 53: ven.sendExampleOadrCancelPartyRegistration();
                    break;
                case 54: ven.sendExampleOadrRequestEvent();
                    break;
                case 55:
                    System.out.println("registraionID: " + OADRConInfo.getRegistrationId());
                    break;
                case 56:
                    ven.sendExampleOadrRegisterReport();
                    break;
                case 57:
                    ven.sendExampleOadrUpdateReport();
                    break;
                case 58:
                    logger.info("openADR venID: " + OADRConInfo.getVENId());
                    break;
                default:
                    break loop;
            }
        }
    }

    private static OADR2VEN initOpenADRVEN(OpenADRColibriBridge bridge){
        Properties prop = new Properties();
        String fileName = "openADRConfig.properties";
        InputStream is = null;

        is = Main.class.getClassLoader().getResourceAsStream(fileName);
        try {
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        XMPPConInfo.VTNUsername = prop.getProperty("VTN.Username");
        XMPPConInfo.VTNPassword = prop.getProperty("VTN.Password");
        XMPPConInfo.VTNServiceName = prop.getProperty("VTN.ServiceName");
        XMPPConInfo.VTNRessourceeName = prop.getProperty("VTN.RessourceeName");
        XMPPConInfo.VENUsername = prop.getProperty("VEN.Username");
        XMPPConInfo.VENPassword = prop.getProperty("VEN.Password");
        XMPPConInfo.VENServiceName = prop.getProperty("VEN.ServiceName");
        XMPPConInfo.VENRessourceeName = prop.getProperty("VEN.RessourceeName");

        int timeoutSec = Integer.parseInt(prop.getProperty("msg.timeoutSec"));

        return new OADR2VEN(bridge, timeoutSec);
    }

    private static ColibriClient initColibriService(OpenADRColibriBridge bridge){
        Properties prop = new Properties();
        String fileName = "colibriCore.properties";
        InputStream is = null;

        is = Main.class.getClassLoader().getResourceAsStream(fileName);
        try {
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String serviceBaseURL = prop.getProperty("service.serviceBaseURL");
        int timeoutSec = Integer.parseInt(prop.getProperty("msg.timeoutSec"));

        WebSocketConInfo.regConnectorAddress = prop.getProperty("REG.connectorAddress");
        WebSocketConInfo.regTechnologyProtocolResourceName = prop.getProperty("REG.technologyProtocolResourceName");
        WebSocketConInfo.regRegisteredDescriptionAbout = prop.getProperty("REG.registeredDescriptionAbout");
        WebSocketConInfo.regTypeResource = prop.getProperty("REG.typeResource");

        return new ColibriClient(bridge, serviceBaseURL, timeoutSec);
    }

    private static void addExampleReportPossibility(){
        openADR.OADRMsgInfo.Report report = new openADR.OADRMsgInfo.Report();

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

/*
        Scanner reader = new Scanner(System.in);  // Reading from System.in

        OADR2VEN ven = new OADR2VEN(bridge);

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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            testDate = sdf.parse("2016-06-06T11:11:11Z");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date min1 = TimeDurationConverter.addDurationToDate(testDate, 60);
        Date min2 = TimeDurationConverter.addDurationToDate(testDate, 120);
        Date min3 = TimeDurationConverter.addDurationToDate(testDate, 180);
        Date min4 = TimeDurationConverter.addDurationToDate(testDate, 240);
        Date min5 = TimeDurationConverter.addDurationToDate(testDate, 360);
        Date min6 = TimeDurationConverter.addDurationToDate(testDate, 420);
        Date min7 = TimeDurationConverter.addDurationToDate(testDate, 480);
        Date min8 = TimeDurationConverter.addDurationToDate(testDate, 540);

        // logger.info("aussehen: " + sdf.format(min1));

        MsgInfo_OADRDistributeEvent msgInfo_oadrDistributeEvent = new MsgInfo_OADRDistributeEvent();
        Pair<Date, Date> inter1 = new Pair<>(min1, min2);
        MsgInfo_OADRDistributeEvent.Event msg1 = msgInfo_oadrDistributeEvent.getNewEvent();
        msg1.setEventID("1");
        Pair<Date, Date> inter2 = new Pair<>(min3, min4);
        MsgInfo_OADRDistributeEvent.Event msg2 = msgInfo_oadrDistributeEvent.getNewEvent();
        msg2.setEventID("2");
        Pair<Date, Date> inter3 = new Pair<>(min5, min6);
        MsgInfo_OADRDistributeEvent.Event msg3 = msgInfo_oadrDistributeEvent.getNewEvent();
        msg3.setEventID("3");

        /*Pair<Date, Date> searchInter1 = new Pair<>(min1,min6);
        Pair<Date, Date> searchInter2 = new Pair<>(min2,min6);
        Pair<Date, Date> searchInter3 = new Pair<>(min3,min4);
        Pair<Date, Date> searchInter4 = new Pair<>(min3,min5);
        Pair<Date, Date> searchInter5 = new Pair<>(min3,null);
        Pair<Date, Date> searchInter6 = new Pair<>(null,min5);

        bridge.addOpenADREvent("http://www.colibri.org/openADRConnector/Price/Service", inter1, msg1);
        bridge.addOpenADREvent("http://www.colibri.org/openADRConnector/Price/Service", inter2, msg2);
        bridge.addOpenADREvent("http://www.colibri.org/openADRConnector/Price/Service", inter3, msg3);

        bridge.getOpenADREvents("test", searchInter1);
        bridge.getOpenADREvents("test", searchInter2);
        bridge.getOpenADREvents("test", searchInter3);
        bridge.getOpenADREvents("test", searchInter4);
        bridge.getOpenADREvents("test", searchInter5);
        bridge.getOpenADREvents("test", searchInter6);*/

        /*TimeoutWatcher timeoutWatcher = new TimeoutWatcher(10000);
        timeoutWatcher.addMonitoredMsg("1");
        timeoutWatcher.addMonitoredMsg("2");
        timeoutWatcher.replyReceivedForMsg("2");
        timeoutWatcher.addMonitoredMsg("3");


//System.exit(1);

 */