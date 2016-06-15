package Utils;

/**
 * Created by georg on 09.06.16.
 * This class holds all necessary openADR communication information.
 */
public class OADRConInfo {
    private static final String VENName = "colibriVen";

    // The assigned ven ID by the VTN
    private static String VENId;
    // The assigned registrationId by the VTN
    private static String registrationId;
    // Counter to generate a unique requestID
    private static int requestCounter = 0;

    /**
     * This method returns the next unique requestID
     * @return
     */
    public static String getUniqueRequestId() {
        if(requestCounter == Integer.MAX_VALUE){
            requestCounter = 0;
        }

        requestCounter++;

        return "request_"+requestCounter;
    }

    public static String getRegistrationId() {
        return registrationId;
    }

    public static void setRegistrationId(String registrationId) {
        OADRConInfo.registrationId = registrationId;
    }

    public static String getVENId() {
        return VENId;
    }

    public static void setVENId(String VENId) {
        OADRConInfo.VENId = VENId;
    }

    public static String getVENName() {
        return VENName;
    }
}
