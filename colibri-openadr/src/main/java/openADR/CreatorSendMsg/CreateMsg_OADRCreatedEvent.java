package openADR.CreatorSendMsg;

import com.enernoc.open.oadr2.model.v20b.OadrCreatedEvent;
import com.enernoc.open.oadr2.model.v20b.ei.EventResponses;
import com.enernoc.open.oadr2.model.v20b.ei.QualifiedEventID;
import com.enernoc.open.oadr2.model.v20b.ei.ResponseCode;
import com.enernoc.open.oadr2.model.v20b.pyld.EiCreatedEvent;
import openADR.OADRMsgInfo.MsgInfo_OADRCreatedEvent;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.ProcessorReceivedMsg.ProcessorReceivedMsg;
import openADR.Utils.OADRConInfo;
import openADR.Utils.OADRMsgObject;

import java.util.HashMap;

/**
 * Created by georg on 07.06.16.
 * This class is used to create oadrCreatedEvent messages.
 */
public class CreateMsg_OADRCreatedEvent extends CreateSendMsg {

    /**
     * Creates a message object with an openADR payload OadrCreatedEvent in it.
     * @param info message info: contains the needed information to create a openADR payload
     * @param receivedMsgMap contains all received messages
     * @return
     */
    @Override
    public OADRMsgObject genSendMsg(OADRMsgInfo info, HashMap<String, OADRMsgInfo> receivedMsgMap) {
        MsgInfo_OADRCreatedEvent con_info = (MsgInfo_OADRCreatedEvent) info;

        OadrCreatedEvent msg = new OadrCreatedEvent();
        // String reqID = OADRConInfo.getUniqueRequestId();

        msg.setSchemaVersion("2.0b");

        EiCreatedEvent eiCreatedEvent = new EiCreatedEvent();
        eiCreatedEvent.setVenID(OADRConInfo.getVENId());

        eiCreatedEvent.setEiResponse(ProcessorReceivedMsg.genEiRespone(null, "200"));

        EventResponses eventResponses = new EventResponses();
        for(MsgInfo_OADRCreatedEvent.EventResponse info_eventResp : con_info.getEventResponses()){
            EventResponses.EventResponse eventResponse = new EventResponses.EventResponse();
            eventResponse.setOptType(info_eventResp.getOptType());
            eventResponse.setRequestID(info_eventResp.getRequestID());
            eventResponse.setResponseDescription("OK");
            ResponseCode responseCode = new ResponseCode();
            responseCode.setValue("200");
            eventResponse.setResponseCode(responseCode);

            QualifiedEventID qualifiedEventID = new QualifiedEventID();
            qualifiedEventID.setEventID(info_eventResp.getEventID());
            qualifiedEventID.setModificationNumber(info_eventResp.getModificationNumber());
            eventResponse.setQualifiedEventID(qualifiedEventID);

            eventResponses.getEventResponses().add(eventResponse);
        }



        eiCreatedEvent.setEventResponses(eventResponses);

        msg.setEiCreatedEvent(eiCreatedEvent);

        OADRMsgObject obj = new OADRMsgObject(info.getMsgType(), null, msg);

        return obj;
    }

    /**
     * This method returns the message type name for an oadrCreatedEvent message
     * @return supported messege type
     */
    @Override
    public String getMsgType() {
        return new MsgInfo_OADRCreatedEvent().getMsgType();
    }

    /**
     * {@inheritDoc}
     */
    public boolean doSendMsgViolateMsgOrderAndUpdateRecMap(OADRMsgInfo info, HashMap<String, OADRMsgInfo> receivedMsgMap){
        if(OADRConInfo.getVENId() == null){
            return true;
        }

        if(receivedMsgMap.get("oadrDistributeEvent") == null){
            return true;
        }
        receivedMsgMap.remove("oadrDistributeEvent");

        return false;
    }
}
