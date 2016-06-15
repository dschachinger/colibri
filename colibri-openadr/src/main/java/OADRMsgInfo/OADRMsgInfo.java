package OADRMsgInfo;

/**
 * Created by georg on 06.06.16.
 * Classes which implements this interface represent a openADR message info.
 * This classes only contains the useful information for a energy consumer and
 * excludes the overhead on information which is needed for the communication.
 */
public interface OADRMsgInfo {
    /**
     * This method returns which message type the class supports.
     * @return supported messege type
     */
    String getMsgType();
}
