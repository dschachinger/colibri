package connectorClient;

import channel.Connector;
import channel.colibri.ColibriChannel;
import model.obix.ObixObject;

import javax.swing.*;

public class ObixObservationUpdates{

    private JCheckBox checkBox;
    private ObixObject obj;
    private JTextField textField;
    private ColibriChannel colibriChannel;

    public ObixObservationUpdates(JCheckBox checkBox, JTextField textField, ObixObject obj, Connector connector) {
        this.checkBox = checkBox;
        this.textField = textField;
        this.obj = obj;
        this.colibriChannel = connector.getColibriChannel();
    }

    public void run() {
        textField.setText("NOT OBSERVED");
            if(obj.getObservedByColibri() && obj.getSetByObix() && colibriChannel.getRegistered() && checkBox.isSelected()) {
                obj.getPutMessageToColibriTask().execute(obj);
                textField.setText(obj.toString());
                obj.setSetByObix(false);
            } else if(checkBox.isSelected()) {
                textField.setText(obj.toString());
            }
            else {
                textField.setText("NOT OBSERVED");
            }
    }

    public ObixObject getObj() {
        return obj;
    }
}
