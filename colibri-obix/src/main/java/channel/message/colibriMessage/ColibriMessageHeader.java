package channel.message.colibriMessage;

import channel.message.messageObj.ContentType;
import service.Configurator;
import service.TimeDurationConverter;

import java.util.Date;
import java.util.UUID;

/**
 * This class represents the header of a {@link ColibriMessage}.
 */
public class ColibriMessageHeader {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    /**
     * The message-id.
     */
    private String id;

    /**
     * The {@link ContentType} of the message.
     */
    private ContentType contentType;

    /**
     * The date when the message was sent.
     */
    private Date date;

    /**
     * The message-id to which this message header is referencing.
     */
    private String refenceId;

    /**
     * The date when the message expires.
     */
    private Date expires;

    /**
     * The message-header as a String.
     */
    private String headerAsString;
    private static final String newLine = Configurator.getInstance().getNewlineString();

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    public ColibriMessageHeader(String headerAsString) {
        this.headerAsString = headerAsString;
    }

    public ColibriMessageHeader(ContentType contentType) {
        this.id = UUID.randomUUID().toString();
        this.id = id.replace("-", "");
        this.contentType = contentType;
        this.date = new Date();
    }

    public ColibriMessageHeader(ContentType contentType, String refenceId) {
        this.id = UUID.randomUUID().toString();
        this.id = id.replace("-", "");
        this.contentType = contentType;
        this.date = new Date();
        this.refenceId = refenceId;
    }

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMessageHeaderAsString() {
        headerAsString = "";
        if (this.hasDate()) {
            headerAsString += "Date: " + TimeDurationConverter.date2Ical(this.getDate()) + newLine;
        }
        headerAsString += "Content-Type: " + this.getContentType().getType() + newLine
                + "Message-Id: " + this.getId() + newLine;
        if (this.hasExpires()) {
            headerAsString += "Expires: " + TimeDurationConverter.date2Ical(this.getExpires()) + newLine;
        }
        if (this.hasReferenceId()) {
            headerAsString += "Reference-Id: " + this.getRefenceId();
        }
        return headerAsString;
    }

    public String getRefenceId() {
        return refenceId;
    }

    public void setRefenceId(String refenceId) {
        this.refenceId = refenceId;
    }

    public Boolean hasReferenceId() {
        return refenceId != null;
    }

    public Boolean hasDate() {
        return date != null;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public Boolean hasExpires() {
        return expires != null;
    }
}
