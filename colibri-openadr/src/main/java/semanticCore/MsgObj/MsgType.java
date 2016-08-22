package semanticCore.MsgObj;

/**
 * Created by georg on 28.06.16.
 */
public enum MsgType {
    REGISTER("REG"),
    DEREGISTER("DRE"),
    ADD_SERVICE("ADD"),
    REMOVE_SERVICE("REM"),
    OBSERVE_SERVICE("OBS"),
    DETACH_OBSERVATION("DET"),
    PUT_DATA_VALUES("PUT"),
    GET_DATA_VALUES("GET"),
    QUERY("QUE"),
    QUERY_RESULT("QRE"),
    UPDATE("UPD"),
    STATUS("STA")
    ;

    private final String text;

    /**
     * @param text
     */
    private MsgType(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }

    public static MsgType stringToEnum(String value){
        for(MsgType type : MsgType.values()){
            if(type.toString().equals(value)){
                return type;
            }
        }
        return null;
    }
}