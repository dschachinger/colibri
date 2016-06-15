package del;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.SimpleIQ;
import org.jivesoftware.smack.util.XmlStringBuilder;

public class CustomIQ extends IQ {
    String token;

    public CustomIQ(String token) {
        super("");

    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
 /*       xml.attribute("ei:schemaVersion", "2.0b");
        xml.attribute("xsi:schemaLocation", "http://openadr.org/oadr-2.0a/2012/07 oadr_20b.xsd");
        xml.attribute("xmlns:ei", "http://docs.oasis-open.org/ns/energyinterop/201110");
        xml.attribute("xmlns:pyld", "http://docs.oasis-open.org/ns/energyinterop/201110/payloads");
        xml.attribute("xmlns:oadr", "http://openadr.org/oadr-2.0b/2012/07");
        xml.attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        xml.rightAngleBracket();
        xml.element("pyld:requestID", "String");
 */       xml.setEmptyElement();
/*      xml.rightAngleBracket();
        xml.element("token", token);
        xml.element("devicetpye", "android");
*/
        return xml;
    }
}