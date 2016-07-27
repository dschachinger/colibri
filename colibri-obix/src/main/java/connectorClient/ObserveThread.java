package connectorClient;

import channel.Connector;
import channel.colibri.ColibriChannel;
import channel.obix.ObixChannel;
import model.obix.ObixObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.RunAndStopAble;

import javax.swing.*;

public class ObserveThread implements RunAndStopAble {

    private JCheckBox checkBox;
    private ObixObject obj;
    private JTextField textField;
    private ColibriChannel colibriChannel;
    private ObixChannel obixChannel;
    private Boolean stop;
    private static final Logger logger = LoggerFactory.getLogger(ObserveThread.class);

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
                    logger.info("Stopping oBix data observation of " + obj.getObixUri());
                    return;
                }
            }
            if(obj.getObservedByColibri() && !obj.getSetByColibri() && colibriChannel.getRegistered() && checkBox.isSelected()) {
                obj.getPutMessageToColibriTask().execute(obj);
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
