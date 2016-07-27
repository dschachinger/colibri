package Utils;

import com.google.gson.*;
import openADR.OADRHandling.OADR2VEN;
import openADR.OADRMsgInfo.MsgInfo_OADRDistributeEvent;
import openADR.OADRMsgInfo.Report;
import openADR.Utils.OADRConInfo;
import openADR.Utils.XMPPConInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import semanticCore.MsgObj.ContentMsgObj.QueryResult;
import semanticCore.MsgObj.ContentMsgObj.Result;
import semanticCore.WebSocketHandling.ColibriClient;
import semanticCore.WebSocketHandling.WebSocketConInfo;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Created by georg on 28.05.16.
 */
public class Main {
    static public Date testDate;

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String args[]){
        initXMPPConfInfo();
        initWebSocketConInfo();
        addExampleReportPossibility();

        OpenADRColibriBridge bridge = new OpenADRColibriBridge();



        // JSON parsen begin
        Gson g = new Gson();
        String cont = "{\n" +
                "\"head\": { \"vars\": [\"service\",\"identifier\"] },\n" +
                "\"results\": {\n" +
                "\"bindings\": [\n" +
                "{\n" +
                "\"service\" : { \"type\": \"uri\", \"value\": \"http://www.colibri-samples.org/service1\" },\n" +
                "\"identifier\" : { \"type\": \"literal\", \"value\": \"temp_monitoring_17\" }\n" +
                "}\n" +
                "]\n" +
                "}\n" +
                "}";

        cont = "{\n" +
                "  \"head\": { \"vars\": [ \"book\" , \"title\" ]\n" +
                "  } ,\n" +
                "  \"results\": { \n" +
                "    \"bindings\": [\n" +
                "      {\n" +
                "        \"book\": { \"type\": \"uri\" , \"value\": \"http://example.org/book/book6\" } ,\n" +
                "        \"title\": { \"type\": \"literal\" , \"value\": \"Harry Potter and the Half-Blood Prince\" }\n" +
                "      } ,\n" +
                "      {\n" +
                "        \"book\": { \"type\": \"uri\" , \"value\": \"http://example.org/book/book7\" } ,\n" +
                "        \"title\": { \"type\": \"literal\" , \"value\": \"Harry Potter and the Deathly Hallows\" }\n" +
                "      } ,\n" +
                "      {\n" +
                "        \"book\": { \"type\": \"uri\" , \"value\": \"http://example.org/book/book5\" } ,\n" +
                "        \"title\": { \"type\": \"literal\" , \"value\": \"Harry Potter and the Order of the Phoenix\" }\n" +
                "      } ,\n" +
                "      {\n" +
                "        \"book\": { \"type\": \"uri\" , \"value\": \"http://example.org/book/book4\" } ,\n" +
                "        \"title\": { \"type\": \"literal\" , \"value\": \"Harry Potter and the Goblet of Fire\" }\n" +
                "      } ,\n" +
                "      {\n" +
                "        \"book\": { \"type\": \"uri\" , \"value\": \"http://example.org/book/book2\" } ,\n" +
                "        \"title\": { \"type\": \"literal\" , \"value\": \"Harry Potter and the Chamber of Secrets\" }\n" +
                "      } ,\n" +
                "      {\n" +
                "        \"book\": { \"type\": \"uri\" , \"value\": \"http://example.org/book/book3\" } ,\n" +
                "        \"title\": { \"type\": \"literal\" , \"value\": \"Harry Potter and the Prisoner Of Azkaban\" }\n" +
                "      } ,\n" +
                "      {\n" +
                "        \"book\": { \"type\": \"uri\" , \"value\": \"http://example.org/book/book1\" } ,\n" +
                "        \"title\": { \"type\": \"literal\" , \"value\": \"Harry Potter and the Philosopher's Stone\" }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        cont = "{ \n" +
                "  \"head\" : { } ,\n" +
                "  \"boolean\" : true\n" +
                "}";

        cont = "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n" +
                "\n" +
                "  <head>\n" +
                "    <variable name=\"x\"/>\n" +
                "    <variable name=\"hpage\"/>\n" +
                "    <variable name=\"name\"/>\n" +
                "    <variable name=\"age\"/>\n" +
                "    <variable name=\"mbox\"/>\n" +
                "    <variable name=\"friend\"/>\n" +
                "  </head>\n" +
                "\n" +
                "  <results>\n" +
                "\n" +
                "    <result> \n" +
                "      <binding name=\"x\">\n" +
                "\t<bnode>r2</bnode>\n" +
                "      </binding>\n" +
                "      <binding name=\"hpage\">\n" +
                "\t<uri>http://work.example.org/bob/</uri>\n" +
                "      </binding>\n" +
                "      <binding name=\"name\">\n" +
                "\t<literal xml:lang=\"en\">Bob</literal>\n" +
                "      </binding>\n" +
                "      <binding name=\"age\">\n" +
                "\t<literal datatype=\"http://www.w3.org/2001/XMLSchema#integer\">30</literal>\n" +
                "      </binding>\n" +
                "      <binding name=\"mbox\">\n" +
                "\t<uri>mailto:bob@work.example.org</uri>\n" +
                "      </binding>\n" +
                "    </result>\n" +
                "\n" +
                "  </results>\n" +
                "\n" +
                "</sparql>";

        logger.info(cont + "\n\n");

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

        ColibriClient colClient = new ColibriClient(bridge, initColibriService());
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
        */
/*
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
*/

        //System.exit(1);

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
                    for(String service : colClient.getKnownServicesHashMap().keySet()){
                        logger.info("\t"+service);
                    }
                    break;
                case 9:
                    logger.info("observed services:");
                    for(String serviceURL : colClient.getKnownServicesHashMap().keySet()){
                        if(colClient.getKnownServicesHashMap().get(serviceURL).isServiceObserved()){
                            logger.info("\t"+serviceURL);
                        }
                    }
                    break;
                case 10:
                    logger.info("colibri: query message");
                    logger.info("\tEnter the query (end query input with \"?!?\"):");
                    String in = "";
                    String buffer= reader.nextLine().trim();
                    while (!buffer.equals("?!?")){
                        in +=buffer;
                        buffer = reader.nextLine().trim();
                    }

                    colClient.sendQueryMessage(in);
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

        XMPPConInfo.VTNUsername = prop.getProperty("VTN.Username");
        XMPPConInfo.VTNPassword = prop.getProperty("VTN.Password");
        XMPPConInfo.VTNServiceName = prop.getProperty("VTN.ServiceName");
        XMPPConInfo.VTNRessourceeName = prop.getProperty("VTN.RessourceeName");
        XMPPConInfo.VENUsername = prop.getProperty("VEN.Username");
        XMPPConInfo.VENPassword = prop.getProperty("VEN.Password");
        XMPPConInfo.VENServiceName = prop.getProperty("VEN.ServiceName");
        XMPPConInfo.VENRessourceeName = prop.getProperty("VEN.RessourceeName");
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