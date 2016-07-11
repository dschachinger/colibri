package channel.message.colibriMessage;


import channel.Connector;
import channel.message.messageObj.ColibriMessageContentCreator;
import model.obix.ObixObject;

public class ColibriMessage {

    private MessageIdentifier msgType;
    private ColibriMessageHeader header;
    private ColibriMessageContent content;
    private static final String newLine = "<br>";
    private ObixObject optionalObixObject;
    private Connector optionalConnector;

    public ColibriMessage(MessageIdentifier msgType, ColibriMessageHeader header, ColibriMessageContent content) {
        this.msgType = msgType;
        this.header = header;
        this.content = content;
    }

    public void setMessage(ColibriMessage msg) {
        this.msgType = msg.getMsgType();
        this.header = msg.getHeader();
        this.content = msg.getContent();
    }

    /**
     * Creates a register message of this connector for the colibri semantic core.
     *
     * @return              The register message.
     */
    public static ColibriMessage createRegisterMessage(Connector connector) {
        ColibriMessage msg = new ColibriMessage(MessageIdentifier.REG,
                new ColibriMessageHeader(ContentType.APPLICATION_RDF_XML),
                new ColibriMessageContent(ColibriMessageContentCreator.createRegisterMessageContent(connector)));
        return msg;
    }

    /**
     * Creates a deregister message of this connector for the colibri semantic core.
     *
     * @return          The deregister message.
     */
    public static ColibriMessage createDeregisterMessage(Connector connector) {
        ColibriMessage msg =  new ColibriMessage(MessageIdentifier.DRE,
                new ColibriMessageHeader(ContentType.TEXT_PLAIN),
                new ColibriMessageContent(connector.getConnectorAddress()));
        msg.setOptionalConnector(connector);
        return msg;
    }

    /**
     * Creates a addService message of this connector for the given obix Object and the colibri semantic core.
     *
     * @param object    The object which should be registered as a service.
     * @return          The addService message.
     */
    public static ColibriMessage createAddServiceMessage(ObixObject object) {
        ColibriMessage msg = new ColibriMessage(MessageIdentifier.ADD,
                new ColibriMessageHeader(ContentType.APPLICATION_RDF_XML),
                new ColibriMessageContent(ColibriMessageContentCreator.createAddServiceMessageContent(object)));
        msg.setOptionalObixObject(object);
        return msg;
    }

    /**
     * Creates a removeService message of this connector for the given obix Object and the colibri semantic core.
     *
     * @param object    The object which represents the service should be removed.
     * @return          The removeService message.
     */
    public static ColibriMessage createRemoveServiceMessage(ObixObject object) {
        ColibriMessage msg = new ColibriMessage(MessageIdentifier.REM,
                new ColibriMessageHeader(ContentType.TEXT_PLAIN),
                new ColibriMessageContent(object.getServiceUri()));
        msg.setOptionalObixObject(object);
        return msg;
    }

    /**
     * Creates a observeService message of this connector for the given obix Object which represents the service.
     *
     * @param object    The object which represents the service which is observed.
     * @return          The observeService message.
     */
    public static ColibriMessage createObserveServiceMessage(ObixObject object) {
        ColibriMessage msg = new ColibriMessage(MessageIdentifier.OBS,
                new ColibriMessageHeader(ContentType.TEXT_PLAIN),
                new ColibriMessageContent(object.getServiceUri()));
        msg.setOptionalObixObject(object);
        return msg;
    }

    /**
     * Creates a detachService message of this connector for the given obix Object which represents the service.
     *
     * @param object    The object which represents the service which is detached.
     * @return          The detachService message.
     */
    public static ColibriMessage createDetachServiceMessage(ObixObject object) {
        ColibriMessage msg = new ColibriMessage(MessageIdentifier.DET,
                new ColibriMessageHeader(ContentType.TEXT_PLAIN),
                new ColibriMessageContent(object.getServiceUri()));
        msg.setOptionalObixObject(object);
        return msg;
    }

    public static ColibriMessage createStatusMessage(StatusCode statusCode, String description) {
        return  new ColibriMessage(MessageIdentifier.STA,
                new ColibriMessageHeader(ContentType.TEXT_PLAIN),
                new ColibriMessageContent(statusCode.getCode() + " " + statusCode.toString() + newLine + description));
    }

    public static ColibriMessage createStatusMessage(StatusCode statusCode, String description, String referenceId) {
        return  new ColibriMessage(MessageIdentifier.STA,
                new ColibriMessageHeader(ContentType.TEXT_PLAIN, referenceId),
                new ColibriMessageContent(statusCode.getCode() + " " + statusCode.toString() + newLine + description));
    }

    public static ColibriMessage createPutMessage(ObixObject object) {
        return  new ColibriMessage(MessageIdentifier.PUT,
                new ColibriMessageHeader(ContentType.APPLICATION_RDF_XML),
                new ColibriMessageContent(ColibriMessageContentCreator.createPutMessageContent(object)));
    }

    public static ColibriMessage createPutMessage(ObixObject object, String referenceId) {
        return  new ColibriMessage(MessageIdentifier.STA,
                new ColibriMessageHeader(ContentType.APPLICATION_RDF_XML, referenceId),
                new ColibriMessageContent(ColibriMessageContentCreator.createPutMessageContent(object)));
    }

    public MessageIdentifier getMsgType() {
        return msgType;
    }

    public void setMsgType(MessageIdentifier msgType) {
        this.msgType = msgType;
    }

    public ColibriMessageHeader getHeader() {
        return header;
    }

    public void setHeader(ColibriMessageHeader header) {
        this.header = header;
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

    @Override
    public String toString() {
        return msgType.getIdentifier() + newLine + header.getMessageHeaderAsString() + newLine + content.getContent();
    }
}
