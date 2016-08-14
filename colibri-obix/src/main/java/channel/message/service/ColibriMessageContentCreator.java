package channel.message.service;

import channel.Connector;
import channel.message.colibriMessage.ColibriMessage;
import channel.message.messageObj.*;
import model.obix.ObixObject;
import model.obix.parameter.Parameter;
import service.Configurator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * This class includes methods to create the content used in {@link channel.message.colibriMessage.ColibriMessage}.
 */
public class ColibriMessageContentCreator {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    private static Marshaller jaxbMarshaller;
    private static Unmarshaller jaxbUnmarshaller;
    private static int dataValueCounter = 1;

    static {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(AddServiceMessageContent.class, PutMessageContent.class, RegisterMessageContent.class);
            JAXBContext jaxbUmarshallerContext = JAXBContext.newInstance(PutMessageContent.class);
            jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbUnmarshaller = jaxbUmarshallerContext.createUnmarshaller();

            // output in pretty format
            jaxbMarshaller.setProperty("com.sun.xml.internal.bind.characterEscapeHandler", new DummyEscapeHandler());
            jaxbMarshaller.setProperty("com.sun.xml.internal.bind.xmlHeaders",
                    "\n<!DOCTYPE rdf:RDF [\n" +
                            "<!ENTITY xsd \"http://www.w3.org/2001/XMLSchema#\" >\n" +
                            "<!ENTITY rdf \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >\n" +
                            "<!ENTITY colibri \"https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl\">]>");

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    /******************************************************************
     *                              Methdos                           *
     ******************************************************************/

    /**
     * This method is used to create the content for an ADD {@link ColibriMessage}.
     *
     * @param obixObject    The object for which the service for the ADD message is created.
     * @return              The created content for an ADD service message.
     */
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
        description.setHasTechnologyConnector(Configurator.getInstance().getConnectorAddress());
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
        parameter1.getStateUris().forEach(parameter1Description::addHasStates);
        addServiceMessageContent.addDescription(parameter1Description);

        //Description of parameter 2
        Description parameter2Description = new Description();
        parameter2Description.setAbout(parameter2.getParameterUri());
        parameter2Description.addType(parameter2.getParameterType());
        if (parameter2.getParameterUnit() != null) {
            parameter2Description.setHasUnit(parameter2.getParameterUnit());
        }
        parameter2.getStateUris().forEach(parameter2Description::addHasStates);
        addServiceMessageContent.addDescription(parameter2Description);

        //Description of possible states
        addServiceMessageContent = addStateDescriptionsToAddServiceContent(addServiceMessageContent,
                obixObject.getParameter1().getStateDescriptions());

        addServiceMessageContent = addStateDescriptionsToAddServiceContent(addServiceMessageContent,
                obixObject.getParameter2().getStateDescriptions());

        StringWriter writer = new StringWriter();

        try {
            jaxbMarshaller.marshal(addServiceMessageContent, writer);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    /**
     * This method is used to create the content for a PUT {@link ColibriMessage}.
     *
     * @param obixObjects   The list of {@link ObixObject} which is used to create the PUT message content.
     * @return              The created content for a PUT message.
     */
    public static String createPutMessageContent(List<ObixObject> obixObjects) {
        PutMessageContent putMessageContent = new PutMessageContent();
        //Description of the service
        Description description = new Description();
        description.setAbout(obixObjects.get(0).getServiceUri());
        putMessageContent.addDescription(description);

        for(ObixObject obixObject : obixObjects) {
            description.addHasDataValue(obixObjects.get(0).getDataValueUri() + dataValueCounter);
            //Description of the data Values
            Description dataValueDescription = new Description();
            dataValueDescription.setAbout(obixObject.getDataValueUri() + dataValueCounter);
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
            dataValueCounter++;
        }

        StringWriter writer = new StringWriter();

        try {
            jaxbMarshaller.marshal(putMessageContent, writer);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    /**
     * This method is used to create the content for a REG {@link ColibriMessage}.
     *
     * @param connector     The {@link Connector} which is used to create REG messages.
     * @return              The created content for a REG message.
     */
    public static String createRegisterMessageContent(Connector connector) {
        RegisterMessageContent registerMessageContent = new RegisterMessageContent();
        Description description = new Description();
        description.setAbout(connector.getConnectorAddress());
        description.addType("&colibri;ObixConnector");
        Address address = new Address();
        address.setAddress(connector.getIpAddress());
        description.setConnectorAddress(address);
        description.setHasTechnologyProtocol("&colibri;Obix");
        registerMessageContent.setDescription(description);
        StringWriter writer = new StringWriter();

        try {
            jaxbMarshaller.marshal(registerMessageContent, writer);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    /**
     * This method is used to parse a {@link PutMessageContent} from a PUT {@link ColibriMessage}.
     *
     * @param putMsg            The PUT message which is parsed.
     * @return                  The parsed PUT message content from the given PUT {@link ColibriMessage}.
     * @throws JAXBException    Thrown, if the message parsing fails.
     */
    public static PutMessageContent getPutMessageContent(ColibriMessage putMsg) throws JAXBException {
        StringReader reader = new StringReader(putMsg.getContent().getContentWithoutBreaks());
        return (PutMessageContent) jaxbUnmarshaller.unmarshal(reader);
    }

    /**
     * This method is used to add the given {@link StateDescription} to the given {@link AddServiceMessageContent}.
     *
     * @param addServiceMessageContent  The {@link AddServiceMessageContent} to which the
     *                                  {@link StateDescription} are added.
     * @param stateDescriptions         The list of {@link StateDescription} which is added to the
     *                                  {@link AddServiceMessageContent}.
     * @return  The {@link AddServiceMessageContent} with the added {@link StateDescription}.
     */
    private static AddServiceMessageContent addStateDescriptionsToAddServiceContent(
            AddServiceMessageContent addServiceMessageContent, List<StateDescription> stateDescriptions) {
        for (StateDescription des : stateDescriptions) {
            Description stateDescription = new Description();
            stateDescription.setAbout(des.getStateDescriptionUri());
            des.getStateTypes().forEach(stateDescription::addType);
            stateDescription.setValue(des.getValue());
            stateDescription.setName(des.getName());
            addServiceMessageContent.addDescription(stateDescription);
        }
        return addServiceMessageContent;
    }
}
