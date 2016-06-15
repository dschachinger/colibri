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
    private JPanel namePanel;
    private Map<JCheckBox, ObixObject> checkBoxObixObjectMap = new HashMap<JCheckBox, ObixObject>();
    private ExecutorService executor = Executors.newCachedThreadPool();

    public Main(ObixChannel channel) {
        this.channel = channel;
        prepareGUI();
    }

    private void prepareGUI() {
        mainFrame = new JFrame("");
        mainFrame.setSize(2000, 1500);
        mainFrame.setLayout(new GridLayout(1, 1));
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        mainFrame.add(namePanel);
    }

    private void displayObixData() {

        /*
            Load the relevant oBIX Objects from the (only) oBIX lobby
         */
        ObixLobby lobby = channel.getLobby(channel.getLobbyUri());
        System.out.println("Relevant Lobby Objects:");

        JLabel header = new JLabel("Check Boxes to observe Objects");
        Font headerFont = new Font("Courier", Font.BOLD, 40);
        header.setFont(headerFont);
        namePanel.add(header);

        /*
            Print lobby Data
         */
        for (String s : lobby.getObservedObjectsLists().keySet()) {
            if (!s.equals("all")) {
                JLabel label = new JLabel(s);
                Font font = new Font("Courier", Font.BOLD, 25);
                label.setFont(font);
                namePanel.add(label);
                List<ObixObject> objects = lobby.getObservedObjectsLists().get(s);
                for (ObixObject o : objects) {
                    JCheckBox tempCheckBox = new JCheckBox(o.getUri() + ": " + "NOT OBSERVED");
                    Font tempF = new Font("Courier", Font.PLAIN, 15);
                    tempCheckBox.setFont(tempF);
                    namePanel.add(tempCheckBox);
                    checkBoxObixObjectMap.put(tempCheckBox, o);
                    tempCheckBox.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent e) {
                            JCheckBox tempCheckBox = (JCheckBox) e.getItem();
                            ObixObject obj = Main.this.getCheckBoxObixObjectMap().get(tempCheckBox);
                            if (e.getStateChange() == ItemEvent.SELECTED) {
                                obj = channel.observe(obj);
                                executor.execute(new ObserveThread(tempCheckBox, obj, mainFrame));

                            } else {
                                obj.getRelation().proactiveCancel();
                                synchronized (obj) {
                                    obj.notify();
                                }
                            }
                        }
                    });
                }
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
