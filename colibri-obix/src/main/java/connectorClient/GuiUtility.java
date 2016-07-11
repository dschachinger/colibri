package connectorClient;

import channel.Connector;
import channel.commandPattern.CommandFactory;
import channel.message.colibriMessage.ColibriMessage;
import channel.obix.ObixChannel;
import exception.CoapException;
import model.obix.ObixLobby;
import model.obix.ObixObject;

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
    private Connector connector;
    private ObixChannel obixChannel;
    private JFrame mainFrame;
    private List<RepresentationRow> representationRows = new ArrayList<RepresentationRow>();
    private ExecutorService executor = Executors.newCachedThreadPool();;
    private ObixLobby lobby;
    private JPanel cards;
    private CommandFactory commandFactory;
    private String test = "";
    private JLabel titel;
    private JCheckBox registeredColibriChannelCheckBox;
    private  UpdateThread updateThread;

    public GuiUtility(Connector connector) {
        this.connector = connector;
        this.obixChannel = connector.getObixChannel();
        this.commandFactory = new CommandFactory();
    }

    public void runGui() {
        try {
            this.lobby = obixChannel.getLobby(obixChannel.getLobbyUri());
        } catch (CoapException e) {
            System.err.println("Cannot connect to oBIX Lobby of host " +  obixChannel.getBaseUri() + " with the CoAP port " + obixChannel.getPort() +". " +
                    "Maybe the lobby URI in the config.properties file is wrong, " +
                    "or the lobby is not online.");
            return;
        }
        //Create and set up the window.
        mainFrame = new JFrame("ObixConnector at " + obixChannel.getBaseUri() + ": " + obixChannel.getPort());
        mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainFrame.setMinimumSize(new Dimension(500, 500));

        mainFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                connector.getColibriChannel().close();
                executor.shutdownNow();
            }
        });


        Container contentPane = mainFrame.getContentPane();

        //Create and set up the content pane.
        this.addComponentToPane(contentPane);

        //Display the window.
        mainFrame.pack();
        updateThread = new UpdateThread(commandFactory);
        executor.execute(updateThread);
        mainFrame.setVisible(true);
    }

    private void addComponentToPane(Container pane) {

        //Create the panel that contains the "cards".
        cards = new JPanel(new CardLayout());
        JScrollPane scrollPane = new JScrollPane(chooseComponents());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(new EmptyBorder(20, 20, 0, 10));
        cards.add(scrollPane);

        pane.add(cards, BorderLayout.CENTER);
    }

    private JPanel chooseComponents() {
        int numRows = lobby.getObixObjects().size() + 2;
        JPanel panel = new JPanel();
        registeredColibriChannelCheckBox = new JCheckBox("IS REGISTERD ON COLIBRI SEMANTIC CORE");
        commandFactory.addCommand(() -> registeredColibriChannelCheckBox.setSelected(connector.getColibriChannel().getRegistered()));
        connector.getColibriChannel().send(ColibriMessage.createRegisterMessage(connector));
        Font regF = new Font("Courier", Font.BOLD, 40);
        registeredColibriChannelCheckBox.setFont(regF);
        registeredColibriChannelCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if(!connector.getColibriChannel().getRegistered()) {
                        connector.getColibriChannel().send(ColibriMessage.createRegisterMessage(connector));
                    }
                } else {
                    if(connector.getColibriChannel().getRegistered()) {
                        connector.getColibriChannel().send(ColibriMessage.createDeregisterMessage(connector));
                    }
                }
            }
        });
        Font titelF = new Font("Courier", Font.BOLD, 30);
        titel = new JLabel("Please choose the components you want to work with");
        titel.setFont(titelF);
        panel.add(registeredColibriChannelCheckBox);
        panel.add(titel);
        panel.setLayout(new GridLayout(numRows + lobby.getObservedObjectsLists().keySet().size(), 1));
        for (String s : lobby.getObservedObjectsLists().keySet()) {
            if (!s.equals("all")) {
                List<ObixObject> objects = lobby.getObservedObjectsLists().get(s);
                JLabel header = new JLabel(s);
                Font headerF = new Font("Courier", Font.BOLD, 25);
                header.setFont(headerF);
                panel.add(header);
                for (ObixObject o : objects) {
                    JCheckBox chooseCheckBox = new JCheckBox(o.getObixUri());
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
       //         updateThread.stop();
                representationRows.clear();
                cards.removeAll();
                JScrollPane scrollPane = new JScrollPane(displayObixData(chosenObjects));
                scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                scrollPane.setBorder(new EmptyBorder(20, 20, 20, 20));
                cards.add(scrollPane);
                //Display the window.
                mainFrame.pack();
            }

            public void mouseEntered(MouseEvent e) {

            }

            public void mouseExited(MouseEvent e) {

            }
        });
   //     updateThread = new UpdateThread(commandFactory);
   //     executor.execute(updateThread);
        return panel;
    }

    private JPanel displayObixData(Map<ObixObject, String> chosenComponents) {
        int numRows = chosenComponents.keySet().size() + chosenComponents.values().size() + 1;
        JPanel panel = new JPanel();
        JLabel label = new JLabel("oBIX Components");
        Font headerF = new Font("Courier", Font.BOLD, 25);
        label.setFont(headerF);
        panel.add(registeredColibriChannelCheckBox);
        panel.add(label);
        panel.setLayout(new GridLayout(numRows, 1));
        /*
            Print lobby Data
         */
        for (ObixObject o : chosenComponents.keySet()) {
            if(connector.getColibriChannel().getRegistered()) {
                connector.getColibriChannel().send(ColibriMessage.createAddServiceMessage(o));
            }
            JLabel uriLabel = new JLabel(o.getObixUri() + ": ");
            uriLabel.setFont(new Font("Courier", Font.ITALIC, 15));
            final JCheckBox observeObixCheckBox = new JCheckBox("observe Obix Data");
            final JCheckBox observedByColibriCheckBox = new JCheckBox("Colibri observes Data");
            final JCheckBox observeColibriActionsCheckbox = new JCheckBox("Observe Colibri Actions");
            final JCheckBox writableCheckBox = new JCheckBox("Writable");
            final JCheckBox addServiceCheckbox = new JCheckBox("Service Added to Colibri");
            observedByColibriCheckBox.setEnabled(false);
            commandFactory.addCommand(() -> addServiceCheckbox.setSelected(o.getAddedAsService()));
            commandFactory.addCommand(() -> addServiceCheckbox.setEnabled(connector.getColibriChannel().getRegistered()));
            commandFactory.addCommand(() -> observedByColibriCheckBox.setSelected(o.getObservedByColibri()));
            if(o.getObj().isWritable()) {
                commandFactory.addCommand(() -> observeColibriActionsCheckbox.setEnabled(o.getObservedByColibri()));
                commandFactory.addCommand(() -> observeColibriActionsCheckbox.setSelected(o.getObservesColibriActions()));
            } else {
                observeColibriActionsCheckbox.setEnabled(false);
            }
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
            writableCheckBox.setEnabled(false);
            observeObixCheckBox.setMargin(new Insets(0, 20, 0, 20));
            final JTextField textField = new JTextField("NOT OBSERVED", 20);
            Font tempF = new Font("Courier", Font.PLAIN, 15);
            textField.setFont(tempF);
            JPanel innerPanel = new JPanel();
            innerPanel.setLayout(new FlowLayout(0, 0, 0));
            innerPanel.add(uriLabel);
            innerPanel.add(textField);
            innerPanel.add(unitLabel);
            innerPanel.add(getButton);
            innerPanel.add(writableCheckBox);
            innerPanel.add(observeObixCheckBox);
            innerPanel.add(observedByColibriCheckBox);
            innerPanel.add(addServiceCheckbox);
            innerPanel.add(observeColibriActionsCheckbox);
            panel.add(innerPanel);
            representationRows.add(new RepresentationRow(uriLabel, observeObixCheckBox, textField, o, writableCheckBox,
                    getButton, addServiceCheckbox, observedByColibriCheckBox, observeColibriActionsCheckbox));
            observeObixCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    ObixObject object = new ObixObject("");
                    JTextField textF = null;
                    for (RepresentationRow r : GuiUtility.this.getRepresentationRows()) {
                        if (r.getObservedCheckBox().equals(observeObixCheckBox)) {
                            object = r.getObixObject();
                            textF = r.getValueTextField();
                        }
                    }
                    JTextField finalTextF = textF;
                    ObixObject finalObject = object;
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        object = obixChannel.observe(object);
                       // commandFactory.addCommand("observe", () -> finalTextF.setText(finalObject.toString()));

                        executor.execute(new ObserveThread(observeObixCheckBox, textF, object, connector.getColibriChannel()));
                    } else {
                        object.getRelation().proactiveCancel();
                       // commandFactory.addCommand("observe", () -> finalTextF.setText("NOT OBSERVED"));
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

                    } else {
                        object.getObj().setWritable(false);
                    }
                    object = obixChannel.put(object);
                    writableCheckBox.setSelected(object.getObj().isWritable());
                }
            });

            addServiceCheckbox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    ObixObject object = new ObixObject("");
                    for (RepresentationRow r : GuiUtility.this.getRepresentationRows()) {
                        if (r.getAddedAsServiceCheckBox().equals(addServiceCheckbox)) {
                            object = r.getObixObject();
                        }
                    }
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        if(!object.getAddedAsService()) {
                            connector.getColibriChannel().send(ColibriMessage.createAddServiceMessage(object));
                        }
                    } else {
                        if(object.getAddedAsService()) {
                            connector.getColibriChannel().send(ColibriMessage.createRemoveServiceMessage(object));
                        }
                    }
                }
            });

            observeColibriActionsCheckbox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    ObixObject object = new ObixObject("");
                    for (RepresentationRow r : GuiUtility.this.getRepresentationRows()) {
                        if (r.getObserveColibriActionsCheckbox().equals(observeColibriActionsCheckbox)) {
                            object = r.getObixObject();
                        }
                    }
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        if(!object.getObservesColibriActions()) {
                            connector.getColibriChannel().send(ColibriMessage.createObserveServiceMessage(object));
                        }
                    } else {
                        if(object.getObservesColibriActions()) {
                            connector.getColibriChannel().send(ColibriMessage.createDetachServiceMessage(object));
                        }
                    }
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
                    object = obixChannel.get(object.getObixUri());
                    textF.setText(object.toString());
                    if(o.getObservedByColibri()) {
                        connector.getColibriChannel().send(ColibriMessage.createPutMessage(object));
                    }
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
                        object.setValue(textField.getText());
                        textField.setText("");
                        object = obixChannel.put(object);
                        textField.setText(object.toString());
                    }
                }
            });

        }
        JTextField receivedMessagesTextField = new JTextField("Received Messages");
        panel.add(receivedMessagesTextField);
        receivedMessagesTextField.setEnabled(false);
        commandFactory.addCommand(() -> receivedMessagesTextField.setText(connector.getColibriChannel().getLastMessageReceived()));
        return panel;
    }

    private List<RepresentationRow> getRepresentationRows() {
        return representationRows;
    }

}
