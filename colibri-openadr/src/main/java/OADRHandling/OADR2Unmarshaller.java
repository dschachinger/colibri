package OADRHandling;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by georg on 03.06.16.
 * This class is used to transform XML text into Java objects.
 */
public class OADR2Unmarshaller {
    Unmarshaller unmarshaller;

    public OADR2Unmarshaller(JAXBContext context) throws JAXBException {
        unmarshaller = context.createUnmarshaller();
    }

    /**
     * This method transform the given XmlPullParser object into a Java object.
     * @param parser The XmlPullParser contains a XML text.
     * @return the transformed java object
     * @throws IOException
     * @throws XmlPullParserException
     * @throws JAXBException
     */
    public Object convertOADRMsg( XmlPullParser parser ) throws IOException, XmlPullParserException, JAXBException {
        int eventType = parser.getEventType();
        String extensionElementName = parser.getName();
        StringBuilder extensionBuilder = new StringBuilder();
        String element = "";

        while(!(eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase(extensionElementName))) {
            if(eventType == XmlPullParser.END_TAG && parser.getText().equalsIgnoreCase(element)) {
                // this is for elements which are closed immediately like the following example: <sample />
                // otherwise, this line would be added twice
            } else {
                extensionBuilder.append(parser.getText());
            }

            element = parser.getText();
            eventType = parser.next();
        }

        // append closing element
        if(!parser.getText().equalsIgnoreCase(element))
            extensionBuilder.append(parser.getText());


        StringReader reader = new StringReader(extensionBuilder.toString());

        return unmarshaller.unmarshal(reader);

    }
}
