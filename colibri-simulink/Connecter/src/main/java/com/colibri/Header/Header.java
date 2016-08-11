package com.colibri.Header;

/**
 * Created by codelife on 12/8/16.
 */
import java.util.Date;
import java.util.UUID;

public class Header {

    private String id;
    private ContentType contentType;
    private Date date;
    private String refenceId;
    private Date expires;
    private String headerAsString;
    private static final String newLine = "<br>";

    protected Header(String headerAsString) {
        this.headerAsString = headerAsString;
    }

    protected Header(ContentType contentType) {
        this.id = UUID.randomUUID().toString();
        this.id = id.replace("-", "");
        this.contentType = contentType;
        this.date = new Date();
    }

    protected Header(ContentType contentType, String refenceId) {
        this.id = UUID.randomUUID().toString();
        this.id = id.replace("-", "");
        this.contentType = contentType;
        this.date = new Date();
        this.refenceId = refenceId;
    }

    public Header(String id, ContentType contentType, Date date, Date expires) {
        this.id = id;
        this.contentType = contentType;
        this.date = date;
        this.expires = expires;
    }

    public Header(String id, ContentType contentType, Date date, String refenceId, Date expires) {
        this.id = id;
        this.contentType = contentType;
        this.date = date;
        this.refenceId = refenceId;
        this.expires = expires;
    }
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
            headerAsString += "Date: " + TimeConverter.date2Ical(this.getDate()) + newLine;
        }
        headerAsString += "Content-Type: " + this.getContentType().getType() + newLine
                + "Message-Id: " + this.getId() + newLine;
        if (this.hasExpires()) {
            headerAsString += "Expires: " + TimeConverter.date2Ical(this.getExpires()) + newLine;
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