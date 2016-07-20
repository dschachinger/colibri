package Utils;

import openADR.OADRHandling.OADR2VEN;
import openADR.OADRMsgInfo.MsgInfo_OADRDistributeEvent;
import openADR.OADRMsgInfo.Report;
import openADR.Utils.OADRConInfo;
import openADR.Utils.XMPPConInfo;
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

    public static void main(String args[]){
        initXMPPConfInfo();
        initWebSocketConInfo();
        addExampleReportPossibility();

        OpenADRColibriBridge bridge = new OpenADRColibriBridge();

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

        */

        TimeDurationConverter.addDurationToDate(new Date(), 60);


        OADR2VEN ven = new OADR2VEN(bridge);
        bridge.setOadrVEN(ven);

        ColibriClient colClient = new ColibriClient(bridge);
        colClient.setServiceBaseURL(initColibriService());
        bridge.setColClient(colClient);

        Scanner reader = new Scanner(System.in);  // Reading from System.in

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

        System.out.println("aussehen: " + sdf.format(min1));

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
        */

        bridge.addOpenADREvent("http://www.colibri.org/openADRConnector/Price/Service", inter1, msg1);
        bridge.addOpenADREvent("http://www.colibri.org/openADRConnector/Price/Service", inter2, msg2);
        bridge.addOpenADREvent("http://www.colibri.org/openADRConnector/Price/Service", inter3, msg3);
/*
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
*/

        //System.exit(1);

        int n;
        loop : while (true){
            System.out.print("Enter a action number: ");
            n = reader.nextInt();
            switch (n){
                case 1: System.out.println("websocket registered: " + colClient.isRegistered());
                    break;
                case 2:
                    System.out.println("colibri: send registration");
                    colClient.sendRegisterMessage();
                    break;
                case 3:
                    System.out.println("ven: terminate");
                    ven.terminate();
                    break;
                case 4:
                    System.out.println("colibri: terminate (can not used anymore)");
                    colClient.terminate();
                    break;
                case 5:
                    System.out.println("colibri: send deregistration");
                    colClient.sendDeregisterMessage();
                    break;
                case 6:
                    System.out.println("colibri: add price service");
                    colClient.sendAddService(EventType.PRICE);
                    break;
                case 7:
                    System.out.println("colibri: add load service");
                    colClient.sendAddService(EventType.LOAD);
                    break;
                case 8:
                    System.out.println("added services:");
                    for(String service : colClient.getKnownServicesHashMap().keySet()){
                        System.out.println("\t"+service);
                    }
                    break;
                case 9:
                    System.out.println("observed services:");
                    for(String service : colClient.getObservedConnectorToColibriServices()){
                        System.out.println("\t"+service);
                    }
                    break;
                case 51: ven.sendExampleOadrQueryRegistration();
                    break;
                case 52: ven.sendExampleOadrCreatePartyRegistration();
                    break;
                default:
                    break loop;
            }
        }
    }

    private static void initWebSocketConInfo(){
        Properties prop = new Properties();
        String fileName = "webSocketConfig.properties";
        InputStream is = null;

        is = Main.class.getClassLoader().getResourceAsStream(fileName);
        try {
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("test: " + prop.getProperty("REG.registeredDescriptionAbout"));

        WebSocketConInfo.regConnectorAddress = prop.getProperty("REG.connectorAddress");
        WebSocketConInfo.regTechnologyProtocolResourceName = prop.getProperty("REG.technologyProtocolResourceName");
        WebSocketConInfo.regRegisteredDescriptionAbout = prop.getProperty("REG.registeredDescriptionAbout");
        WebSocketConInfo.regTypeResource = prop.getProperty("REG.typeResource");
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

        System.out.println("out: " + XMPPConInfo.getVTNFullAdrName() + " part " + XMPPConInfo.VTNUsername);
    }

    private static String initColibriService(){
        Properties prop = new Properties();
        String fileName = "colibriCore.properties";
        InputStream is = null;

        is = Main.class.getClassLoader().getResourceAsStream(fileName);
        try {
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return prop.getProperty("service.serviceBaseURL");
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
        int n;
        loop : while (true){
            System.out.println("Enter a msg type number: ");
            n = reader.nextInt();
            switch (n){
                case 1:
                    genSendMessage.send_REGISTER();
                    break;
                case 2:
                    genSendMessage.send_DEREGISTER();
                    break;
                case 3:
                    genSendMessage.send_ADD_SERVICE();
                    break;
                case 4:
                    genSendMessage.send_REMOVE_SERVICE();
                    break;
                case 5:
                    genSendMessage.send_OBSERVE_SERVICE();
                    break;
                case 6:
                    genSendMessage.send_DETACH_OBSERVATION();
                    break;
                case 7:
                    genSendMessage.send_PUT_DATA_VALUES();
                    break;
                case 8:
                    genSendMessage.send_GET_DATA_VALUES();
                    break;
                case 9:
                    genSendMessage.send_QUERY();
                    break;
                case 10:
                    genSendMessage.send_UPDATE();
                    break;
                case 11:
                    genSendMessage.send_STATUS();
                    break;
                default:
                    break loop;
            }
        }

        socket.close();
*/