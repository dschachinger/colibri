package OADRHandling;

import CreatorSendMsg.*;
import OADRMsgInfo.*;
import ProcessorReceivedMsg.*;
import Utils.OADRMsgObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by georg on 02.06.16.
 * This classes implements a singelton pattern so that only one object can be instantiated.
 * Get the single object with the method getController().
 */
public class Controller {
    /* This hash map holds the sended messages and
        the key is the openADR requestID */
    private HashMap<String, OADRMsgObject> sendedMsgMap;

    /* This hash map holds the received messages and
        the key is the openADR message type */
    private HashMap<String, OADRMsgInfo> receivedMsgMap;

    /* This hash map holds the processors to deal with different receive messages and
        the key is the openADR message type */
    private HashMap<String, ProcessorReceivedMsg> procReceivedMsgMap;

    /* This hash map holds the Objects to create the different send messages and
        the key is the openADR message type */
    private HashMap<String, CreateSendMsg> createSendMsgMap;

    // the only controller object --> singelton pattern
    private static Controller controller;

    /**
     * This constructor is not visible outsidem due to the singelton pattern.
     * It will initiate a controller object.
     */
    private Controller(){
        // init sendedMsgMap
        sendedMsgMap = new HashMap<>();

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

        for(CreateSendMsg createSendMsg : createSendMsgList){
            createSendMsgMap.put(createSendMsg.getMsgType(), createSendMsg);
        }

        /* init procReceivedMsgMap                                  */
        procReceivedMsgMap = new HashMap<>();
        List<ProcessorReceivedMsg> procReceivedMsgList = new ArrayList<>();

        // add all possible ProcessorReceivedMsg types
        procReceivedMsgList.add(new Process_OADRCreatedPartyRegistration());
        procReceivedMsgList.add(new Process_OADRRequestReregistration());
        procReceivedMsgList.add(new Process_OADRCanceledPartyRegistration());
        procReceivedMsgList.add(new Process_OADRCancelPartyRegistration());
        procReceivedMsgList.add(new Process_OADRDistributeEvent());

        for(ProcessorReceivedMsg processorReceivedMsg : procReceivedMsgList){
            procReceivedMsgMap.put(processorReceivedMsg.getMsgType(), processorReceivedMsg);
        }


    }

    /**
     * Returns the only controller object. If there is none than it creates one.
     * @return
     */
    public static Controller getController(){
        if(controller == null){
            controller = new Controller();
        }
        return controller;
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

        // extreact information
        OADRMsgInfo recInfo = proc.extractInfo(recObj);
        if(recInfo != null){
            receivedMsgMap.put(recInfo.getMsgType(), recInfo);
        }

        // generate reply
        OADRMsgObject respObj= proc.genResponse(recObj);
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
        if(sendObj.getID() != null){
            sendedMsgMap.put(sendObj.getID(), sendObj);
        }

        return sendObj;
    }
}