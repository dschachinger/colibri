package Utils;

/**
 * Created by georg on 11.07.16.
 */
public enum EventType {
    PRICE("Price"),
    LOAD("Load")
    ;

    private final String text;
    public enum Mode {ABSOLUTE, RELATIVE, MULTIPLIER};
    private Mode mode;

    /**
     * @param text
     */
    private EventType(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }

    public String getText() {
        return text;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public static EventType stringToEnum(String value){
        for(EventType type : EventType.values()){
            if(type.toString().equals(value)){
                return type;
            }
        }
        return null;
    }
}
