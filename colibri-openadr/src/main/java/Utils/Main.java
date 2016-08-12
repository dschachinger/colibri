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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by georg on 28.05.16.
 */
public class Main {
    static public Date testDate;

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    private static OADR2VEN ven;
    private static ColibriClient colClient;

    private static AtomicBoolean alreadyTerminated = new AtomicBoolean(false);

    public static void main(String args[]){
        addExampleReportPossibility();

        final OpenADRColibriBridge bridge = new OpenADRColibriBridge();

        ven = initOpenADRVEN(bridge);
        bridge.setOadrVEN(ven);

        colClient = initColibriService(bridge);
        bridge.setColClient(colClient);

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                logger.info("Shutdown hook ran!");
                Main.shutdown();
            }
        });

        Scanner reader = new Scanner(System.in);  // Reading from System.in

        int n;
        loop : while (true){
            logger.info("Enter an action number: ");
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
                    // good possibility to check if correctly registered
                    logger.info("registraionID: " + OADRConInfo.getRegistrationId());
                    break;
                case 56:
                    ven.sendExampleOadrRegisterReport();
                    break;
                case 57:
                    ven.sendExampleOadrUpdateReport();
                    break;
                case 58:
                    // good possibility to check if correctly registered
                    logger.info("openADR venID: " + OADRConInfo.getVENId());
                    break;
                default:
                    break loop;
            }
        }

        Main.shutdown();
    }

    private static void shutdown(){
        if(alreadyTerminated.compareAndSet(false, true)){
            Main.ven.terminate();
            // give colibri client termination thread time to terminate
            try {
                Main.colClient.terminate().join();
            } catch (InterruptedException e) {
                logger.error("can not shut down the colibri client properly");
            }
            logger.info("finished shutdown task");
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
        String colibriCoreURL = prop.getProperty("colibriCore.url");
        int timeoutSec = Integer.parseInt(prop.getProperty("msg.timeoutSec"));

        WebSocketConInfo.regConnectorAddress = prop.getProperty("REG.connectorAddress");
        WebSocketConInfo.regTechnologyProtocolResourceName = prop.getProperty("REG.technologyProtocolResourceName");
        WebSocketConInfo.regRegisteredDescriptionAbout = prop.getProperty("REG.registeredDescriptionAbout");
        WebSocketConInfo.regTypeResource = prop.getProperty("REG.typeResource");

        return new ColibriClient(bridge, serviceBaseURL, colibriCoreURL, timeoutSec);
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