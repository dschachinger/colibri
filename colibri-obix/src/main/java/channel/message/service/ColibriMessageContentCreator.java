package channel.message.service;

import channel.Connector;
import channel.colibri.ColibriChannel;
import channel.colibri.taskServices.PutMessageToColibriTask;
import channel.message.colibriMessage.ColibriMessage;
import channel.message.colibriMessage.ColibriMessageContent;
import channel.message.colibriMessage.ColibriMessageHeader;
import channel.message.messageObj.*;
import channel.obix.CoapChannel;
import channel.obix.ObixChannel;
import channel.obix.ObixXmlChannelDecorator;
import model.obix.ObixObject;
import model.obix.parameter.Parameter;
import service.Configurator;
import service.TimeDurationConverter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;

public class ColibriMessageContentCreator {

    private static Marshaller jaxbMarshaller;
    private static Unmarshaller jaxbUnmarshaller;
    private static boolean booleanStatesAlreadyReigstered;
    private static int dataValueCounter = 1;

    static {
        try {
            booleanStatesAlreadyReigstered = false;

            JAXBContext jaxbContext = JAXBContext.newInstance(AddServiceMessageContent.class, PutMessageContent.class, RegisterMessageContent.class);
            JAXBContext jaxbUmarshallerContext = JAXBContext.newInstance(PutMessageContent.class);
            jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbUnmarshaller = jaxbUmarshallerContext.createUnmarshaller();

            // output pretty printed
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
        List<StateDescription> list = obixObject.getParameter1().getStateDescriptions();
        for (StateDescription des : obixObject.getParameter1().getStateDescriptions()) {
            Description stateDescription = new Description();
            stateDescription.setAbout(des.getStateDescriptionUri());
            des.getStateTypes().forEach(stateDescription::addType);
            stateDescription.setValue(des.getValue());
            stateDescription.setName(des.getName());
            addServiceMessageContent.addDescription(stateDescription);
        }

        for (StateDescription des : obixObject.getParameter2().getStateDescriptions()) {
            Description stateDescription = new Description();
            stateDescription.setAbout(des.getStateDescriptionUri());
            des.getStateTypes().forEach(stateDescription::addType);
            stateDescription.setValue(des.getValue());
            stateDescription.setName(des.getName());
            addServiceMessageContent.addDescription(stateDescription);
        }

        StringWriter writer = new StringWriter();

        try {
            jaxbMarshaller.marshal(addServiceMessageContent, writer);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

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

    public static PutMessageContent getPutMessageContent(ColibriMessage putMsg) throws JAXBException {
        StringReader reader = new StringReader(putMsg.getContent().getContentWithoutBreaks());
        return (PutMessageContent) jaxbUnmarshaller.unmarshal(reader);
    }

    public static int increase() {
        return dataValueCounter++;
    }

    public static void main(String[] args) {
        ObixChannel channel = new ObixXmlChannelDecorator(new CoapChannel("localhost", "localhost/obix", Collections.synchronizedList(new ArrayList<>())));
        ObixObject obj = channel.get("VirtualDevices/virtualTemperatureSensor/value");

        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("ADD SERVICE MESSAGE");

        System.out.println(ColibriMessageContentCreator.createAddServiceMessageContent(obj));

        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("PUT MESSAGE");

        List<ObixObject> putObjectList = Collections.synchronizedList(new ArrayList<>());;
        putObjectList.add(obj);
        System.out.println(ColibriMessageContentCreator.createPutMessageContent(putObjectList));

        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("RECEIVED PUT MESSAGE");

        ColibriMessage observeMsg = new ColibriMessage(MessageIdentifier.OBS, new ColibriMessageHeader(ContentType.TEXT_PLAIN),
                new ColibriMessageContent("http://test.org/res1?freq=sad"));

        ColibriChannel colibriChannel = new ColibriChannel("bla", "127.0.0.1", 6789);

        String serviceUri = "";
        if (observeMsg.getContent().getContentWithoutBreaksAndWhiteSpace().contains("?freq=")) {
            String[] content = observeMsg.getContent().getContentWithoutBreaksAndWhiteSpace().split("\\?freq=");
            Date dateNow = new Date();
            String icalTemp = TimeDurationConverter.date2Ical(dateNow).toString();
            Timer timer = new Timer();
            PutMessageToColibriTask executionTask = new PutMessageToColibriTask(obj, colibriChannel, observeMsg.getHeader().getId());
            try {
                Date d = (TimeDurationConverter.ical2Date(icalTemp.split("T")[0] + "T" + content[1]));

                /**
                 * Send Put once a day at the specified time
                 */
                timer.schedule(executionTask, d, 24 * 60 * 60 * 1000);
            } catch (ParseException e) {
                Duration duration = Duration.parse(content[1]);
                /**
                 * Send Put with the specified duration
                 */
                timer.schedule(executionTask, duration.toMillis());

            }
        } else {
            serviceUri = observeMsg.getContent().getContentWithoutBreaksAndWhiteSpace();
        }
        while (true) {

        }
    }
}
