package Utils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by georg on 14.06.16.
 * This class is used for conversion between java date objects and the XCal format.
 */
public class TimeDurationConverter {

    static private DatatypeFactory datatypeFactory;
    static{
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
    }


    /**
     * This method returns a Date object. This object is according to the values of the given XCal Date String assigned.
     * The method expect that the XCal Date String is in UTC timezone.
     * @param icalDate ical Date String
     * @return date object
     */
    public static Date ical2Date(String icalDate){
        icalDate = icalDate.replaceAll("\\.\\d*Z", "");
        System.out.println("new string " + icalDate);
        // if fractional seconds needed SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date d = null;
        try {
            d = sdf.parse(icalDate);
            System.out.println("parsed date: " + d); // output in your system timezone using toString()
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return d;
    }


    /**
     * This method transforms a given data object into an XMLGregorianCalendar.
     * The returned time is in UTC timezone
     * @param date given data object
     * @return date in XCal Format in UTC
     */
    public static XMLGregorianCalendar date2Ical(Date date){
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

    /**
     * Takes a long of seconds and converts it into an XCal string
     *
     * @param seconds - The number of seconds required by the String. Only positive allowed
     * @return the String properly formatted for XCal with 0 values omitted
     */
    public static String createXCalString(long seconds){
        int years = (int) (seconds / (525949 *60));
        seconds -= years * 545949 *60;
        int months = (int) (seconds / (43829 *60));
        seconds -= months * 43829 *60;
        int days = (int) (seconds / (1440 *60));
        seconds -= days * 1440 *60;
        int hours = (int) (seconds / (60 *60));
        seconds -= hours * 60 *60;
        int minutes = (int) (seconds / 60);
        seconds -= minutes *60;
        String returnString = "P";
        if(years > 0){
            returnString += (years + "Y");
        }
        if(months > 0){
            returnString += (months + "M");
        }
        if(days > 0){
            returnString += (days + "D");
        }
        returnString += "T";
        if(hours > 0){
            returnString += (hours + "H");
        }
        if(minutes > 0){
            returnString += (minutes + "M");
        }
        if(seconds > 0){
            returnString += (seconds + "S");
        }

        return returnString;
    }

    /**
     * Converts an xCal string to an integer in seconds
     *
     * @param xCal the xCal string to be parsed
     * @return the total number of seconds contained in the xCal string
     */
    public static long xCal2Seconds(String xCal){
        Pattern p = Pattern.compile("(-?)?P(?:(\\d+)Y)?(?:(\\d+)M)?(?:(\\d+)D)?T?(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?");
        Matcher m = p.matcher(xCal);
        int returnSeconds = 0;
        m.find();
        if(m.group(2) != null){
            returnSeconds += Integer.parseInt(m.group(2)) * 525949 *60;
        }
        if(m.group(3) != null){
            returnSeconds += Integer.parseInt(m.group(3)) * 43829 * 60;
        }
        if(m.group(4) != null){
            returnSeconds += Integer.parseInt(m.group(4)) * 1440 *60;
        }
        if(m.group(5) != null){
            returnSeconds += Integer.parseInt(m.group(5)) * 60 *60;
        }
        if(m.group(6) != null){
            returnSeconds += Integer.parseInt(m.group(6)) *60;
        }
        if(m.group(7) != null){
            returnSeconds += Integer.parseInt(m.group(7));
        }

        if(m.group(1) != null && m.group(1).equals("-")){
            returnSeconds = returnSeconds * (-1);
        }

        return returnSeconds;
    }

    /**
     * Get a diff between two dates
     * @param date1 the oldest date
     * @param date2 the newest date
     * @param timeUnit the unit in which you want the diff
     * @return the diff value, in the provided unit
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    public static Date addDurationToDate(Date date, long durSec){
        System.out.println("old Time: " + date.toString());

        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, (int) durSec);

        System.out.println("new Time: " + calendar.getTime().toString());

        return calendar.getTime();
    }

}
