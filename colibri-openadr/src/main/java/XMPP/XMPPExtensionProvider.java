package XMPP;

import OADRHandling.JAXBManager;
import OADRHandling.OADR2Unmarshaller;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Element;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;;
import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * This is an implementation of the Smack {@link IQProvider} to 
 * provide automatic de-serialization of OpenADR payloads in XMPP.
 * IQs that contain an OpenADR payload will automatically be parsed
 * as {@link OADR2IQ} instances. 
 * @author tnichols
 */
public class XMPPExtensionProvider extends IQProvider{

    protected JAXBManager jaxb;
    protected OADR2Unmarshaller oadr2Unmarshaller;

    public XMPPExtensionProvider() {
        try {
            this.jaxb = new JAXBManager();
            this.oadr2Unmarshaller = new OADR2Unmarshaller(jaxb.getContext());
        }
        catch ( JAXBException ex ) {
            throw new RuntimeException("Error initializing JAXB context",ex);
        }
    }

    @Override
    public Element parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
        System.out.println( "++++++++++++++++++++++++++ Parsing IQ!!!" );
        try {
            return new OADR2IQ( (OADR2PacketExtension)parseExtension(parser) );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public XMPPExtensionProvider(JAXBManager jaxb) {
        try {
            this.jaxb = jaxb;
            this.oadr2Unmarshaller = new OADR2Unmarshaller(jaxb.getContext());
        }
        catch ( JAXBException ex ) {
            throw new RuntimeException("Error initializing JAXB context",ex);
        }
		System.out.println("++++++++++++++++++++ LOADED OADR Packet Extension Provider");
    }

    public ExtensionElement parseExtension(XmlPullParser pullParser) throws Exception {
		System.out.println( "++++++++++++++++++++++++++ Parsing Extension!!!" );
        return new OADR2PacketExtension( oadr2Unmarshaller.convertOADRMsg(pullParser), this.jaxb );
    }
}
