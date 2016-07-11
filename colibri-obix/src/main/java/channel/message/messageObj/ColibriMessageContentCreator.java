package channel.message.messageObj;

import channel.Connector;
import channel.message.colibriMessage.ColibriMessage;
import channel.obix.CoapChannel;
import channel.obix.ObixChannel;
import channel.obix.ObixXmlChannelDecorator;
import model.obix.ObixObject;
import model.obix.parameter.Parameter;
import service.Configurator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

public class ColibriMessageContentCreator {

    private static Marshaller jaxbMarshaller;
    private static Unmarshaller jaxbUnmarshaller;

    static {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(AddServiceMessageContent.class, PutMessageContent.class, RegisterMessageContent.class);
            JAXBContext jaxbUmarshallerContext = JAXBContext.newInstance(PutMessageContent.class);
            jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbUnmarshaller = jaxbUmarshallerContext.createUnmarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty("com.sun.xml.internal.bind.characterEscapeHandler", new DummyEscapeHandler());
            jaxbMarshaller.setProperty("com.sun.xml.internal.bind.xmlHeaders",
                    "\n<!DOCTYPE rdf:RDF [\n" +
                            "<!ENTITY xsd \"http://www.w3.org/2001/XMLSchema#\" >\n" +
                            "<!ENTITY colibri \"https://raw.githubusercontent.com/dschachinger/colibri/master/res/colibri.owl#\">]>");

        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }

    public static String createAddServiceMessageContent(ObixObject obixObject) {
        AddServiceMessageContent addServiceMessageContent = new AddServiceMessageContent();
        //Description of the service
        Description description = new Description();
        description.setAbout(obixObject.getServiceUri());
        description.addType("&colibri;BuildingData");
        description.addType("&colibri;DataService");
        if (obixObject.getObj().isWritable()) {
            description.addType("&colibri;ControlService");
        }
        Address address = new Address();
        address.setAddress(obixObject.getObixUri());
        description.setServiceAddress(address);
        description.setHasDataConfiguration(obixObject.getConfigurationUri());
        description.setHasTechnologyConnector(new Configurator().getConnectorAddress());
        addServiceMessageContent.addDescription(description);

        //Description of the configuration
        Description configurationDescription = new Description();
        configurationDescription.setAbout(obixObject.getConfigurationUri());
        Parameter parameter1 = obixObject.getParameter1();
        Parameter parameter2 = obixObject.getParameter2();
        configurationDescription.addHasParamater(parameter1.getParameterUri());
        configurationDescription.addHasParamater(parameter2.getParameterUri());
        addServiceMessageContent.addDescription(configurationDescription);

        //Description of parameter 1
        Description parameter1Description = new Description();
        parameter1Description.setAbout(parameter1.getParameterUri());
        parameter1Description.addType(parameter1.getParameterType());
        if (parameter1.getParameterUnit() != null) {
            parameter1Description.setHasUnit(parameter1.getParameterUnit());
        }
        if (parameter1.hasBooleanStates() != null) {
            parameter1Description.addHasStates(obixObject.getColibriBaseUri() + "true");
            parameter1Description.addHasStates(obixObject.getColibriBaseUri() + "false");
        }
        addServiceMessageContent.addDescription(parameter1Description);

        //Description of parameter 2
        Description parameter2Description = new Description();
        parameter2Description.setAbout(parameter2.getParameterUri());
        parameter2Description.addType(parameter2.getParameterType());
        if (parameter2.getParameterUnit() != null) {
            parameter2Description.setHasUnit(parameter2.getParameterUnit());
        }
        addServiceMessageContent.addDescription(parameter2Description);

        StringWriter writer = new StringWriter();

        try {
            jaxbMarshaller.marshal(addServiceMessageContent, writer);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    public static String createPutMessageContent(ObixObject obixObject) {
        PutMessageContent putMessageContent = new PutMessageContent();
        //Description of the service
        Description description = new Description();
        description.setAbout(obixObject.getServiceUri());
        description.setHasDataValue(obixObject.getDataValueUri());
        putMessageContent.addDescription(description);

        //Description of the data Values
        Description dataValueDescription = new Description();
        dataValueDescription.setAbout(obixObject.getDataValueUri());
        dataValueDescription.addType("&colibri;DataValue");
        dataValueDescription.addHasValue(obixObject.getParameter1().getValueUri());
        dataValueDescription.addHasValue(obixObject.getParameter2().getValueUri());
        putMessageContent.addDescription(dataValueDescription);

        //Description of the values
        Description value1Description = new Description();
        value1Description.setAbout(obixObject.getParameter1().getValueUri());
        value1Description.addType("&colibri;Value");
        Value value1 = new Value();
        value1.setValue(obixObject.getParameter1().getValueAsString());
        value1.setDatatype(obixObject.getParameter1().getValueType());
        value1Description.setValue(value1);
        value1Description.addHasParamater(obixObject.getParameter1().getParameterUri());
        putMessageContent.addDescription(value1Description);

        //Description of parameter 2
        Description value2Description = new Description();
        value2Description.setAbout(obixObject.getParameter2().getValueUri());
        value2Description.addType("&colibri;Value");
        Value value2 = new Value();
        value2.setValue(obixObject.getParameter2().getValueAsString());
        value2.setDatatype(obixObject.getParameter2().getValueType());
        value2Description.setValue(value2);
        value2Description.addHasParamater(obixObject.getParameter2().getParameterUri());
        putMessageContent.addDescription(value2Description);

        StringWriter writer = new StringWriter();

        try {
            jaxbMarshaller.marshal(putMessageContent, writer);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    public static String createRegisterMessageContent(Connector connector) {
        RegisterMessageContent registerMessageContent = new RegisterMessageContent();
        Description description = new Description();
        description.setAbout(connector.getConnectorAddress() + "/" + "tc1");
        description.addType("&colibri;ObixConnector");
        Address address = new Address();
        address.setAddress(connector.getIpAddress());
        description.setConnectorAddress(address);
        description.setHasTechnologyProtocol("&colibri;obix");
        registerMessageContent.setDescription(description);
        StringWriter writer = new StringWriter();

        try {
            jaxbMarshaller.marshal(registerMessageContent, writer);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    public static PutMessageContent getPutMessageContent(ColibriMessage putMsg) throws JAXBException {
        StringReader reader = new StringReader(putMsg.getContent().getContentWithoutBreaks());
        return (PutMessageContent) jaxbUnmarshaller.unmarshal(reader);
    }

    public static void main(String[] args) {
        ObixChannel channel = new ObixXmlChannelDecorator(new CoapChannel("localhost", "localhost/obix", new ArrayList<>()));
        ObixObject obj = channel.get("VirtualDevices/virtualTemperatureSensor/value");

        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("ADD SERVICE MESSAGE");

        System.out.println(ColibriMessageContentCreator.createAddServiceMessageContent(obj));

        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("PUT MESSAGE");

        System.out.println(ColibriMessageContentCreator.createPutMessageContent(obj));

        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("RECEIVED PUT MESSAGE");

        ColibriMessage msg = ColibriMessage.createPutMessage(obj);
    }
}
