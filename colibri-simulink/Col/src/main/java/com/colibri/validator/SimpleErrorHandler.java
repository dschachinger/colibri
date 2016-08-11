package com.colibri.validator;

/**
 * Created by codelife on 12/8/16.
 */
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
public class SimpleErrorHandler implements ErrorHandler {
    @Override
    public void warning(SAXParseException e) throws SAXException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        System.out.println(e.getMessage());
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        System.out.println(e.getMessage());
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        System.out.println(e.getMessage());
    }
}