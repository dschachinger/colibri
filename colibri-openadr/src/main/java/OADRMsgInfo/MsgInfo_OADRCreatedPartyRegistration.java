package OADRMsgInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georg on 07.06.16.
 * This class holds the important information for a oadrCreatedPartyRegistration message.
 */
public class MsgInfo_OADRCreatedPartyRegistration implements OADRMsgInfo {
    private List<String> transportMethods;
    /* Identifier for Registration transaction. Not included in response to
        query registration unless already registered */
    private String registrationID;

    public MsgInfo_OADRCreatedPartyRegistration() {
        this.transportMethods = new ArrayList<>();
    }

    public List<String> getTransportMethods() {
        return transportMethods;
    }

    public void addTransportMethod(String transportMethod) {
        this.transportMethods.add(transportMethod);
    }

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
        return "oadrCreatedPartyRegistration";
    }
}
