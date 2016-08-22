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

/**
 * This class represents a task which is used for the scheduled sending of PUT-messages to an OBIX gateway.
 */
public class PutToObixTask extends TimerTask {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    /**
     * The object which represents the service to which the PUT message is referring.
     */
    private ObixObject serviceObject;

    private ColibriChannel colibriChannel;
    private ObixChannel obixChannel;
    private ColibriMessage putMessage;
    private static final Logger logger = LoggerFactory.getLogger(PutToObixTask.class);

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

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
        /**
         * If the PUT message received from Colibri contains a time parameter to schedule a PUT message to OBIX,
         * this parameter is parsed in {@link ColibriChannel#setTimerTask(ObixObject, ColibriMessage, PutMessageContent)}.
         * The run() method in {@link PutToObixTask} is then executed according to the parsed schedule.
         */
        PutMessageContent content;
        try {
            // parse the PUT message content
            content = ColibriMessageContentCreator.getPutMessageContent(putMessage);
        } catch (JAXBException e) {
            colibriChannel.send(ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "Unmarshalling PUT message failed!",
                    putMessage.getHeader().getId()));
            return;
        }
        boolean setParam1 = false;
        boolean setParam2 = false;
        /**
         * Check if there are boolean values in the received
         * PUT {@link channel.message.colibriMessage.ColibriMessageContent}.
         * If there are boolean values, than check if they are correct boolean values,
         * i.e. if they are either "true" or "false".
         */
        if(!checkBooleanValuesForCorrectness(content.getValue1().getDatatype(),
                content.getValue1().getValue())
            || !checkBooleanValuesForCorrectness(content.getValue2().getDatatype(),
                content.getValue2().getDatatype())) {
            colibriChannel.send(ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "One of the received " +
                    "boolean values is neither true nor false in message: ", putMessage.getHeader().getId()));
            return;
        }
        if (serviceObject != null) {
            /**
             * Parse the parameters of the received PUT message and check if the parameters are fitting the
             * {@link ObixObject}  serviceObject.
             */
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
            /**
             * The parameters in the PUT message were successfully parsed and are fitting the according
             * {@link ObixObject} serviceObject.
             */
            obixChannel.put(serviceObject);
            colibriChannel.send(ColibriMessage.createStatusMessage(StatusCode.OK, "Successfully send PUT message to obix.",
                    putMessage.getHeader().getId()));
        } else {
            colibriChannel.send(ColibriMessage.createStatusMessage(StatusCode.ERROR_SEMANTIC, "PUT message to the service with this address is not possible." +
                    "Please check if the service address is correct.", putMessage.getHeader().getId()));
        }

    }

    /**
     * This method returns true, if the given value and its type are correct booleans.
     *
     * @param valueType     The value type of the value which is checked.
     * @param valueToCheck  The value which is checked.
     * @return              True, if the given value and its type are correct booleans, otherwise false.
     */
    private boolean checkBooleanValuesForCorrectness(String valueType, String valueToCheck) {
        if(valueType.contains("boolean")) {
            return ((valueToCheck.equals("true")) || (valueToCheck.equals("false")));
        }
        return true;
    }

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public ObixObject getObj() {
        return serviceObject;
    }

    public void setObj(ObixObject serviceObject) {
        this.serviceObject = serviceObject;
    }

    public void setPutMessage(ColibriMessage putMessage) {
        this.putMessage = putMessage;
    }
}
