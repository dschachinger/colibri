package semanticCore.MsgObj;

/**
 * Created by georg on 28.06.16.
 */
public enum StatusCode {
    OK("200"),
    STRUCTURE_MSG_ERROR("300"),
    SYNTACTICAL_ERROR("400"),
    SEMANTIC_ERROR("500"),
    CONNECTION_ERROR("600"),
    INTERNAL_PROCESSING_ERROR("700"),
    ACCESS_PERMISSION_ERROR("800")
    ;

    private final String text;

    /**
     * @param text
     */
    private StatusCode(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}