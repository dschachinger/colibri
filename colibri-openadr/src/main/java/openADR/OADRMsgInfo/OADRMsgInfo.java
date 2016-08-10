package openADR.OADRMsgInfo;

/**
 * Created by georg on 06.06.16.
 * Classes which implement this interface represent an openADR message info.
 * These classes only contain the useful information for an energy consumer and
 * excludes the overhead on information which is needed for the communication.
 */
public interface OADRMsgInfo {
    /**
     * This method returns which message type the class supports.
     * @return supported messege type
     */
    String getMsgType();
}
