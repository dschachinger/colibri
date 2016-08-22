package com.colibri.Header;
// This class provides the random message id and date for the Message Header
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author codelife
 */
public class Header {

    private static String id;
    private static Date date;
    
    public static String getId() {
        String id = UUID.randomUUID().toString();
        id = id.replace("-", "");
        return id;
    }
    
    public static void setId(String msgid)
    {
        id = msgid;
    }
    
    public static String getRefId()
    {
        return id;
    }

    public static String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
        return dateFormat.format(date);
    }

}

