package service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Georg Faustmann on 14.06.16.
 * This class is used for conversion between java date objects and the XCal format.
 */
public class TimeDurationConverter {

    private static final Logger logger = LoggerFactory.getLogger(TimeDurationConverter.class);

    /**
     * This method returns a Date object. This object is according to the values of the given XCal Date String assigned.
     * The method expect that the XCal Date String is in UTC timezone.
     *
     * @param icalDate ical Date String
     * @return date object
     */
    public static Date ical2Date(String icalDate) throws ParseException {
        icalDate = icalDate.replaceAll("\\.\\d*Z", "");
        logger.info("new string " + icalDate);
        // if fractional seconds needed SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date d = null;
        d = sdf.parse(icalDate);
        logger.info("parsed date: " + d); // output in your system timezone using toString()
        return d;
    }

    /**
     * This method transforms a given data object into an XMLGregorianCalendar.
     * The returned time is in UTC timezone
     *
     * @param date given data object
     * @return date in XCal Format in UTC
     */
    public static XMLGregorianCalendar date2Ical(Date date) {
        // TODO SimpleDateFormat not thread safe
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateStr = sdf.format(date);
        XMLGregorianCalendar xCalendar = null;
        try {
            xCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateStr);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }

        return xCalendar;
    }
}
