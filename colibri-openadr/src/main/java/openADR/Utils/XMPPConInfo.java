package openADR.Utils;

/**
 * Created by georg on 09.06.16.
 * This class holds all necessary openADR.XMPP communication information.
 */
public class XMPPConInfo {
    // This specifies the xmpp username of the VTN party
    public static String VTNUsername;
    // This specifies the xmpp password of the VTN party
    public static String VTNPassword;
    // This specifies the xmpp service name (=server address) of the VTN party
    public static String VTNServiceName;
    // This specifies the xmpp ressource name of the VTN party
    public static String VTNRessourceeName;
    // This specifies the xmpp username of the VEN party
    public static String VENUsername;
    // This specifies the xmpp password of the VEN party
    public static String VENPassword;
    // This specifies the xmpp service name (=server address) of the VEN party
    public static String VENServiceName;
    // This specifies the xmpp ressource name of the VEN party
    public static String VENRessourceeName;

    public static String getVTNFullAdrName() {
        return VTNUsername+"@"+VTNServiceName+"/"+VTNRessourceeName;
    }

    public static String getVENFullAdrName() {
        return VENUsername+"@"+VENServiceName+"/"+VENRessourceeName;
    }
}
