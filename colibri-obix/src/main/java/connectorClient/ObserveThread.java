package connectorClient;

import channel.Connector;
import channel.colibri.ColibriChannel;
import channel.message.colibriMessage.ColibriMessage;
import channel.obix.ObixChannel;
import model.obix.ObixObject;

import javax.swing.*;

public class ObserveThread implements Runnable {

    private JCheckBox checkBox;
    private ObixObject obj;
    private JTextField textField;
    private ColibriChannel colibriChannel;
    private ObixChannel obixChannel;
    private Boolean stop;

    public ObserveThread(JCheckBox checkBox, JTextField textField, ObixObject obj, Connector connector) {
        this.checkBox = checkBox;
        this.textField = textField;
        this.obj = obj;
        this.colibriChannel = connector.getColibriChannel();
        this.obixChannel = connector.getObixChannel();
        this.stop = false;
    }

    public void run() {
        textField.setText("NOT OBSERVED");
        while (!stop) {
            synchronized (obj) {
                try {
                    obj.wait();
                } catch (InterruptedException e) {
                    System.out.println("Aborting observation on " + obj.getObixUri());
                    return;
                }
            }
            if(obj.getObservedByColibri() && !obj.getSetByColibri() && colibriChannel.getRegistered() && checkBox.isSelected()) {
                colibriChannel.send(ColibriMessage.createPutMessage(obj));
                textField.setText(obj.toString());
            }
         //   else if(obj.getObservesColibriActions() && obj.getSetByColibri()) {
            else if(obj.getSetByColibri()) {
                obixChannel.put(obj);
                obj.setSetByColibri(false);
                textField.setText(obj.toString());
            } else if(checkBox.isSelected()) {
                textField.setText(obj.toString());
            }
            else {
                textField.setText("NOT OBSERVED");
            }

        }
    }

    public void stop() {
        this.stop = true;
    }
}
