package openADR.XMPP;

import openADR.OADRHandling.JAXBManager;
import openADR.OADRHandling.XMLNS;
import org.jivesoftware.smack.packet.ExtensionElement;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import java.io.StringWriter;

/**
 * Packet extensions are created by openADR.XMPP extension implementations
 * that understand a particular namespace that appears in the body
 * of an openADR.XMPP packet (message or IQ.)  In this case, this extension
 * will be created whenever the OpenADR 2.0 namespace 
 * (http://openadr.org/oadr-2.0[a or b]/2012/07) is encountered.  It's also
 * used to serialize (send) a packet that contains an OpenADR payload.
 * 
 * @see ExtensionElement
 * @author tnichols
 */
public class OADR2PacketExtension implements ExtensionElement {

	// JAXB object don't inherit a base class, so you'll need to cast to 
	// the correct type, which you can infer based on the root element name
	// or just do `instanceof` checks
	Object payload; 
	 
	JAXBManager jaxb;
	Marshaller marshaller;
	
	public OADR2PacketExtension(Object e) {
		this.payload = e;
	}
	
	public OADR2PacketExtension(Object e, JAXBManager jaxb) {
		this.payload = e;
		this.jaxb = jaxb;
	}
	
	public OADR2PacketExtension(Object e, Marshaller marshaller) {
		this.payload = e;
		this.marshaller = marshaller;
	}
	
	/**
	 * This will return the parsed OpenADR model object which will be 
	 * one of:
	 * {@link com.enernoc.open.oadr2.model.v20b.OadrDistributeEvent}, {@link com.enernoc.open.oadr2.model.v20b.OadrRequestEvent},
	 * {@link com.enernoc.open.oadr2.model.v20b.OadrCreatedEvent} or {@link com.enernoc.open.oadr2.model.v20b.OadrResponse}.
	 * @return the JAXB-parsed payload object.
	 */
	public Object getPayload() {
		return this.payload;
	}
	
	@Override public String getElementName() {
//		return this.payload.getName().getLocalPart();
		return XMLNS.OADR2.getPrefix()+":" + getElementNameWithoutNamespace();
	}

	public String getElementNameWithoutNamespace(){
		return this.payload.getClass().getAnnotation(XmlRootElement.class).name();
	}

	@Override public String getNamespace() {
//		return this.payload.getName().getNamespaceURI();
 		String namespace = this.payload.getClass().getAnnotation(XmlRootElement.class).namespace();
	    if ( "##default".equals( namespace ) )
	        namespace = this.payload.getClass().getPackage().getAnnotation( XmlSchema.class ).namespace();
		return namespace;
	}

	@Override public String toXML() {
		try {
			if ( this.marshaller == null ) // TODO implement later synchronize
				this.marshaller = this.jaxb.createMarshaller();
			
			StringWriter sw = new StringWriter();
			this.marshaller.marshal(this.payload, sw);
			String xml = sw.toString();

			xml=xml.replaceFirst("^<"+getElementName()+"", "");
			xml=xml.replaceFirst("</"+getElementName()+">$", "");
			xml=xml.replaceFirst("xmlns=\"http://openadr.org/oadr-2.0b/2012/07\"", "");
			// TODO implement later old version xml=xml.replaceFirst("xmlns=\"http://openadr.org/oadr-2.0b/2012/07\"", "xmlns:oadr=\"http://openadr.org/oadr-2.0b/2012/07\"");
			return xml;
			// TODO implement later clean version return sw.toString();
		}
		catch ( JAXBException ex ) {
			throw new RuntimeException("JAXB error marshalling XML to string", ex);
		}
	}	
}
