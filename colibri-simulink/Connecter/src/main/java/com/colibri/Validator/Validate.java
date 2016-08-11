package com.colibri.Validator;

/**
 * Created by codelife on 12/8/16.
 */
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author codelife
 */
public class Validate {

    public static String get(String xml, String value, String dates) throws SAXException, IOException, ParserConfigurationException {
        // TODO code application logic here
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new SimpleErrorHandler());
        // the "parse" method also validates XML, will throw an exception if misformatted
        Document document = builder.parse(new InputSource("reg.xml"));
        //System.out.println(document.getXmlEncoding());
        return(readFile(xml, value, dates));
    }
    public static String readFile(String path, String value, String dates) throws IOException{
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String sCurrentLine;
        while ((sCurrentLine = br.readLine()) != null) {
            if (sCurrentLine.contains(">tempvalue"))
                sb.append(sCurrentLine.replace(">tempvalue", ">"+value));
            else if (sCurrentLine.contains(">lightvalue"))
                sb.append(sCurrentLine.replace(">lightvalue", ">"+value));
            else if (sCurrentLine.contains(">datevalue"))
                sb.append(sCurrentLine.replace(">datevalue", ">"+dates));
            else
                sb.append(sCurrentLine);
            sb.append("\n");
            //System.out.println("Returned Value =  "+ value + ", Date =  "+ dates);
        }
        return sb.toString();
    }
}