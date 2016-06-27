import channel.ObixChannel;
import exception.CoapException;
import model.ObixLobby;
import model.ObixObject;
import obix.Bool;
import obix.Int;
import obix.Real;

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

public class GuiUtility {
    private ObixChannel channel;
    private JFrame mainFrame;
    private List<RepresentationRow> representationRows = new ArrayList<RepresentationRow>();
    private ExecutorService executor = Executors.newCachedThreadPool();;
    private ObixLobby lobby;
    private JPanel cards;

    public GuiUtility(ObixChannel channel) {
        this.channel = channel;
    }

    public void runGui() {
        try {
            this.lobby = channel.getLobby(channel.getLobbyUri());
        } catch (CoapException e) {
            System.err.println("Cannot connect to oBIX Lobby of host " +  channel.getBaseUri() + " with the CoAP port " + channel.getPort() +". " +
                    "Maybe the lobby URI in the config.properties file is wrong, " +
                    "or the lobby is not online.");
            return;
        }
        //Create and set up the window.
        mainFrame = new JFrame("ObixConnector at " + channel.getBaseUri() + ": " + channel.getPort());
        mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainFrame.setMinimumSize(new Dimension(500, 500));

        mainFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                executor.shutdownNow();
            }
        });


        mainFrame.setVisible(true);
        Container contentPane = mainFrame.getContentPane();

        //Create and set up the content pane.
        this.addComponentToPane(contentPane);

        //Display the window.
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    private void addComponentToPane(Container pane) {

        //Create the panel that contains the "cards".
        cards = new JPanel(new CardLayout());
        JScrollPane scrollPane = new JScrollPane(chooseComponents());
        scrollPane.setBorder(new EmptyBorder(20, 20, 0, 10));
        cards.add(scrollPane);
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
                    JCheckBox chooseCheckBox = new JCheckBox(o.getUri());
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
                for (RepresentationRow r : GuiUtility.this.getRepresentationRows()) {
                    if (r.getChooseCheckbox().isSelected()) {
                        chosenObjects.put(r.getObixObject(), r.getObjectType());
                    }
                }
                representationRows.clear();
                cards.removeAll();
                JScrollPane scrollPane = new JScrollPane(displayObixData(chosenObjects));
                scrollPane.setBorder(new EmptyBorder(20, 20, 20, 20));
                cards.add(scrollPane);
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
        JLabel label = new JLabel("oBIX Components");
        Font headerF = new Font("Courier", Font.BOLD, 25);
        label.setFont(headerF);
        panel.add(label);
        panel.setLayout(new GridLayout(numRows, 1));
        /*
            Print lobby Data
         */
        for (ObixObject o : chosenComponents.keySet()) {
            JLabel uriLabel = new JLabel(o.getUri() + ": ");
            uriLabel.setFont(new Font("Courier", Font.ITALIC, 20));
            final JCheckBox observeCheckBox = new JCheckBox("observe");
            final JCheckBox writableCheckBox = new JCheckBox("writable");
            final JButton getButton = new JButton("GET");
            JLabel unitLabel = new JLabel();
            if(o.hasUnit()) {
                String unitString = o.getUnit().symbol().get();
                int unitCode = unitString.codePointAt(0);
                if(unitCode == 65533) {
                    unitString = "\u2103";
                }
                unitLabel.setText(unitString);
            }
            writableCheckBox.setSelected(o.getObj().isWritable());
            observeCheckBox.setMargin(new Insets(0, 20, 0, 20));
            final JTextField textField = new JTextField("NOT OBSERVED", 20);
            Font tempF = new Font("Courier", Font.PLAIN, 15);
            textField.setFont(tempF);
            JPanel innerPanel = new JPanel();
            innerPanel.setLayout(new FlowLayout(0, 0, 0));
            innerPanel.add(uriLabel);
            innerPanel.add(textField);
            innerPanel.add(unitLabel);
            innerPanel.add(writableCheckBox);
            innerPanel.add(observeCheckBox);
            innerPanel.add(getButton);
            panel.add(innerPanel);
            representationRows.add(new RepresentationRow(uriLabel, observeCheckBox, textField, o, writableCheckBox, getButton));
            observeCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    ObixObject object = new ObixObject("");
                    JTextField textF = null;
                    for (RepresentationRow r : GuiUtility.this.getRepresentationRows()) {
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
                    for (RepresentationRow r : GuiUtility.this.getRepresentationRows()) {
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
                    for (RepresentationRow r : GuiUtility.this.getRepresentationRows()) {
                        if (r.getGetButton().equals(getButton)) {
                            object = r.getObixObject();
                            textF = r.getValueTextField();
                        }
                    }
                    textField.setText("");
                    object = channel.get(object.getUri());
                    textF.setText(object.toString());
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
                        for (RepresentationRow r : GuiUtility.this.getRepresentationRows()) {
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
                        textField.setText(object.toString());
                    }
                }
            });

        }
        return panel;
    }

    private List<RepresentationRow> getRepresentationRows() {
        return representationRows;
    }
}
