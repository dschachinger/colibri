package openADR.OADRMsgInfo;

/**
 * Created by georg on 07.06.16.
 * This class holds the important information for a oadrCanceledPartyRegistration message.
 */
public class MsgInfo_OADRCanceledPartyRegistration implements OADRMsgInfo {
    // Identifier for Registration transaction.
    private String registrationID;

    public String getRegistrationID() {
        return registrationID;
    }

    public void setRegistrationID(String registrationID) {
        this.registrationID = registrationID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return "oadrCanceledPartyRegistration";
    }
}
