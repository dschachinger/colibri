import model.ObixObject;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class ObserveThread implements Runnable {

    private JCheckBox checkBox;
    private ObixObject obj;
    private JFrame mainFrame;

    public ObserveThread(JCheckBox checkBox, ObixObject obj, JFrame mainFrame) {
        this.checkBox = checkBox;
        this.obj = obj;
        this.mainFrame = mainFrame;
    }

    public void run() {
        String str = obj.getUri() + ": ";
        checkBox.setText(str + obj.getObj().toString());
        while (checkBox.isSelected()) {
            synchronized (obj) {
                try {
                    obj.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            checkBox.setText(str + obj.getObj().toString());
            mainFrame.setVisible(true);
        }
        checkBox.setText(str + "NOT OBSERVED");
        mainFrame.setVisible(true);
    }
}
