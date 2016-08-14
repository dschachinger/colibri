package channel.message;

import java.util.Date;

/**
 * This class represents messages which are sent to the atmosphere web socket chat-server.
 */
public class AtmosphereMessage {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    private String message;
    private String author;
    private long time;

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    public AtmosphereMessage() {
        this("", "");
    }

    public AtmosphereMessage(String author, String message) {
        this.author = author;
        this.message = message;
        this.time = new Date().getTime();
    }

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
