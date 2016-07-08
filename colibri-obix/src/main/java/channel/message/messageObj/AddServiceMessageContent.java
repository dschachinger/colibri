package channel.message.messageObj;

import channel.obix.CoapChannel;
import channel.obix.ObixChannel;
import channel.obix.ObixXmlChannelDecorator;
import model.obix.ObixObject;
import model.obix.parameter.Parameter;
import service.Configurator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "RDF" ,namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
@XmlAccessorType(XmlAccessType.FIELD)
public class AddServiceMessageContent {

    private String connectorAddress;

    @XmlElement(name="Description", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private List<Description> descriptions;

    public AddServiceMessageContent() {
        this.descriptions = new ArrayList<>();
    }

    public List<Description> getDescriptions() {
        return descriptions;
    }

    public void addDescription(Description description) {
        this.descriptions.add(description);
    }

    public String getAddServiceMessageContent(ObixObject obixObject) {
        //Description of the service
        Description description = new Description();
        description.setAbout(obixObject.getServiceUri());
        description.addType("&colibri;BuildingData");
        description.addType("&colibri;DataService");
        Address address = new Address();
        address.setAddress(obixObject.getUri());
        description.setServiceAddress(address);
        description.setHasDataConfiguration(obixObject.getUri() + "/configuration");
        description.setHasTechnologyConnector(new Configurator().getConnectorAddress());
        this.addDescription(description);

        //Description of the configuration
        Description configurationDescription = new Description();
        configurationDescription.setAbout(obixObject.getConfigurationUri());
        Parameter parameter1 = obixObject.getParameter1();
        Parameter parameter2 = obixObject.getParameter2();
        configurationDescription.addHasParamater(parameter1.getParameterUri());
        configurationDescription.addHasParamater(parameter2.getParameterUri());

        this.addDescription(configurationDescription);
        //Description of parameter 1
        Description parameter1Description = new Description();
        parameter1Description.setAbout(parameter1.getParameterUri());
        parameter1Description.addType(parameter1.getParameterType());
        parameter1Description.setHasUnit(parameter1.getParameterUnit());
        this.addDescription(parameter1Description);

        //Description of parameter 2
        Description parameter2Description = new Description();
        parameter2Description.setAbout(parameter2.getParameterUri());
        parameter2Description.addType(parameter2.getParameterType());
    //    parameter2Description.setHasUnit(parameter2.getParameterUnit());
        this.addDescription(parameter2Description);

        StringWriter writer = new StringWriter();

        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(AddServiceMessageContent.class);
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

    public static void main(String[] args) {
        ObixChannel channel = new ObixXmlChannelDecorator(new CoapChannel("localhost", "localhost/obix", new ArrayList<>()));
        ObixObject obj = channel.get("VirtualDevices/virtualTemperatureSensor/value");
        AddServiceMessageContent addServiceMessageContent = new AddServiceMessageContent();
        System.out.println(addServiceMessageContent.getAddServiceMessageContent(obj));

    }
}
