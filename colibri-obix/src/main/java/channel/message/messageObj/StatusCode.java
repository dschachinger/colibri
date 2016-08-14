package channel.message.messageObj;

/**
 * This enum represents the status code used in {@link channel.message.colibriMessage.ColibriMessage}.
 */
public enum StatusCode {
    OK(200), ERROR_STRUCTURE(300), ERROR_SYNTACTIC(400),
    ERROR_SEMANTIC(500), ERROR_CONNECTION(600), ERROR_PROCESSING(700),
    ERROR_PERMISSION(800);

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    private int code;

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    StatusCode(int code) {
        this.code = code;
    }

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public int getCode() {
        return code;
    }

}
