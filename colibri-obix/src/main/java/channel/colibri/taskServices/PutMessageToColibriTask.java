package channel.colibri.taskServices;

import channel.colibri.ColibriChannel;
import channel.message.colibriMessage.ColibriMessage;
import model.obix.ObixObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

public class PutMessageToColibriTask extends TimerTask {

    private List<ObixObject> bundledObjects;
    private ColibriChannel channel;
    private String observeMessageId;
    private boolean scheduled;
    private ObixObject lastObixObjectForPut;

    public PutMessageToColibriTask(ObixObject initialObixObject, ColibriChannel colibriChannel, String observeMessageId) {
        this.lastObixObjectForPut = initialObixObject;
        this.bundledObjects = Collections.synchronizedList(new ArrayList<>());
        bundledObjects.add(initialObixObject);
        this.channel = colibriChannel;
        this.observeMessageId = observeMessageId;
        this.scheduled = false;
    }

    @Override
    public void run() {
        sendPutMessage();
    }

    /**
     * Sends PUT message for the specified oBIX object if the task is not scheduled.
     * Otherwise it adds the object to the objectBundle to send it in one PUT message at the tasks schedule.
     *
     * @param obj The Object for which the PUT message is sent
     */
    public void execute(ObixObject obj) {
        addObjectToBundle(obj);
        if(!scheduled) {
            sendPutMessage();
        }
    }

    private void sendPutMessage() {
        if (bundledObjects.isEmpty()) {
            bundledObjects.add(lastObixObjectForPut);
        }
        channel.send(ColibriMessage.createPutMessage(bundledObjects, observeMessageId));
        bundledObjects.clear();
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }

    public void addObjectToBundle(ObixObject obj) {
        ObixObject temp = new ObixObject(obj.getObixUri(), obj.getObixChannelPort());
        temp.setParameter1(obj.getParameter1());
        temp.setParameter2(obj.getParameter2());
        bundledObjects.add(temp);
        lastObixObjectForPut = temp;
    }
}
