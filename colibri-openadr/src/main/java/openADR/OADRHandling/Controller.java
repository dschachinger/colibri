package openADR.OADRHandling;

import openADR.CreatorSendMsg.*;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.ProcessorReceivedMsg.*;
import openADR.Utils.OADRMsgObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by georg on 02.06.16.
 * Objects from this class handel the creation of openADR send messages
 * and the processing of the received messages.
 */
public class Controller {

    /* This hash map holds the processors to deal with different receive messages and
        the key is the openADR message type */
    private HashMap<String, ProcessorReceivedMsg> procReceivedMsgMap;

    /* This hash map holds the Objects to create the different send messages and
        the key is the openADR message type */
    private HashMap<String, CreateSendMsg> createSendMsgMap;

    /* This variable holds the openADR party which uses this controller.
     */
    private OADRParty party;

    private Logger logger = LoggerFactory.getLogger(Controller.class);

    /**
     * It will initiate a controller object.
     */
    public Controller(OADRParty party){
        this.party = party;

        // init createSendMsgMap
        createSendMsgMap = new HashMap<>();
        List<CreateSendMsg> createSendMsgList = new ArrayList<>();

        // add all possible CreateSendMsg types
        createSendMsgList.add(new CreateMsg_OADRCancelPartyRegistration());
        createSendMsgList.add(new CreateMsg_OADRCreatePartyRegistration());
        createSendMsgList.add(new CreateMsg_OADRQueryRegistration());
        createSendMsgList.add(new CreateMsg_OADRRequestEvent());
        createSendMsgList.add(new CreateMsg_OADRCreatedEvent());
        createSendMsgList.add(new CreateMsg_OADRRegisterReport());
        createSendMsgList.add(new CreateMsg_OADRUpdateReport());


        for(CreateSendMsg createSendMsg : createSendMsgList){
            createSendMsgMap.put(createSendMsg.getMsgType(), createSendMsg);
        }

        logger.info("amount of different send message types: " + createSendMsgMap.keySet().size());

        /* init procReceivedMsgMap                                  */
        procReceivedMsgMap = new HashMap<>();
        List<ProcessorReceivedMsg> procReceivedMsgList = new ArrayList<>();

        // add all possible openADR.ProcessorReceivedMsg types
        procReceivedMsgList.add(new Process_OADRCreatedPartyRegistration());
        procReceivedMsgList.add(new Process_OADRRequestReregistration());
        procReceivedMsgList.add(new Process_OADRCanceledPartyRegistration());
        procReceivedMsgList.add(new Process_OADRCancelPartyRegistration());
        procReceivedMsgList.add(new Process_OADRDistributeEvent());
        procReceivedMsgList.add(new Process_OADRResponse());
        procReceivedMsgList.add(new Process_OADRCancelReport());
        procReceivedMsgList.add(new Process_OADRCreateReport());
        procReceivedMsgList.add(new Process_OADRRegisteredReport());
        procReceivedMsgList.add(new Process_OADRUpdatedReport());
        procReceivedMsgList.add(new Process_OADRRegisterReport());

        for(ProcessorReceivedMsg processorReceivedMsg : procReceivedMsgList){
            procReceivedMsgMap.put(processorReceivedMsg.getMsgType(), processorReceivedMsg);
        }

    }

    /**
     * This method returns all supported openADR received messages types
     * @return Set of all supported openADR received messages types
     */
    public Set<String> getSupportedReceivedMsgTypes(){
        return procReceivedMsgMap.keySet();
    }

    /**
     * This class extract the needful information from a given received message and will
     * return the proper reply message.
     * @param recObj process this received message
     * @return reply message
     */
    public OADRMsgObject processReceivedMessage(OADRMsgObject recObj){
        ProcessorReceivedMsg proc = procReceivedMsgMap.get(recObj.getMsgType());

        String statusCode = proc.doRecMsgViolateConstraints(recObj,party.getChannel().getSendedMsgMap());
        if(statusCode.equals("200")){
            logger.info("received message type " + recObj.getMsgType()+
                    " does not violate "+"constraints");

            // extract information
            OADRMsgInfo recInfo = proc.extractInfo(recObj, party);
            if(recInfo != null){
                party.getBridge().informationFlowFromOpenADRToColibri(recInfo);
            }
            proc.updateSendedMsgMap(recObj, party.getChannel().getSendedMsgMap());
        } else {
            logger.error("received message type " + recObj.getMsgType()+
                    " VIOLATES "+"constraints. status code " + statusCode + " info: " +
                    ProcessorReceivedMsg.respValueText.get(statusCode));

        }

        // generate reply
        OADRMsgObject respObj= proc.genResponse(recObj, statusCode);

        return respObj;
    }

    /**
     * This method creates an openADR send message out of the given info object
     * @param info create a message with these inforamtion
     * @return an openADR send message
     */
    public OADRMsgObject createSendMsg(OADRMsgInfo info){
        CreateSendMsg createSendMsg = createSendMsgMap.get(info.getMsgType());
        OADRMsgObject sendObj = createSendMsg.genSendMsg(info);

        boolean violate = createSendMsg.doSendMsgViolateMsgOrder(info);
        if(violate){
            logger.error("send message type " + info.getMsgType()+
                    " VIOLATES " + "constraints");
        } else {
            logger.info("send message type " + info.getMsgType()+
                    " does not violate "+"constraints");
        }




        return sendObj;
    }


}