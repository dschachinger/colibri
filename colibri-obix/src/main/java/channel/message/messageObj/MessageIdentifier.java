package channel.message.messageObj;

/**
 * This enum represents the message identifiers used in {@link channel.message.colibriMessage.ColibriMessage}.
 */
public enum MessageIdentifier {
    STA("STA"),
    REG("REG"),
    DRE("DRE"),
    ADD("ADD"),
    OBS("OBS"),
    DET("DET"),
    REM("REM"),
    PUT("PUT"),
    GET("GET"),
    QUE("QUE"),
    QRE("QRE"),
    UPD("UPD");

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    private String type;

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    MessageIdentifier(String type) {
        this.type = type;
    }

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public String getIdentifier() {
        return type;
    }

    public static MessageIdentifier fromString(String text) {
        if (text != null) {
            for (MessageIdentifier identifier : MessageIdentifier.values()) {
                if (text.equalsIgnoreCase(identifier.getIdentifier())) {
                    return identifier;
                }
            }
        }
        return STA;
    }
}
