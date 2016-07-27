package openADR.XMPP;

import org.jivesoftware.smack.packet.IQ;

/**
 * This class is a decendant from the smack class IQ.
 * Objects from this class represent the xmpp iq messages.
 */
public class OADR2IQ extends IQ {

    // This wrapper object holds the actual openADR payload
    private OADR2PacketExtension extension;

    public OADR2IQ(OADR2PacketExtension extension) {
        super(extension.getElementName(), extension.getNamespace());
        this.init( extension );
    }

    // you have to call init afterwards to set extension
    public OADR2IQ(String elemName, String namespace) {
        super(elemName, namespace);
        extension = null;
    }

    /**
     * This mehtod can be used to set the extension object after the initialization.
     * You are only allowed to call this method if the extension object was not set before.
     * @param extension use this extension for this iq message
     */
    public void init( OADR2PacketExtension extension ) {
        if(this.extension != null){
            throw new IllegalStateException("Call init method from OADR2IQ class only once");
        }
        this.setType(Type.set);
        this.extension = extension;
        this.addExtension(extension);
    }

    /**
     * This method returns the actual openADR payload
     * @return
     */
    public Object getOADRPayload() {
        return this.extension.getPayload();
    }

    /**
     * This method is called by smack and can be used to add additional xml elements into the iq message.
     * @param xml you add some elements to this object
     * @return changed IQChildElementXmlStringBuilder object
     */
    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        // do not add any additional elements to the message
        return xml;
    }

    public OADR2PacketExtension getExtension() {
        return extension;
    }
}