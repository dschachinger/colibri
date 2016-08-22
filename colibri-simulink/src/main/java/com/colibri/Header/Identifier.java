package com.colibri.Header;
// This class provides the message identifiers for recognizing the message
/**
 * Created by codelife on 12/8/16.
 */
public enum Identifier {
    STA("STA"),
    REG("REG"),
    UPD("UPD"),
    DRE("DRE"),
    ADD("ADD"),
    OBS("OBS"),
    DET("DET"),
    REM("REM"),
    QUE("QUE"),
    QRE("QRE"),
    PUT("PUT");

    private String type;

    Identifier(String type) {
        this.type = type;
    }

    public String getIdentifier() {
        return type;
    }

    public static Identifier fromString(String text) {
        if (text != null) {
            for (Identifier identifier : Identifier.values()) {
                if (text.equalsIgnoreCase(identifier.getIdentifier())) {
                    return identifier;
                }
            }
        }
        return STA;
    }
}
