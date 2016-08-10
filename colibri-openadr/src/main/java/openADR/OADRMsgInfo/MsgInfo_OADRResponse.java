package openADR.OADRMsgInfo;

/**
 * Created by georg on 07.06.16.
 * This class holds the important information for an oadrQueryRegistration message.
 */
public class MsgInfo_OADRResponse implements OADRMsgInfo {

    // This code shows how the origin message was processed
    private int responseCode;
    // Id of the origin message
    private String correspondingRequestID;
    // Description shows how the origin message was processed
    private String responseDescription;

    public String getResponseDescription() {
        return responseDescription;
    }

    public void setResponseDescription(String responseDescription) {
        this.responseDescription = responseDescription;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getCorrespondingRequestID() {
        return correspondingRequestID;
    }

    public void setCorrespondingRequestID(String correspondingRequestID) {
        this.correspondingRequestID = correspondingRequestID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return "oadrResponse";
    }
}
