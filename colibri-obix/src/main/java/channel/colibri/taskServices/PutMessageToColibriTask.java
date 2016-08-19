package channel.colibri.taskServices;

import channel.colibri.ColibriChannel;
import channel.message.colibriMessage.ColibriMessage;
import model.obix.ObixObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

/**
 * Represents Tasks for (un)scheduled sending of PUT messages to the colibri web socket endpoint.
 */
public class PutMessageToColibriTask extends TimerTask {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    /**
     * List of {@link ObixObject} which represents all value changes of a specific obix service since the last PUT message.
     */
    private List<ObixObject> bundledObjects;

    private ColibriChannel channel;

    /**
     * The ID of the OBS message which started the task.
     */
    private String observeMessageId;

    /**
     * True, if the task has a schedule.
     */
    private boolean scheduled;

    /**
     * Last change on the {@link ObixObject} according to this task.
     */
    private ObixObject lastObixObjectForPut;

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    public PutMessageToColibriTask(ObixObject initialObixObject, ColibriChannel colibriChannel, String observeMessageId) {
        this.lastObixObjectForPut = initialObixObject;
        this.bundledObjects = Collections.synchronizedList(new ArrayList<>());
        this.bundledObjects.add(initialObixObject);
        this.channel = colibriChannel;
        this.observeMessageId = observeMessageId;
        this.scheduled = false;
    }

    /******************************************************************
     *                            Methods                             *
     ******************************************************************/

    @Override
    public void run() {
        sendPutMessage();
    }

    /**
     * Sends PUT message for the specified {@link ObixObject} if the task is not scheduled.
     * Otherwise it adds the object to the objectBundle to send it in one PUT message at the tasks schedule.
     *
     * @param obj The {@link ObixObject} for which the PUT message is sent
     */
    public void execute(ObixObject obj) {
        addObjectToBundle(obj);
        if(!scheduled) {
            sendPutMessage();
        }
    }

    /**
     * Sends a PUT message to the colibri semantic core.
     */
    private void sendPutMessage() {
        if (bundledObjects.isEmpty()) {
            bundledObjects.add(lastObixObjectForPut);
        }
        channel.send(ColibriMessage.createPutMessage(bundledObjects, observeMessageId));
        bundledObjects.clear();
    }

    /**
     * Adds an {@link ObixObject} to the bundle which contains all value changes of the object since the last PUT message.
     *
     * @param obj   The {@link ObixObject} with the updated value.
     */
    private void addObjectToBundle(ObixObject obj) {
        ObixObject temp = new ObixObject(obj.getObixUri(), obj.getObixChannelPort());
        temp.setParameter1(obj.getParameter1());
        temp.setParameter2(obj.getParameter2());
        bundledObjects.add(temp);
        lastObixObjectForPut = temp;
    }

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }
}
