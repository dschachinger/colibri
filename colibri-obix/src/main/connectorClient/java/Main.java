import channel.ObixChannel;
import channel.ObixXmlChannelDecorator;
import model.ObixLobby;
import model.ObixObject;
import obix.Bool;
import obix.Int;
import obix.Real;
import service.Configurator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private ObixChannel channel;
    private JFrame mainFrame;
    private List<RepresentationRow> representationRows = new ArrayList<RepresentationRow>();
    private ExecutorService executor = Executors.newCachedThreadPool();
    private ObixLobby lobby;
    private JPanel cards;

    public Main(ObixChannel channel) {
        this.channel = channel;
        this.lobby = channel.getLobby(channel.getLobbyUri());
        prepareGUI();
    }

    private void prepareGUI() {
        //Create and set up the window.
        mainFrame = new JFrame("ObixConnector");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setMinimumSize(new Dimension(500, 500));

        Container contentPane = mainFrame.getContentPane();
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.add(mainPanel);

        //Create and set up the content pane.
        this.addComponentToPane(mainPanel);

        //Display the window.
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public void addComponentToPane(Container pane) {

        //Create the panel that contains the "cards".
        cards = new JPanel(new CardLayout());
        cards.add(chooseComponents());

        pane.add(cards, BorderLayout.CENTER);
    }

    private JPanel chooseComponents() {
        int numRows = lobby.getObixObjects().size() + 1;
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(numRows + lobby.getObservedObjectsLists().keySet().size(), 1));
        Font titelF = new Font("Courier", Font.BOLD, 30);
        JLabel titel = new JLabel("Please choose the components you want to work with");
        titel.setFont(titelF);
        for (String s : lobby.getObservedObjectsLists().keySet()) {
            if (!s.equals("all")) {
                List<ObixObject> objects = lobby.getObservedObjectsLists().get(s);
                JLabel header = new JLabel(s);
                Font headerF = new Font("Courier", Font.BOLD, 25);
                header.setFont(headerF);
                panel.add(header);
                for (ObixObject o : objects) {
                    JCheckBox chooseCheckBox = new JCheckBox(o.getUri() + ": ");
                    JPanel innerPanel = new JPanel();
                    innerPanel.setLayout(new FlowLayout(0, 0, 0));
                    innerPanel.add(chooseCheckBox);
                    panel.add(innerPanel);
                    representationRows.add(new RepresentationRow(o, chooseCheckBox, s));

                }
            }
        }
        JButton acceptButton = new JButton("Accept");
        panel.add(acceptButton);

        acceptButton.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {

            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
                Map<ObixObject, String> chosenObjects = new HashMap<ObixObject, String>();
                for (RepresentationRow r : Main.this.getRepresentationRows()) {
                    if (r.getChooseCheckbox().isSelected()) {
                        chosenObjects.put(r.getObixObject(), r.getObjectType());
                    }
                }
                representationRows.clear();
                cards.removeAll();
                cards.add(displayObixData(chosenObjects));
                //Display the window.
                mainFrame.pack();
                mainFrame.setVisible(true);
            }

            public void mouseEntered(MouseEvent e) {

            }

            public void mouseExited(MouseEvent e) {

            }
        });
        return panel;
    }

    private JPanel displayObixData(Map<ObixObject, String> chosenComponents) {
        int numRows = chosenComponents.keySet().size() + chosenComponents.values().size() + 1;
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Obix Objects");
        Font headerF = new Font("Courier", Font.BOLD, 25);
        label.setFont(headerF);
        panel.add(label);
        panel.setLayout(new GridLayout(numRows, 1));
        /*
            Print lobby Data
         */
        for (ObixObject o : chosenComponents.keySet()) {
            JLabel uriLabel = new JLabel(o.getUri() + ": ");
            final JCheckBox observeCheckBox = new JCheckBox("observe");
            final JCheckBox writableCheckBox = new JCheckBox("writable");
            JButton getButton = new JButton("GET");
            getButton.setMargin(new Insets(0, 50, 0, 0));
            writableCheckBox.setSelected(o.getObj().isWritable());
            observeCheckBox.setMargin(new Insets(0, 50, 0, 0));
            final JTextField textField = new JTextField("NOT OBSERVED", 20);
            Font tempF = new Font("Courier", Font.PLAIN, 15);
            textField.setFont(tempF);
            JPanel innerPanel = new JPanel();
            innerPanel.setLayout(new FlowLayout(0, 0, 0));
            innerPanel.add(uriLabel);
            innerPanel.add(textField);
            innerPanel.add(writableCheckBox);
            innerPanel.add(observeCheckBox);
            innerPanel.add(getButton);
            panel.add(innerPanel);
            representationRows.add(new RepresentationRow(uriLabel, observeCheckBox, textField, o, writableCheckBox, getButton));
            observeCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    ObixObject object = new ObixObject("");
                    JTextField textF = null;
                    for (RepresentationRow r : Main.this.getRepresentationRows()) {
                        if (r.getObservedCheckBox().equals(observeCheckBox)) {
                            object = r.getObixObject();
                            textF = r.getValueTextField();
                        }
                    }

                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        object = channel.observe(object);
                        executor.execute(new ObserveThread(observeCheckBox, textF, object, mainFrame));
                    } else {
                        object.getRelation().proactiveCancel();
                        synchronized (object) {
                            object.notify();
                        }
                    }
                }
            });

            writableCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    ObixObject object = new ObixObject("");
                    for (RepresentationRow r : Main.this.getRepresentationRows()) {
                        if (r.getWritableCheckbox().equals(writableCheckBox)) {
                            object = r.getObixObject();
                        }
                    }

                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        object.getObj().setWritable(true);
                    } else {
                        object.getObj().setWritable(false);
                    }
                    object = channel.put(object);
                    writableCheckBox.setSelected(object.getObj().isWritable());
                }
            });

            getButton.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {

                }

                public void mousePressed(MouseEvent e) {
                }

                public void mouseReleased(MouseEvent e) {
                    ObixObject object = new ObixObject("");
                    JTextField textF = null;
                    for (RepresentationRow r : Main.this.getRepresentationRows()) {
                        if (r.getObservedCheckBox().equals(observeCheckBox)) {
                            object = r.getObixObject();
                            textF = r.getValueTextField();
                        }
                    }
                    textField.setText("");
                    object = channel.get(object.getUri());
                    textF.setText(object.getObj().toString());
                }

                public void mouseEntered(MouseEvent e) {

                }

                public void mouseExited(MouseEvent e) {

                }
            });

            textField.addKeyListener(new KeyListener() {
                public void keyTyped(KeyEvent e) {
                    //intentionally empty
                }

                public void keyPressed(KeyEvent e) {
                    //intentionally empty
                }

                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        ObixObject object = new ObixObject("");
                        for (RepresentationRow r : Main.this.getRepresentationRows()) {
                            if (r.getValueTextField().equals(textField)) {
                                object = r.getObixObject();
                            }
                        }
                        if (object.getObj().isInt()) {
                            Int i = (Int) object.getObj();
                            i.set(Long.parseLong(textField.getText()));
                            object.setObj(i);
                        } else if (object.getObj().isBool()) {
                            Bool b = (Bool) object.getObj();
                            if (textField.getText().equals("true")) {
                                b.set(true);
                            } else {
                                b.set(false);
                            }
                            object.setObj(b);
                        } else if (object.getObj().isReal()) {
                            Real r = (Real) object.getObj();
                            r.set(Double.parseDouble(textField.getText()));
                            object.setObj(r);
                        }
                        textField.setText("");
                        object = channel.put(object);
                        textField.setText(object.getObj().toString());
                    }
                }
            });

        }
        return panel;
    }

    private List<RepresentationRow> getRepresentationRows() {
        return representationRows;
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
    }
}
