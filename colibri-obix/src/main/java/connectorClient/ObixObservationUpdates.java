package connectorClient;

import channel.Connector;
import channel.colibri.ColibriChannel;
import model.obix.ObixObject;
import javax.swing.*;

/**
 * Updates an {@link ObixObject} and its representation on actions from colibri or obix.
 */
public class ObixObservationUpdates implements Runnable{

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    /**
     * The checkbox which indicates if colibri observes actions from the {@link ObixObject} in this class.
     */
    private JCheckBox observeObixcheckBox;

    /**
     * The {@link ObixObject} which is observed.
     */
    private ObixObject obj;

    /**
     * The textfield which is connected to the value of the {@link ObixObject}.
     */
    private JTextField textField;

    /**
     * The colibri channel which observes the {@link ObixObject} in this class.
     */
    private ColibriChannel colibriChannel;

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    public ObixObservationUpdates(JCheckBox observeObixcheckBox, JTextField textField, ObixObject obj, Connector connector) {
        this.observeObixcheckBox = observeObixcheckBox;
        this.textField = textField;
        this.obj = obj;
        this.colibriChannel = connector.getColibriChannel();
    }

    /******************************************************************
     *                            Methods                             *
     ******************************************************************/

    public void run() {
        textField.setText("NOT OBSERVED");
            if(obj.getObservedByColibri() && obj.getSetByObix() && colibriChannel.getRegistered() && observeObixcheckBox.isSelected()) {
                obj.getPutMessageToColibriTask().execute(obj);
                textField.setText(obj.toString());
                obj.setSetByObix(false);
            } else if(observeObixcheckBox.isSelected()) {
                textField.setText(obj.toString());
            }
            else {
                textField.setText("NOT OBSERVED");
            }
    }

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public ObixObject getObj() {
        return obj;
    }
}
