package semanticCore.WebSocketHandling;

import java.util.Date;

/**
 * Created by georg on 29.06.16.
 */

public class Message {

    private String message;
    private String author;
    private long time;

    public Message() {
        this("");
    }

    public Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}