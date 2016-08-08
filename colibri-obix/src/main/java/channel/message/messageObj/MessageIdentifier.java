package channel.message.messageObj;

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

    private String type;

    MessageIdentifier(String type) {
        this.type = type;
    }

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
