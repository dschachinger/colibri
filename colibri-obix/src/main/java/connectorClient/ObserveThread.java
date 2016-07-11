package connectorClient;

import channel.colibri.ColibriChannel;
import channel.message.colibriMessage.ColibriMessage;
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
        colibriChannel.send(ColibriMessage.createPutMessage(obj));
        while (checkBox.isSelected()) {
            synchronized (obj) {
                try {
                    obj.wait();
                } catch (InterruptedException e) {
                    System.out.println("Aborting observation on " + obj.getObixUri());
                    obj.getRelation().proactiveCancel();
                    return;
                }
            }
            textField.setText(obj.toString());
            if(obj.getObservedByColibri()) {
                colibriChannel.send(ColibriMessage.createPutMessage(obj));
            }
        }
        textField.setText("NOT OBSERVED");

    }
}
