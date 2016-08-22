package semanticCore.WebSocketHandling;

/**
 * Created by georg on 09.06.16.
 * This class holds all necessary colibri websocket communication information.
 */
public class WebSocketConInfo {

    public static String regConnectorAddress;
    public static String regTechnologyProtocolResourceName;
    public static String regRegisteredDescriptionAbout;
    public static String regTypeResource;

    public static String getRegConnectorAddress() {
        return regConnectorAddress;
    }

    public static void setRegConnectorAddress(String regConnectorAddress) {
        WebSocketConInfo.regConnectorAddress = regConnectorAddress;
    }

    public static String getRegTechnologyProtocolResourceName() {
        return regTechnologyProtocolResourceName;
    }

    public static void setRegTechnologyProtocolResourceName(String regTechnologyProtocolResourceName) {
        WebSocketConInfo.regTechnologyProtocolResourceName = regTechnologyProtocolResourceName;
    }

    public static String getRegRegisteredDescriptionAbout() {
        return regRegisteredDescriptionAbout;
    }

    public static void setRegRegisteredDescriptionAbout(String regRegisteredDescriptionAbout) {
        WebSocketConInfo.regRegisteredDescriptionAbout = regRegisteredDescriptionAbout;
    }

    public static String getRegTypeResource() {
        return regTypeResource;
    }

    public static void setRegTypeResource(String regTypeResource) {
        WebSocketConInfo.regTypeResource = regTypeResource;
    }
}
