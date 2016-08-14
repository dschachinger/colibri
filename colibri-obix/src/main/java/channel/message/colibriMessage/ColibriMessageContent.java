package channel.message.colibriMessage;

import service.Configurator;

/**
 * This class represents the content of a {@link ColibriMessage}.
 */
public class ColibriMessageContent {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    private String content;
    private final String newLine = Configurator.getInstance().getNewlineString();

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    public ColibriMessageContent(String content) {
        this.content = content;
    }

    /******************************************************************
     *                            Methods                             *
     ******************************************************************/

    /**
     * This method returns the message content without whitespaces and breaks
     * (<br>, used in messages to and from the Atmosphere Chat server in place of \n) as a String.
     *
     * @return  The content without whitespaces and breaks (<br>).
     */
    public String getContentWithoutBreaksAndWhiteSpace() {
        content = content.replaceAll(newLine, "");
        content = content.replaceAll(" ", "");
        return content;
    }

    /**
     * This method returns the message content and breaks
     * (<br>, used in messages to and from the Atmosphere Chat server in place of \n) as a String.
     *
     * @return  The content without breaks (<br>).
     */
    public String getContentWithoutBreaks() {
        content = content.replaceAll(newLine, "");
        while(content.contains(" <")) {
            content = content.replaceAll(" <", "<");
        }
        return content;
    }

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    /**
     * This method returns the message content as a String.
     * (<br>, used in messages to and from the Atmosphere Chat server in place of \n).
     *
     * @return  The content without whitespaces and breaks (<br>).
     */
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return getContent();
    }
}

