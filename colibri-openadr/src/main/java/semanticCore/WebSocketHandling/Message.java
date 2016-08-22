package semanticCore.WebSocketHandling;

import java.util.Date;

/**
 * Created by georg on 29.06.16.
 */

public class Message {

    private String message;

    public Message() {
        this("");
    }

    public Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setAuthor(String author) {

    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTime(long time) {

    }

}
