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

        printHelp();

        int n;
        loop : while (true){
            logger.info("\n ___      ___  ___  __           __  ___    __                           __   ___  __   \n" +
                        "|__  |\\ |  |  |__  |__)     /\\  /  `  |  | /  \\ |\\ |    |\\ | |  |  |\\/| |__) |__  |__) .\n" +
                        "|___ | \\|  |  |___ |  \\    /~~\\ \\__,  |  | \\__/ | \\|    | \\| \\__/  |  | |__) |___ |  \\ .\n" +
                        "                                                                                        ");
            n = reader.nextInt();
            switch (n){
                case 0: printHelp();
                case 1: logger.info("colibri registered: " + colClient.isRegistered());
                    break;
                case 2:
                    logger.info("colibri: send registration");
                    colClient.sendRegisterMessage();
                    break;
                case 3:
                    logger.info("colibri: send deregistration");
                    colClient.sendDeregisterMessage();
                    break;
                case 4:
                    logger.info("colibri: add price service");
                    colClient.sendAddService(EventType.PRICE);
                    break;
                case 5:
                    logger.info("colibri: add load service");
                    colClient.sendAddService(EventType.LOAD);
                    break;
                case 6:
                    logger.info("added services:");
                    for(String service : colClient.getServicesMap().keySet()){
                        if(colClient.getServicesMap().get(service).isServiceAdded()){
                            logger.info("\t"+service);
                        }
                    }
                    break;
                case 7:
                    logger.info("observed services:");
                    for(String serviceURL : colClient.getServicesMap().keySet()){
                        if(colClient.getServicesMap().get(serviceURL).isServiceObserved()){
                            logger.info("\t"+serviceURL);
                        }
                    }
                    break;
                case 8:
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
                case 9:
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
                case 10:
                    logger.info("colibri: terminate (can not used anymore)");
                    colClient.terminate();
                    break;
                case 21:
                    // good possibility to check if correctly registered
                    logger.info("registraionID: " + OADRConInfo.getRegistrationId());
                    break;
                case 22:
                    // good possibility to check if correctly registered
                    logger.info("openADR venID: " + OADRConInfo.getVENId());
                    break;
                case 23: ven.sendExampleOadrQueryRegistration();
                    break;
                case 24: ven.sendExampleOadrCreatePartyRegistration();
                    break;
                case 25: ven.sendExampleOadrCancelPartyRegistration();
                    break;
                case 26: ven.sendExampleOadrRequestEvent();
                    break;
                case 27:
                    ven.sendExampleOadrRegisterReport();
                    break;
                case 28:
                    ven.sendExampleOadrUpdateReport();
                    break;
                case 29:
                    logger.info("ven: terminate (can not used anymore)");
                    ven.terminate();
                    break;
                default:
                    break loop;
            }
        }

        Main.shutdown();
    }

    private static void printHelp(){
        String bar = "--------------------------------------------------------------------";
        String actions = "      __  ___    __        __   \n" +
                " /\\  /  `  |  | /  \\ |\\ | /__` .\n" +
                "/~~\\ \\__,  |  | \\__/ | \\| .__/ .\n" +
                "                                ";
        String colibri_part =
                "\t\t1:\tprint if connector is registered\n" +
                "\t\t2:\tregister the connector\n" +
                "\t\t3:\tderegister the connector\n" +
                "\t\t4:\tsend an add message for price events to the core\n" +
                "\t\t5:\tsend an add message for load events to the core\n" +
                "\t\t6:\tshow the added services\n" +
                "\t\t7:\tshow the observed services\n" +
                "\t\t8:\tsend query message to the core\n" +
                "\t\t9:\tsend update message to the core\n" +
                "\t\t10:\tterminate the connector\n";

        String openADR_part =
                "\t\t21:\tprint the registration id\n" +
                "\t\t22:\tprint the ven id\n" +
                "\t\t23:\tquery the VTN about registration information\n" +
                "\t\t24:\tregister the VEN on the VTN party\n" +
                "\t\t25:\tderegister the VEN on the VTN party\n" +
                "\t\t26:\trequest for new events\n" +
                "\t\t27:\tregister the VEN report possibilities on the VTN party\n" +
                "\t\t28:\tsend new report data to the VTN party\n" +
                "\t\t29:\tterminate the VEN\n";

        logger.info("\n"+bar+"\n"+actions+"\n\t\t0:\tprints this help page again\n\n"+"\tcolibri actions:\n"+colibri_part+"\n\topenADR actions:\n"+openADR_part+"\n"+bar);
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