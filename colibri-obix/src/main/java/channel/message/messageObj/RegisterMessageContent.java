package channel.message.messageObj;


import channel.Connector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringWriter;

@XmlRootElement(name = "RDF" ,namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
@XmlAccessorType(XmlAccessType.FIELD)
public class RegisterMessageContent {

    @XmlElement(name="Description", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private Description description;

    public Description getDescription() {
        return description;
        }

    public void setDescription(Description description) {
        this.description = description;
        }

    public String getRegisterMessageContent(Connector connector) {
        Description description = new Description();
        description.setAbout("http://www.colibri-samples.org/tc1");
        description.addType("&colibri;oBIXConnector");
        Address address = new Address();
        address.setAddress(connector.getConnectorAddress());
        description.setConnectorAddress(address);
        description.setHasTechnologyProtocol("&colibri;oBIX");
        this.setDescription(description);
        StringWriter writer = new StringWriter();

        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(RegisterMessageContent.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty("com.sun.xml.internal.bind.characterEscapeHandler", new DummyEscapeHandler());
            jaxbMarshaller.setProperty("com.sun.xml.internal.bind.xmlHeaders",
                    "\n<!DOCTYPE rdf:RDF [\n" +
                            "<!ENTITY xsd \"http://www.w3.org/2001/XMLSchema#\" >\n" +
                            "<!ENTITY colibri \"https://raw.githubusercontent.com/dschachinger/colibri/master/res/colibri.owl#\">]>");


            jaxbMarshaller.marshal(this, writer);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }
}
