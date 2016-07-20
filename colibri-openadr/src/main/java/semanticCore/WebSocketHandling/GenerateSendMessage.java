package semanticCore.WebSocketHandling;

import Utils.EventType;
import openADR.Utils.XMPPConInfo;
import semanticCore.MsgObj.ColibriMessage;
import semanticCore.MsgObj.ContentMsgObj.*;
import semanticCore.MsgObj.ContentType;
import semanticCore.MsgObj.Header;
import semanticCore.MsgObj.MsgType;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

/**
 * Created by georg on 29.06.16.
 * This class is used to produce colibri send messages
 */
public class GenerateSendMessage {

    Marshaller jaxbMarshaller;
    static int msgID = 0;
    static Header header;
    static {
        header = new Header();
        header.setReferenceId("ref1");
        header.setExpires(new Date());
        header.setDate(new Date());
        header.setContentType(ContentType.TEXT_PLAIN);
        header.setMessageId("msgID1");
    }

    public GenerateSendMessage(Marshaller jaxbMarshaller){
        this.jaxbMarshaller = jaxbMarshaller;
    }

    /**
     * This method returns a register colibri message.
     *
     * This message is used to give the receiver details about the sender. This
     * information can be used by the receiver to create an instance of the sender
     * in its data store or manage access permissions to the data store. An STA
     * message returns the status of the message reception. Other message types
     * can only be sent after a successful registration.
     * @return register colibri message
     */
    public ColibriMessage gen_REGISTER(){
        System.out.println(">>>>>>>send "+MsgType.REGISTER + " message");

        // create header
        Header header = new Header();
        header.setContentType(ContentType.APPLICATION_RDF_XML);
        header.setDate(new Date());
        header.setMessageId(getUniqueMsgID());

        // create content
        RegisterMsg regMsg = new RegisterMsg();
        Description description = new Description();
        Type type = new Type();
        type.setResource(WebSocketConInfo.getRegTypeResource());
        description.getType().add(type);
        Address connectorAddress = new Address();
        connectorAddress.setAddress(WebSocketConInfo.getRegConnectorAddress());
        connectorAddress.setDatatype("&xsd;string");
        description.setConnectorAddress(connectorAddress);
        HasProperty hasTechnologyProtocol = new HasProperty();
        hasTechnologyProtocol.setResource(WebSocketConInfo.getRegTechnologyProtocolResourceName());
        description.setHasTechnologyProtocol(hasTechnologyProtocol);

        description.setAbout(WebSocketConInfo.getRegRegisteredDescriptionAbout());
        regMsg.setDescription(description);

        String content = transformPOJOToXML(regMsg);

        ColibriMessage msg = new ColibriMessage(MsgType.REGISTER, header, content, regMsg);
        return msg;
    }

    /**
     * This method returns a deregister colibri message.
     *
     * A previously registered connector can be deregistered with this message.
     * The handling of this message is not specified within this document. Both
     * sender and receiver have to run necessary measures when canceling a
     * registration. STA message responses the result of this deregistration.
     * @return
     */
    public ColibriMessage gen_DEREGISTER(){
        System.out.println(">>>>>>>send "+MsgType.DEREGISTER + " message");

        // create header
        Header header = new Header();
        header.setContentType(ContentType.TEXT_PLAIN);
        header.setDate(new Date());
        header.setMessageId(getUniqueMsgID());

        String content = WebSocketConInfo.getRegRegisteredDescriptionAbout();

        ColibriMessage msg = new ColibriMessage(MsgType.DEREGISTER, header, content);
        return msg;
    }

    /**
     * This method returns a add service colibri message.
     *
     * This message is used to inform the receiver about new services that are
     * available at the sender of the message. This information can be used by the
     * receiver to create instances in its local storage. STA message confirms
     * correct reception or any error that occurred. If a service is initially defined
     * in the Colibri semantic core, an ADD message will be sent to the
     * corresponding technology connector. On the other hand, if the service
     * engineering is done at the technology connector side, the ADD message
     * with the service details is pushed from the technology connector to the
     * Colibri semantic core.
     *
     * This message is specific for an openADR event type. The message adds two services.
     * One service is used to inform the colibri core about new event information and the
     * other service is used to inform the connector if it is possible to comply to an openADR event.
     * @return
     */
    public ColibriMessage gen_ADD_SERVICE(EventType eventType, String serviceBaseURL){
        System.out.println(">>>>>>>send "+MsgType.ADD_SERVICE + " message");

        String type = eventType.toString();

        String serviceID = serviceBaseURL+"/"+type+"/"+"Service";

        // create header
        Header header = new Header();
        header.setContentType(ContentType.APPLICATION_RDF_XML);
        header.setDate(new Date());
        header.setMessageId(getUniqueMsgID());

        // create content
        // normal service
        Description description;
        AddMsg addMsg = new AddMsg();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setAbout(serviceID);
        serviceDescription.getType().add(new Type().withRessource("&colibri;GridData"));
        serviceDescription.getType().add(new Type().withRessource("&colibri;DataService"));
        Address serviceAdress = new Address();
        serviceAdress.setDatatype("&xsd;string");
        serviceAdress.setAddress(XMPPConInfo.getVTNFullAdrName());
        serviceDescription.setServiceAddress(serviceAdress);
        Address identifier = new Address();
        identifier.setDatatype("&xsd;string");
        identifier.setAddress("openADR_set"+type);
        serviceDescription.setIdentifier(identifier);
        serviceDescription.setHasDataConfiguration(new HasProperty().withRessource(serviceBaseURL+"/"+type+"/"+"ServiceConfiguration"));
        serviceDescription.setHasTechnologyConnector(new HasProperty().withRessource(serviceBaseURL));
        addMsg.getServiceDescriptions().add(serviceDescription);

        description = new Description();
        description.setAbout(serviceBaseURL+"/"+type+"/"+"ServiceConfiguration");
        description.getType().add(new Type().withRessource("&colibri;DataConfiguration"));
        description.getHasParameter().add(new HasProperty().withRessource(serviceBaseURL+"/"+type+"/"+"ServiceParameter1-1"));
        description.getHasParameter().add(new HasProperty().withRessource(serviceBaseURL+"/"+type+"/"+"ServiceParameter1-2"));
        description.setHasDataConfiguration(new HasProperty().withRessource(serviceBaseURL+"/"+type+"/"+"ServiceConfiguration2"));
        addMsg.getDescriptions().add(description);

        description = new Description();
        description.setAbout(serviceBaseURL+"/"+type+"/"+"ServiceParameter1-1");
        description.getType().add(new Type().withRessource("&colibri;InformationParameter"));
        addMsg.getDescriptions().add(description);

        description = new Description();
        description.setAbout(serviceBaseURL+"/"+type+"/"+"ServiceParameter1-2");
        description.getType().add(new Type().withRessource("&colibri;TimeParameter"));
        addMsg.getDescriptions().add(description);

        description = new Description();
        description.setAbout(serviceBaseURL+"/"+type+"/"+"ServiceConfiguration2");
        description.getType().add(new Type().withRessource("&colibri;DataConfiguration"));
        description.getHasParameter().add(new HasProperty().withRessource(serviceBaseURL+"/"+type+"/"+"ServiceParameter2-1"));
        description.getHasParameter().add(new HasProperty().withRessource(serviceBaseURL+"/"+type+"/"+"ServiceParameter2-2"));
        addMsg.getDescriptions().add(description);

        if(eventType.equals(EventType.PRICE)){
            description = new Description();
            description.setAbout(serviceBaseURL+"/"+type+"/"+"ServiceParameter2-1");
            description.getType().add(new Type().withRessource("&colibri;MoneyParameter"));
            description.setHasCurrency(new HasProperty().withRessource("http://www.colibri.org/Euro"));
            description.setHasUnit(new HasProperty().withRessource("http://www.colibri.org/KiloWattHour"));
            addMsg.getDescriptions().add(description);
        } else {
            description = new Description();
            description.setAbout(serviceBaseURL +"/"+type+"/"+"ServiceParameter2-1");
            description.getType().add(new Type().withRessource("&colibri;EnergyParameter"));
            description.setHasUnit(new HasProperty().withRessource("http://www.colibri.org/KiloWattHour"));
            addMsg.getDescriptions().add(description);
        }


        description = new Description();
        description.setAbout(serviceBaseURL +"/"+type+"/"+"ServiceParameter2-2");
        description.getType().add(new Type().withRessource("&colibri;IntervalParameter"));
        description.getType().add(new Type().withRessource("&colibri;TimeParameter"));
        addMsg.getDescriptions().add(description);


        // follow service
        serviceDescription = new ServiceDescription();
        serviceDescription.setAbout(serviceBaseURL+"/accept"+type+"/"+"Service");
        serviceDescription.getType().add(new Type().withRessource("&colibri;BuildingData"));
        serviceDescription.getType().add(new Type().withRessource("&colibri;DataService"));
        serviceAdress = new Address();
        serviceAdress.setDatatype("&xsd;string");
        serviceAdress.setAddress(XMPPConInfo.getVTNFullAdrName());
        serviceDescription.setServiceAddress(serviceAdress);
        identifier = new Address();
        identifier.setDatatype("&xsd;string");
        identifier.setAddress("openADR_accept"+type);
        serviceDescription.setIdentifier(identifier);
        serviceDescription.setHasDataConfiguration(new HasProperty().withRessource(serviceBaseURL+"/accept"+type+"/"+"ServiceConfiguration"));
        serviceDescription.setHasTechnologyConnector(new HasProperty().withRessource(serviceBaseURL));
        serviceDescription.setIsPrecededBy(new HasProperty().withRessource(serviceID));
        addMsg.getServiceDescriptions().add(serviceDescription);

        description = new Description();
        description.setAbout(serviceBaseURL+"/accept"+type+"/"+"ServiceConfiguration");
        description.getType().add(new Type().withRessource("&colibri;DataConfiguration"));
        description.getHasParameter().add(new HasProperty().withRessource(serviceBaseURL+"/accept"+type+"/"+"ServiceParameter1-1"));
        description.getHasParameter().add(new HasProperty().withRessource(serviceBaseURL+"/accept"+type+"/"+"ServiceParameter1-2"));
        addMsg.getDescriptions().add(description);

        description = new Description();
        description.setAbout(serviceBaseURL+"/accept"+type+"/"+"ServiceParameter1-1");
        description.getType().add(new Type().withRessource("&colibri;InformationParameter"));
        addMsg.getDescriptions().add(description);

        description = new Description();
        description.setAbout(serviceBaseURL+"/accept"+type+"/"+"ServiceParameter1-2");
        description.getType().add(new Type().withRessource("&colibri;StateParameter"));
        description.getHasState().add(new HasProperty().withRessource(serviceBaseURL+"/OptIn"));
        description.getHasState().add(new HasProperty().withRessource(serviceBaseURL+"/OptOut"));
        addMsg.getDescriptions().add(description);

        description = new Description();
        description.setAbout(serviceBaseURL+"/accept"+type+"/"+"OptOut");
        description.getType().add(new Type().withRessource("&colibri;AbsoluteState"));
        description.getType().add(new Type().withRessource("&colibri;DiscreteState"));
        description.setName(new Value().withValue("optOut").withDatatype("&xsd;string"));
        description.setValue(new Value().withValue("false").withDatatype("&xsd;boolean"));
        addMsg.getDescriptions().add(description);

        description = new Description();
        description.setAbout(serviceBaseURL+"/accept"+type+"/"+"OptIn");
        description.getType().add(new Type().withRessource("&colibri;AbsoluteState"));
        description.getType().add(new Type().withRessource("&colibri;DiscreteState"));
        description.setName(new Value().withValue("optIn").withDatatype("&xsd;string"));
        description.setValue(new Value().withValue("true").withDatatype("&xsd;boolean"));
        addMsg.getDescriptions().add(description);

        ColibriMessage msg = new ColibriMessage(MsgType.ADD_SERVICE, header, transformPOJOToXML(addMsg), addMsg);
        return msg;
    }

    /**
     * This method returns a remove service colibri message.
     *
     * A previously registered service can be unregistered with this message. For
     * example, the receiver can remove the service from its storage if a service
     * with the given URI is found. Only available services of the particular sender
     * are accepted. STA indicates if any error occurred or reception was
     * successful.
     * @return
     */
    private ColibriMessage gen_REMOVE_SERVICE(){
        System.out.println(">>>>>>>send "+MsgType.REMOVE_SERVICE + " message");

        // create header
        Header header = new Header();
        header.setContentType(ContentType.TEXT_PLAIN);
        header.setDate(new Date());
        header.setMessageId(getUniqueMsgID());

        String content = WebSocketConInfo.getRegRegisteredDescriptionAbout();

        ColibriMessage msg = new ColibriMessage(MsgType.REMOVE_SERVICE, header, content);

        return msg;
    }

    /**
     * This method returns a observe service colibri message.
     *
     * A previously registered service can be marked for value changes. Then,
     * changes are observed by the receiver of the OBS message. If any change is
     * observed, the sender of the OBS message is informed by receiving an
     * ordinary PUT message. If the service is successfully marked for observation,
     * an STA message with a positive status code is sent. Otherwise an error
     * status code is returned.
     * @return
     */
    public ColibriMessage gen_OBSERVE_SERVICE(String serviceURL){
        System.out.println(">>>>>>>send "+MsgType.OBSERVE_SERVICE + " message");

        // create header
        Header header = new Header();
        header.setContentType(ContentType.TEXT_PLAIN);
        header.setDate(new Date());
        header.setMessageId(getUniqueMsgID());

        ColibriMessage msg = new ColibriMessage(MsgType.OBSERVE_SERVICE, header, serviceURL);



        return msg;
    }

    /**
     * This method returns a detach observe service colibri message.
     *
     * This message undoes an observation of the given service. STA message
     * returns the status of the detaching process.
     * @return
     */
    public ColibriMessage gen_DETACH_OBSERVATION(String serviceURL){
        System.out.println(">>>>>>>send "+MsgType.DETACH_OBSERVATION + " message");
        // create header
        Header header = new Header();
        header.setContentType(ContentType.TEXT_PLAIN);
        header.setDate(new Date());
        header.setMessageId(getUniqueMsgID());

        ColibriMessage msg = new ColibriMessage(MsgType.DETACH_OBSERVATION, header, serviceURL);
        return msg;
    }

    /**
     * This method returns a get colibri message.
     *
     * A GET message forces the receiver of the message to look for the latest or
     * currently useful value of the given data or control service. For example, the
     * current temperature value is returned, or the currently active set point
     * temperature is sent as response. Moreover, historic data values can be
     * requested by adding the optional query parameters to and from to the
     * message content. The response is a PUT message with the requested data
     * value(s). STA message can be sent to indicate the status code.
     * @return
     */
    private ColibriMessage gen_GET_DATA_VALUES(){
        System.out.println(">>>>>>>send "+MsgType.GET_DATA_VALUES + " message");
        ColibriMessage msg = new ColibriMessage(MsgType.GET_DATA_VALUES, header, "");
        return msg;
    }

    /**
     * This method returns a query colibri message.
     *
     * This message is used to send a complete SPARQL query (SELECT statement)
     * to the Colibri semantic core. The results are sent back in a QRE message.
     * STA messages can be sent to indicate the status code.
     * @return
     */
    private ColibriMessage gen_QUERY(){
        System.out.println(">>>>>>>send "+MsgType.QUERY + " message");
        ColibriMessage msg = new ColibriMessage(MsgType.QUERY, header, "");
        return msg;
    }

    /**
     * This method returns a update colibri message.
     *
     * This message is used to send a complete SPARQL update (INSERT or
     * DELETE statement) to the Colibri semantic core. An STA messages is sent in
     * response in order to indicate the status code.
     * @return
     */
    private ColibriMessage gen_UPDATE(){
        System.out.println(">>>>>>>send "+MsgType.UPDATE + " message");
        ColibriMessage msg = new ColibriMessage(MsgType.UPDATE, header, "");
        return msg;
    }

    /**
     * This method returns a status colibri message.
     *
     * This message sends a status code according to a previously received
     * message.
     * @return
     */
    public ColibriMessage gen_STATUS(String statusCode, String referenceId){
        System.out.println(">>>>>>>send "+MsgType.STATUS + " message");


        // create header
        Header header = new Header();
        header.setContentType(ContentType.TEXT_PLAIN);
        header.setDate(new Date());
        header.setMessageId(getUniqueMsgID());
        header.setReferenceId(referenceId);

        String content = WebSocketConInfo.getRegRegisteredDescriptionAbout();

        ColibriMessage msg = new ColibriMessage(MsgType.STATUS, header, statusCode);

        return msg;
    }

    /**
     * This message transforms a given object into an xml string.
     * @param obj given object
     * @return xml string
     */
    public String transformPOJOToXML(Object obj){
        Writer writer = new StringWriter();

        try {
            jaxbMarshaller.marshal(obj, writer);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return writer.toString();
    }

    /**
     * This method returns every time it is called a unique message id
     * @return
     */
    public String getUniqueMsgID(){
        msgID++;
        return "message_"+msgID;
    }

}
