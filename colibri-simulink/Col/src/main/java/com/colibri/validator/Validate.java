package com.colibri.validator;

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
    public static String get(String xml) throws SAXException, IOException, ParserConfigurationException {
        // TODO code application logic here
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new SimpleErrorHandler());
        // the "parse" method also validates XML, will throw an exception if misformatted
        Document document = builder.parse(new InputSource("reg.xml"));
        //System.out.println(document.getXmlEncoding());
        return readFile("reg.xml");
    }
    public static String readFile(String path) throws IOException{
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String sCurrentLine;
        while ((sCurrentLine = br.readLine()) != null) {
            sb.append(sCurrentLine);
            sb.append("\n");
        }
        return sb.toString();
    }
}