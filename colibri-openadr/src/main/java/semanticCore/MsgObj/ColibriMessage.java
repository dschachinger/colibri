package semanticCore.MsgObj;

/**
 * Created by georg on 28.06.16.
 * Objects from this class represent a colibri message
 */
public class ColibriMessage {
    // Describes which message type it is
    private MsgType msgType;
    // This variable holds the header of the message
    private Header header;
    // This variable holds the content of the message in string representation
    private String content;
    // This variable holds the content of the message in object representation.
    private Object contentObj;
    // This variable holds the message which leads to this ColibriMessage
    private ColibriMessage originMessage;

    // This variable indicates how often a message was resent due to timeouts.
    private int resendIteration;

    public ColibriMessage(MsgType msgType, Header header, String content, Object contentObj){
        this.msgType = msgType;
        this.header = header;
        this.content = content;
        this.contentObj = contentObj;
        resendIteration = 0;
    }

    public ColibriMessage(MsgType msgType, Header header, String content){
        this(msgType, header, content, null);
    }

    public ColibriMessage(ColibriMessage other){
        this(other.getMsgType(), new Header(other.getHeader()), other.getContent(), other.getContentObj());
        resendIteration = other.getResendIteration();
    }

    public Object getContentObj() {
        return contentObj;
    }

    public ColibriMessage getOriginMessage() {
        return originMessage;
    }

    public void setOriginMessage(ColibriMessage originMessage) {
        this.originMessage = originMessage;
    }

    public MsgType getMsgType() {
        return msgType;
    }

    public Header getHeader() {
        return header;
    }

    public String getContent() {
        return content;
    }

    public int getResendIteration() {
        return resendIteration;
    }

    public void incResendIteration() {
        this.resendIteration++;
    }

    public String toMsgString(){
        String out = "";

        out += msgType.toString() + "\n";
        out += header.toHeaderMsgString();
        if(content != null){
            out += "\n";
            out += content;
        }

        return out;
    }
}
