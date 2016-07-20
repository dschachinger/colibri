package Utils;


import java.util.Map;

/**
 * Created by georg on 20.07.16.
 */
public interface TimeoutHandler<S, T> {
    public void handleTimeout(S client, Map<String, T> monitoredMsg, String messageID);
}
