package semanticCore.WebSocketHandling;

import Utils.EventType;

/**
 * Created by georg on 13.07.16.
 */
public class ServiceDescriptor {
    private String serviceURL;
    private String messageIDURL;
    private String createdMessageURL;
    private String signalValueURL;
    private String timeIntervalURL;

    private EventType eventType;

    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    public String getMessageIDURL() {
        return messageIDURL;
    }

    public void setMessageIDURL(String messageIDURL) {
        this.messageIDURL = messageIDURL;
    }

    public String getCreatedMessageURL() {
        return createdMessageURL;
    }

    public void setCreatedMessageURL(String createdMessageURL) {
        this.createdMessageURL = createdMessageURL;
    }

    public String getSignalValueURL() {
        return signalValueURL;
    }

    public void setSignalValueURL(String signalValueURL) {
        this.signalValueURL = signalValueURL;
    }

    public String getTimeIntervalURL() {
        return timeIntervalURL;
    }

    public void setTimeIntervalURL(String timeIntervalURL) {
        this.timeIntervalURL = timeIntervalURL;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
