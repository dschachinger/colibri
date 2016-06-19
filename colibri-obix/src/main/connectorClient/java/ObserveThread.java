import model.ObixObject;

import javax.swing.*;

public class ObserveThread implements Runnable {

    private JCheckBox checkBox;
    private ObixObject obj;
    private JFrame mainFrame;
    private JTextField textField;

    public ObserveThread(JCheckBox checkBox, JTextField textField, ObixObject obj, JFrame mainFrame) {
        this.checkBox = checkBox;
        this.textField = textField;
        this.obj = obj;
        this.mainFrame = mainFrame;
    }

    public void run() {
        textField.setText(obj.getObj().toString());
        while (checkBox.isSelected()) {
            synchronized (obj) {
                try {
                    obj.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            textField.setText(obj.getObj().toString());
        }
        textField.setText("NOT OBSERVED");
    }
}
