package del;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.util.XmlStringBuilder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Time extends IQ {
    private Date utc;
    private TimeZone timeZone;
    private String display;

    public Time() {
        super("versuch", "jabber:iq:tiny");
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        System.out.println("du war da!!");
        xml.setEmptyElement();
        return xml;
    }

    public void setUtc(Date utcString) {
        utc = utcString;
/*
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            Date result =  df.parse(utcString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
*/
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public void setTz(TimeZone zone) {
        timeZone = zone;
    }

    public void setDisplay(String timeDisplay) {
        display = timeDisplay;
    }

    public Date getUtc() {
        return utc;
    }

    public TimeZone getTz() {
        return timeZone;
    }

    public String getDisplay() {
        return display;
    }
}