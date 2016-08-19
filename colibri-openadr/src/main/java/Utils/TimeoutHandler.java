package Utils;


import java.util.Map;

/**
 * Created by georg on 20.07.16.
 * This class is used in combination with the TimeoutWatcher and it defines how to react on a timeout.
 */
public interface TimeoutHandler<S, T> {
    public void handleTimeout(S client, Map<String, T> monitoredMsg, String messageID);
}
