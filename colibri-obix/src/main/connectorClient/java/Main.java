import channel.CoapChannel;
import channel.ObixChannel;
import channel.ObixXmlChannelDecorator;
import model.ObixLobby;
import model.ObixObject;
import obix.Int;
import obix.Obj;
import obix.Val;
import service.Configurator;
import sun.nio.ch.ThreadPool;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends JPanel {

    private ObixChannel channel;
    private JFrame mainFrame;
    private JPanel valPanel;
    private JPanel namePanel;
    private Map<JCheckBox, ObixObject> checkBoxObixObjectMap = new HashMap<JCheckBox, ObixObject>();
    private ExecutorService executor = Executors.newCachedThreadPool();
    private ObixLobby lobby;

    public Main(ObixChannel channel) {
        this.channel = channel;
        this.lobby = channel.getLobby(channel.getLobbyUri());
        prepareGUI();
    }

    private void prepareGUI() {
        int numRows = lobby.getObixObjects().size() + 1;
        mainFrame = new JFrame("Connector GUI");
        mainFrame.setSize(2000, 2000);
        mainFrame.setLayout(new GridLayout(numRows, 1));
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
    }

    private void displayObixData() {
        /*
            Print lobby Data
         */
        int row = 0;
        for (String s : lobby.getObservedObjectsLists().keySet()) {
            if (!s.equals("all")) {
                List<ObixObject> objects = lobby.getObservedObjectsLists().get(s);
                for (ObixObject o : objects) {

                    JCheckBox tempCheckBox = new JCheckBox(o.getUri() + ": " );
                    Font tempF = new Font("Courier", Font.PLAIN, 15);
                    final JTextField textField = new JTextField("NOT OBSERVED");
                    textField.setFont(tempF);
                    JPanel panel = new JPanel();
                    panel.setLayout(new FlowLayout(0, 0, 0));
                    panel.add(tempCheckBox);
                    panel.add(textField);
                    mainFrame.add(panel);
                    checkBoxObixObjectMap.put(tempCheckBox, o);
                    tempCheckBox.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent e) {
                            JCheckBox tempCheckBox = (JCheckBox) e.getItem();
                            ObixObject obj = Main.this.getCheckBoxObixObjectMap().get(tempCheckBox);
                            if (e.getStateChange() == ItemEvent.SELECTED) {
                                obj = channel.observe(obj);
                                executor.execute(new ObserveThread(tempCheckBox, textField, obj, mainFrame));
                            } else {
                                obj.getRelation().proactiveCancel();
                                synchronized (obj) {
                                    obj.notify();
                                }
                            }
                        }
                    });
                }
                row++;
            }
        }
        mainFrame.setVisible(true);
    }

    private Map<JCheckBox, ObixObject> getCheckBoxObixObjectMap() {
        return checkBoxObixObjectMap;
    }

    public static void main(String[] args) {
        /*
            Load configuration from config.properties file. For example all oBIX Lobbies
         */
        Configurator configurator = new Configurator();
        List<ObixChannel> channels = configurator.getObixCoapChannels();

        /*
            Create an oBIX CoAP-XML Channel
         */
        ObixChannel channel = new ObixXmlChannelDecorator(channels.get(0));

        /*
            Display Data
         */
        Main main = new Main(channel);
        main.displayObixData();
    }
}
