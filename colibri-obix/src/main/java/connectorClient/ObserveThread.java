package connectorClient;

import channel.colibri.ColibriChannel;
import channel.message.colibriMessage.ColibriMessage;
import channel.message.colibriMessage.StatusCode;
import model.obix.ObixObject;

import javax.swing.*;

public class ObserveThread implements Runnable {

    private JCheckBox checkBox;
    private ObixObject obj;
    private JTextField textField;
    private ColibriChannel colibriChannel;

    public ObserveThread(JCheckBox checkBox, JTextField textField, ObixObject obj, ColibriChannel colibriChannel) {
        this.checkBox = checkBox;
        this.textField = textField;
        this.obj = obj;
        this.colibriChannel = colibriChannel;
    }

    public void run() {
        textField.setText(obj.toString());
        while (checkBox.isSelected()) {
            synchronized (obj) {
                try {
                    obj.wait();
                } catch (InterruptedException e) {
                    System.out.println("Aborting observation on " + obj.getUri());
                    obj.getRelation().proactiveCancel();
                    return;
                }
            }
            textField.setText(obj.toString());
            //TODO:CHANGE
            if(obj.getObservedByColibri()) {
                colibriChannel.send(ColibriMessage.createStatusMessage(StatusCode.OK, "PARAM1: " + obj.getParameter1().getValueAsString() +
                        "<br>" + "PARAM2: " + obj.getParameter2().getValueAsString()));
            }
        }
        textField.setText("NOT OBSERVED");

    }
}
