import OADRHandling.OADR2VEN;
import Utils.OADRConInfo;
import Utils.XMPPConInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by georg on 28.05.16.
 */
public class Main {
    public static void main(String args[]){
        initXMPPConfInfo();


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
                default:
                    break loop;

            }
        }

        try {
            Thread.sleep(200000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
}
