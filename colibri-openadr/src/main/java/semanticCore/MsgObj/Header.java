package semanticCore.MsgObj;

import Utils.TimeDurationConverter;

import java.util.Date;

/**
 * Created by georg on 28.06.16.
 */
public class Header {
    /* Message-Id (mandatory) is used to identify a message. This identifier is unique within the scope
        of the message sender. Any alphanumeric character (i.e. letters and digits) can be used. */
    private String messageId;
    /* Content-Type (mandatory) represents the MIME type of the message content. Depending on the
        message type */
    private ContentType contentType;
    /* Date (optional) shows the date and time that the message was originated using ISO 8601. The
        used format is “<date>T<time>Z” with date format “YYYY-MM-DD” and time format “hh:mm:ss”.
        The value is given in UTC. */
    private Date date;
    /* Expires (optional) gives the date and time after which the message is run off. The format is the
        same as for the date header field. */
    private Date expires;
    /* Reference-Id (optional/mandatory) is used to specify the identifier of a preceding message the
        current message is referred to. For example, the reference identifier within an STA message
        refers to the message that led to this status message. This header field is usually optional.
        However, if a message is sent in response to another message (e.g. PUT after a GET call, QRE in
        response to a QUE message), this header field is mandatory. */
    private String referenceId;

    public Header(){

    }

    public Header(Header other){
        messageId = other.messageId;
        contentType = other.contentType;
        if(other.date != null) {
            date = new Date(other.date.getTime());
        }
        if(other.expires != null){
            expires = new Date(other.expires.getTime());
        }
        referenceId = other.referenceId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getContentType() {
        return contentType.toString();
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    /**
     * This method returns the String representation which is needed for the colibri message.
     * @return header for colibri message
     */
    public String toHeaderMsgString(){
        String out = "";

        if(messageId != null){
            out += "Message-Id: " + messageId + "\n";
        }

        if(contentType != null){
            out += "Content-Type: " + contentType + "\n";
        }

        if(date != null){
            out += "Date: " + TimeDurationConverter.date2Ical(date).toXMLFormat() + "\n";
        }

        if(expires != null){
            out += "Expires: " + TimeDurationConverter.date2Ical(expires).toXMLFormat() + "\n";
        }

        if(referenceId != null){
            out += "Reference-Id: " + referenceId + "\n";
        }

        return out;
    }
}
