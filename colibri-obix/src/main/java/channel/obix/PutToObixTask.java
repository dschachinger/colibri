package channel.obix;

import channel.colibri.ColibriChannel;
import channel.message.colibriMessage.ColibriMessage;
import channel.message.messageObj.PutMessageContent;
import channel.message.messageObj.StatusCode;
import channel.message.service.ColibriMessageContentCreator;
import model.obix.ObixObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.util.TimerTask;

public class PutToObixTask extends TimerTask {

    private ObixObject serviceObject;
    private ColibriChannel colibriChannel;
    private ObixChannel obixChannel;
    private ColibriMessage putMessage;
    private static final Logger logger = LoggerFactory.getLogger(PutToObixTask.class);

    public PutToObixTask(ObixObject serviceObject, ColibriChannel colibriChannel, ObixChannel obixChannel, ColibriMessage putMessage) {
        this.serviceObject = serviceObject;
        this.colibriChannel = colibriChannel;
        this.obixChannel = obixChannel;
        this.putMessage = putMessage;
    }

    public PutToObixTask(PutToObixTask putToObixTaskToCopy) {
        this.serviceObject = putToObixTaskToCopy.serviceObject;
        this.colibriChannel = putToObixTaskToCopy.colibriChannel;
        this.obixChannel = putToObixTaskToCopy.obixChannel;
        this.putMessage = putToObixTaskToCopy.putMessage;
    }

    @Override
    public void run() {
        PutMessageContent content;
        try {
            content = ColibriMessageContentCreator.getPutMessageContent(putMessage);
        } catch (JAXBException e) {
            colibriChannel.send(ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "Unmarshalling PUT message failed!",
                    putMessage.getHeader().getId()));
            return;
        }
        boolean setParam1 = false;
        boolean setParam2 = false;
        if (serviceObject != null) {
            logger.info(serviceObject.getParameter1().getParameterUri());
            if (content.getValue1HasParameterUri().equals(serviceObject.getParameter1().getParameterUri())) {
                if (content.getValue1Uri().equals(serviceObject.getParameter1().getValueUri())) {
                    serviceObject.setValueParameter1(content.getValue1());
                    setParam1 = true;
                }
            }
            if (content.getValue1HasParameterUri().equals(serviceObject.getParameter2().getParameterUri())) {
                if (content.getValue1Uri().equals(serviceObject.getParameter2().getValueUri())) {
                    serviceObject.setValueParameter2(content.getValue1());
                    setParam2 = true;
                }
            }
            if (content.getValue2HasParameterUri().equals(serviceObject.getParameter1().getParameterUri())) {
                if (content.getValue2Uri().equals(serviceObject.getParameter1().getValueUri())) {
                    serviceObject.setValueParameter1(content.getValue2());
                    setParam1 = true;
                }
            }
            if (content.getValue2HasParameterUri().equals(serviceObject.getParameter2().getParameterUri())) {
                if (content.getValue2Uri().equals(serviceObject.getParameter2().getValueUri())) {
                    serviceObject.setValueParameter2(content.getValue2());
                    setParam2 = true;
                }
            }
        }
        if (setParam1 && setParam2) {
            obixChannel.put(serviceObject);
            colibriChannel.send(ColibriMessage.createStatusMessage(StatusCode.OK, "Successfully send PUT message to obix.",
                    putMessage.getHeader().getId()));
        } else {
            colibriChannel.send(ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "PUT message to the service with this address is not possible." +
                    "Please check if the service address is correct.", putMessage.getHeader().getId()));
        }

    }

    public ObixObject getObj() {
        return serviceObject;
    }

    public void setObj(ObixObject serviceObject) {
        this.serviceObject = serviceObject;
    }

    public ColibriMessage getPutMessage() {
        return putMessage;
    }

    public void setPutMessage(ColibriMessage putMessage) {
        this.putMessage = putMessage;
    }
}
