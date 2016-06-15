package OADRMsgInfo;

import com.enernoc.open.oadr2.model.v20b.ei.OptTypeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georg on 07.06.16.
 * This class holds the important information for a oadrCreatedEvent message.
 */
public class MsgInfo_OADRCreatedEvent implements OADRMsgInfo {
    // optIn or optOut responses for received events
    private List<EventResponse> eventResponses;

    public MsgInfo_OADRCreatedEvent() {
        this.eventResponses = new ArrayList<>();
    }

    public List<MsgInfo_OADRCreatedEvent.EventResponse> getEventResponses() {
        return eventResponses;
    }

    public EventResponse getNewEventResponse(){
        return new EventResponse();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsgType() {
        return "oadrCreatedEvent";
    }

    public class EventResponse{
        // A ID used to match up a logical transaction request and response
        private String requestID;
        // An ID value that identifies a specific DR event instance.
        private String eventID;
        // Incremented each time an event is modified.
        private long modificationNumber;
        // optIn or optOut of an event, or used to indicate the type of opt schedule
        private OptTypeType optType;

        public String getRequestID() {
            return requestID;
        }

        public void setRequestID(String requestID) {
            this.requestID = requestID;
        }

        public String getEventID() {
            return eventID;
        }

        public void setEventID(String eventID) {
            this.eventID = eventID;
        }

        public long getModificationNumber() {
            return modificationNumber;
        }

        public void setModificationNumber(long modificationNumber) {
            this.modificationNumber = modificationNumber;
        }

        public OptTypeType getOptType() {
            return optType;
        }

        public void setOptType(OptTypeType optType) {
            this.optType = optType;
        }
    }
}
