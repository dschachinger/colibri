package channel.message.colibriMessage;

public enum MessageIdentifier {
    STA("STA"),
    REG("REG"),
    DRE("DRE"),
    ADD("ADD"),
    OBS("OBS"),
    DET("DET"),
    REM("REM"),
    PUT("PUT");

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
