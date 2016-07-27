package channel.obix;

import channel.colibri.ColibriChannel;
import channel.message.colibriMessage.ColibriMessage;
import channel.message.colibriMessage.StatusCode;
import model.obix.ObixObject;

import java.util.TimerTask;

public class PutExecutionTask extends TimerTask {

    private ObixObject obj;
    private ColibriChannel channel;
    private String statusMessageId;

    public PutExecutionTask(ObixObject obj, ColibriChannel colibriChannel, String statusMessageId) {
        this.obj = obj;
        this.channel = colibriChannel;
        this.statusMessageId = statusMessageId;
    }

    @Override
    public void run() {
        synchronized (obj) {
            obj.setSetByColibri(true);
            obj.notify();
        }
        channel.send(ColibriMessage.createStatusMessage(StatusCode.OK, "", statusMessageId));
    }
}
