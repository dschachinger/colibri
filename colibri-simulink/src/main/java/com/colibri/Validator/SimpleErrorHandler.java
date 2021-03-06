package com.colibri.Validator;

// This class throws the error messages if the xml messages are incorrect

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author codelife
 */
public class SimpleErrorHandler implements ErrorHandler {
    @Override
    public void warning(SAXParseException e) throws SAXException {
        System.out.println(e.getMessage());
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        System.out.println(e.getMessage());
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        System.out.println(e.getMessage());
    }
}
