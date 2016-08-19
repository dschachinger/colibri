package channel.message.colibriMessage;


import channel.Connector;
import channel.message.messageObj.ContentType;
import channel.message.messageObj.MessageIdentifier;
import channel.message.messageObj.StatusCode;
import channel.message.service.ColibriMessageContentCreator;
import model.obix.ObixObject;
import service.Configurator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This Class represents messages which are sent to or received from the colibri websocket endpoint.
 * It contains helper methods to create these messages.
 */
public class ColibriMessage {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    private MessageIdentifier msgType;
    private ColibriMessageHeader header;
    private ColibriMessageContent content;
    private static final String newLine = Configurator.getInstance().getNewlineString();

    /**
     * An optional {@link ObixObject} which is set, if the {@link ColibriMessage} is produced
     * with the aid of an {@link ObixObject}. It is set in ADD, REM, OBS, DET, PUT and GET messages.
     */
    private ObixObject optionalObixObject;

    /**
     * An optional {@link Connector} which is set, if the {@link ColibriMessage} is produced with the
     * aid of an {@link Connector}. It is set in REG and DRE messages.
     */
    private Connector optionalConnector;

    /**
     * A optional list of Strings which is only used for QUE {@link ColibriMessage} and is used for checking the correctness
     * of the corresponding QRE responses.
     */
    private List<String> optionalExpectedVars;

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    public ColibriMessage(MessageIdentifier msgType, ColibriMessageHeader header, ColibriMessageContent content) {
        this.msgType = msgType;
        this.header = header;
        this.content = content;
    }

    public ColibriMessage(MessageIdentifier msgType, ColibriMessageHeader header, ColibriMessageContent content,
                          List<String> expectedVars) {
        this.msgType = msgType;
        this.header = header;
        this.content = content;
        this.optionalExpectedVars = expectedVars;
    }

    /******************************************************************
     *                Helper methods for creating messages            *
     ******************************************************************/

    /**
     * Creates a REG {@link ColibriMessage} for the given {@link Connector} sending it to the colibri semantic core.
     *
     * @param connector The {@link Connector} which should be registered.
     * @return          The register message.
     */
    public static ColibriMessage createRegisterMessage(Connector connector) {
        ColibriMessage msg = new ColibriMessage(MessageIdentifier.REG,
                new ColibriMessageHeader(ContentType.APPLICATION_RDF_XML),
                new ColibriMessageContent(ColibriMessageContentCreator.createRegisterMessageContent(connector)));
        msg.setOptionalConnector(connector);
        return msg;
    }

    /**
     * Creates a DRE {@link ColibriMessage} for the given {@link Connector} sending it to the colibri semantic core.
     *
     * @param connector The {@link Connector} which should be deregistered.
     * @return          The deregister message.
     */
    public static ColibriMessage createDeregisterMessage(Connector connector) {
        ColibriMessage msg = new ColibriMessage(MessageIdentifier.DRE,
                new ColibriMessageHeader(ContentType.TEXT_PLAIN),
                new ColibriMessageContent(connector.getConnectorAddress()));
        msg.setOptionalConnector(connector);
        return msg;
    }

    /**
     * Creates an ADD {@link ColibriMessage} for the given {@link ObixObject} for sending it to the colibri semantic core.
     *
     * @param object    The {@link ObixObject} which should be added as a service on colibri.
     * @return          The add-service message.
     */
    public static ColibriMessage createAddServiceMessage(ObixObject object) {
        ColibriMessage msg = new ColibriMessage(MessageIdentifier.ADD,
                new ColibriMessageHeader(ContentType.APPLICATION_RDF_XML),
                new ColibriMessageContent(ColibriMessageContentCreator.createAddServiceMessageContent(object)));
        msg.setOptionalObixObject(object);
        return msg;
    }

    /**
     * Creates a REM {@link ColibriMessage} for the given {@link ObixObject} for sending it to the colibri semantic core.
     *
     * @param object    The {@link ObixObject} which represents the service which should be removed from colibri.
     * @return          The remove-service message.
     */
    public static ColibriMessage createRemoveServiceMessage(ObixObject object) {
        ColibriMessage msg = new ColibriMessage(MessageIdentifier.REM,
                new ColibriMessageHeader(ContentType.TEXT_PLAIN),
                new ColibriMessageContent(object.getServiceUri()));
        msg.setOptionalObixObject(object);
        return msg;
    }

    /**
     * Creates an OBS {@link ColibriMessage} for the given {@link ObixObject} which represents a service
     * for sending it to the colibri semantic core.
     *
     * @param object    The {@link ObixObject} which represents the service which is observed.
     * @return          The observe-service message.
     */
    public static ColibriMessage createObserveServiceMessage(ObixObject object) {
        ColibriMessage msg = new ColibriMessage(MessageIdentifier.OBS,
                new ColibriMessageHeader(ContentType.TEXT_PLAIN),
                new ColibriMessageContent(object.getServiceUri()));
        msg.setOptionalObixObject(object);
        return msg;
    }

    /**
     * Creates a DET {@link ColibriMessage} for the given {@link ObixObject} which represents a service
     * for sending it to the colibri semantic core.
     *
     * @param object    The {@link ObixObject} which represents the service which is detached.
     * @return          The detach-service message.
     */
    public static ColibriMessage createDetachServiceMessage(ObixObject object) {
        ColibriMessage msg = new ColibriMessage(MessageIdentifier.DET,
                new ColibriMessageHeader(ContentType.TEXT_PLAIN),
                new ColibriMessageContent(object.getServiceUri()));
        msg.setOptionalObixObject(object);
        return msg;
    }

    /**
     * Creates a STA {@link ColibriMessage} with the given {@link StatusCode} and a description in the message content
     * for sending it to the colibri semantic core.
     *
     * @param statusCode    The {@link StatusCode} of the STA message.
     * @param description   Explanatory description.
     * @return              The status message.
     */
    public static ColibriMessage createStatusMessage(StatusCode statusCode, String description) {
        return new ColibriMessage(MessageIdentifier.STA,
                new ColibriMessageHeader(ContentType.TEXT_PLAIN),
                new ColibriMessageContent(statusCode.getCode() + " " + statusCode.toString() + newLine + description));
    }

    /**
     * Creates a STA {@link ColibriMessage} with the given {@link StatusCode}, a description in the message content and a reference ID
     * for sending it to the colibri semantic core.
     *
     * @param statusCode    The {@link StatusCode} of the STA message.
     * @param description   Explanatory description.
     * @param referenceId   The ID of the message to which this message is referencing.
     * @return              The status message.
     */
    public static ColibriMessage createStatusMessage(StatusCode statusCode, String description, String referenceId) {
        return new ColibriMessage(MessageIdentifier.STA,
                new ColibriMessageHeader(ContentType.TEXT_PLAIN, referenceId),
                new ColibriMessageContent(statusCode.getCode() + " " + statusCode.toString() + newLine + description));
    }

    /**
     * Creates a PUT {@link ColibriMessage} using a given list of{@link ObixObject} which represent the values (and their changes)
     * of obix devices and the timing of the value changes.
     *
     * @param objects   A list of{@link ObixObject} which represent the values (and their changes) of obix devices
     *                  and the timing of the value changes.
     * @return          The PUT message.
     */
    public static ColibriMessage createPutMessage(List<ObixObject> objects) {
        ColibriMessage msg = new ColibriMessage(MessageIdentifier.PUT,
                new ColibriMessageHeader(ContentType.APPLICATION_RDF_XML),
                new ColibriMessageContent(ColibriMessageContentCreator.createPutMessageContent(objects)));
        msg.setOptionalObixObject(objects.get(0));
        return msg;
    }

    /**
     * Creates a PUT {@link ColibriMessage} using a given list of{@link ObixObject} which represent the values (and their changes)
     * of obix devices and the timing of the value changes and a reference ID.
     *
     * @param objects       A list of{@link ObixObject} which represent the values (and their changes) of obix devices
     *                      and the timing of the value changes.
     * @param referenceId   The ID of the message to which this message is referencing.
     * @return              The PUT message.
     */
    public static ColibriMessage createPutMessage(List<ObixObject> objects, String referenceId) {
        ColibriMessage msg = new ColibriMessage(MessageIdentifier.PUT,
                new ColibriMessageHeader(ContentType.APPLICATION_RDF_XML, referenceId),
                new ColibriMessageContent(ColibriMessageContentCreator.createPutMessageContent(objects)));
        msg.setOptionalObixObject(objects.get(0));
        return msg;
    }

    /**
     * Creates a GET {@link ColibriMessage} for the given {@link ObixObject} which represents a service
     * for sending it to the colibri semantic core.
     *
     * @param object    The {@link ObixObject} which represents the service which wants to get value changes.
     * @return          The get message.
     */
    public static ColibriMessage createGetMessage(ObixObject object) {
        ColibriMessage msg = new ColibriMessage(MessageIdentifier.GET,
                new ColibriMessageHeader(ContentType.TEXT_PLAIN),
                new ColibriMessageContent(object.getServiceUri()));
        msg.setOptionalObixObject(object);
        return msg;
    }

    /**
     * Creates a QUE {@link ColibriMessage} with the given SPARQL query content and a list of Strings which
     * is used for checking the correctness of the corresponding QRE responses.
     *
     * @param queryContent  The SPARQL query Content
     * @param expectedVars  A list of Strings which is used for checking the correctness
     *                      of the corresponding QRE responses
     * @return              The query message containing a SPARQL query.
     */
    public static ColibriMessage createQueryMessage(String queryContent, List<String> expectedVars) {
        ColibriMessage msg = new ColibriMessage(MessageIdentifier.QUE,
                new ColibriMessageHeader(ContentType.APPLICATION_SPARQL_QUERY),
                new ColibriMessageContent(queryContent), expectedVars);
        return msg;
    }

    /**
     * Creates a UPD {@link ColibriMessage} with the given update message content.
     *
     * @param updateMessageContent  The update message content.
     * @return                      The update message.
     */
    public static ColibriMessage createUpdateMessage(String updateMessageContent) {
        ColibriMessage msg = new ColibriMessage(MessageIdentifier.UPD,
                new ColibriMessageHeader(ContentType.APPLICATION_SPARQL_QUERY),
                new ColibriMessageContent(updateMessageContent));
        return msg;
    }

    /******************************************************************
     *                            Helper                              *
     ******************************************************************/

    /**
     * Creates a new {@link ColibriMessage} using the given message, but with a new message ID.
     *
     * @param oldMessage    The message which is used to create the new {@link ColibriMessage}
     * @return              The new {@link ColibriMessage}.
     */
    public static ColibriMessage createMessageWithNewId(ColibriMessage oldMessage) {
        MessageIdentifier i = oldMessage.getMsgType();
        switch (i) {
            case REG:
                return ColibriMessage.createRegisterMessage(oldMessage.getOptionalConnector());
            case DRE:
                return ColibriMessage.createDeregisterMessage(oldMessage.getOptionalConnector());
            case ADD:
                return ColibriMessage.createAddServiceMessage(oldMessage.getOptionalObixObject());

            case REM:
                return ColibriMessage.createAddServiceMessage(oldMessage.getOptionalObixObject());

            case OBS:
                return ColibriMessage.createObserveServiceMessage(oldMessage.getOptionalObixObject());

            case DET:
                return ColibriMessage.createDetachServiceMessage(oldMessage.getOptionalObixObject());

            case STA:
                if(oldMessage.getHeader().hasReferenceId()) {
                    return new ColibriMessage(oldMessage.getMsgType(), new ColibriMessageHeader(ContentType.TEXT_PLAIN,
                            oldMessage.getHeader().getRefenceId()), oldMessage.getContent());
                } else {
                    return new ColibriMessage(oldMessage.getMsgType(), new ColibriMessageHeader(ContentType.TEXT_PLAIN), oldMessage.getContent());
                }

            case PUT:
                List<ObixObject> bundledObjects = Collections.synchronizedList(new ArrayList<>());;
                bundledObjects.add(oldMessage.getOptionalObixObject());
                return ColibriMessage.createPutMessage(bundledObjects);

            case GET:
                return ColibriMessage.createGetMessage(oldMessage.getOptionalObixObject());

            default:
                return ColibriMessage.createStatusMessage(StatusCode.ERROR_PROCESSING, "Error creating message with new ID");

        }
    }

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public MessageIdentifier getMsgType() {
        return msgType;
    }

    public ColibriMessageHeader getHeader() {
        return header;
    }

    public ColibriMessageContent getContent() {
        return content;
    }

    public void setContent(ColibriMessageContent content) {
        this.content = content;
    }

    public ObixObject getOptionalObixObject() {
        return optionalObixObject;
    }

    public void setOptionalObixObject(ObixObject optionalObixObject) {
        this.optionalObixObject = optionalObixObject;
    }

    public Connector getOptionalConnector() {
        return optionalConnector;
    }

    public void setOptionalConnector(Connector optionalConnector) {
        this.optionalConnector = optionalConnector;
    }

    public List<String> getOptionalExpectedVars() {
        if(optionalExpectedVars == null) {
            return new ArrayList<>();
        }
        return optionalExpectedVars;
    }

    public void setMessage(ColibriMessage msg) {
        this.msgType = msg.getMsgType();
        this.header = msg.getHeader();
        this.content = msg.getContent();
    }

    @Override
    public String toString() {
        return msgType.getIdentifier() + newLine + header.getMessageHeaderAsString() + newLine + content.getContent();
    }
}
