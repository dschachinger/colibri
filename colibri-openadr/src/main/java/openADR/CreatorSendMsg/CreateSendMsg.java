package openADR.CreatorSendMsg;

import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

/**
 * Created by georg on 06.06.16.
 * Descendant Classes from this abstract class are used to transmit a specific openADR message
 */
public abstract class CreateSendMsg {
    /**
     * This method generates an openADR message object out of a message info.
     * The return value contains the payload of the openADR message,
     * but no information about layers underneath the openADR layer.
     * An example layer below the openADR layer is openADR.XMPP or HTTP.
     *
     * @param info message info: contains the needed information to create an openADR payload
     * @return openADR message object
     */
    abstract public OADRMsgObject genSendMsg(OADRMsgInfo info);

    /**
     * This method returns which message type the class supports.
     * @return supported message type
     */
    abstract public String getMsgType();

    /**
     * This method returns if a given send message info object violates openADR constraints
     * when it is sent.
     * @param info given send message info object
     * @return true...It violates constraints, false...It does not violate constraints.
     */
    public abstract boolean doSendMsgViolateMsgOrder(OADRMsgInfo info);

    /**
     * This method copes with all similar constraints checks for the send messages.
     * The parameters specify the needed constraints.
     * @param info check the constraints for the give given send message info object
     * @param checkIfRegistered  true...check if the connector is registered, false..otherwise
     * @return true...constraints are violated, otherwise...everything is fine
     */
    protected boolean checkConstraints(OADRMsgInfo info, boolean checkIfRegistered){
        if(checkIfRegistered && OADRConInfo.getVENId() == null){
            return true;
        }
        return false;
    }

}
