package openADR.OADRHandling;

import openADR.CreatorSendMsg.*;
import openADR.OADRMsgInfo.OADRMsgInfo;
import openADR.ProcessorReceivedMsg.*;
import openADR.Utils.OADRMsgObject;

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


    /* This hash map holds the received messages and
        the key is the openADR message type */
    private HashMap<String, OADRMsgInfo> receivedMsgMap;

    /* This hash map holds the processors to deal with different receive messages and
        the key is the openADR message type */
    private HashMap<String, ProcessorReceivedMsg> procReceivedMsgMap;

    /* This hash map holds the Objects to create the different send messages and
        the key is the openADR message type */
    private HashMap<String, CreateSendMsg> createSendMsgMap;

    /* This variable holds the openADR party which uses this controller.
     */
    private OADRParty party;

    /**
     * It will initiate a controller object.
     */
    public Controller(OADRParty party){
        this.party = party;

        // init receivedMsgMap
        receivedMsgMap = new HashMap<>();

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

        System.out.println("amount of send: " + createSendMsgMap.keySet().size());

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

        // extract information
        OADRMsgInfo recInfo = proc.extractInfo(recObj, party);
        if(recInfo != null){
            receivedMsgMap.put(recInfo.getMsgType(), recInfo);
            party.getBridge().informationFlowFromOpenADRToColibri(recInfo);
        }

        boolean violate = proc.doRecMsgViolateConstraintsAndUpdateSendMap(recObj,party.getChannel().getSendedMsgMap());
        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>> received message type " + recObj.getMsgType()+
                (violate?" VIOLATES ":" does not violates ")+"constraints");

        // generate reply
        // TODO possible: implement different error codes
        OADRMsgObject respObj= proc.genResponse(recObj, violate?"450":"200");



        return respObj;
    }

    /**
     * This method creates an openADR send message out of the given info object
     * @param info create a message with these inforamtion
     * @return an openADR send message
     */
    public OADRMsgObject createSendMsg(OADRMsgInfo info){
        CreateSendMsg createSendMsg = createSendMsgMap.get(info.getMsgType());
        OADRMsgObject sendObj = createSendMsg.genSendMsg(info, receivedMsgMap);

        boolean violate = createSendMsg.doSendMsgViolateMsgOrderAndUpdateRecMap(info,receivedMsgMap);
        System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<<<< send message type " + info.getMsgType()+
                (violate?" VIOLATES ":" does not violates ")+"constraints");



        return sendObj;
    }


}