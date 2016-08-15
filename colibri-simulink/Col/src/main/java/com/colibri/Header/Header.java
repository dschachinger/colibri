/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.colibri.message.Header;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author codelife
 */
public class Header {

    private String id;
    private Date date;
    
    public static String getId() {
        String id = UUID.randomUUID().toString();
        id = id.replace("-", "");
        return id;
    }

    public static String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
        return dateFormat.format(date);
    }

}

