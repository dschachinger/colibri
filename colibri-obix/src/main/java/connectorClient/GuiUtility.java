package connectorClient;

import channel.Connector;
import channel.commandPattern.CommandFactory;
import channel.message.colibriMessage.ColibriMessage;
import channel.obix.ObixChannel;
import exception.CoapException;
import model.obix.ObixLobby;
import model.obix.ObixObject;
import service.Configurator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GuiUtility {
    private Connector connector;
    private ObixChannel obixChannel;
    private JFrame mainFrame;
    private List<RepresentationRow> representationRows = new ArrayList<RepresentationRow>();
    private ExecutorService executor = Executors.newCachedThreadPool();
    ;
    private ObixLobby lobby;
    private JPanel cards;
    private CommandFactory commandFactory;
    private JLabel titel;
    private JCheckBox registeredColibriChannelCheckBox;
    private UpdateThread updateThread;
    private List<ObserveThread> observeThreads;

    public GuiUtility(Connector connector) {
        this.connector = connector;
        this.obixChannel = connector.getObixChannel();
        this.commandFactory = new CommandFactory();
        this.observeThreads = new ArrayList<>();
    }

    public void runGui() {
        try {
            this.lobby = obixChannel.getLobby(obixChannel.getLobbyUri());
        } catch (CoapException e) {
            System.err.println("Cannot connect to oBIX Lobby of host " + obixChannel.getBaseUri() + " with the CoAP port " + obixChannel.getPort() + ". " +
                    "Maybe the lobby URI in the config.properties file is wrong, " +
                    "or the lobby is not online.");
            return;
        }
        //Create and set up the window.
        mainFrame = new JFrame("ObixConnector at " + obixChannel.getBaseUri() + ": " + obixChannel.getPort());
        mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainFrame.setMinimumSize(new Dimension(500, 500));

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (ObserveThread thread : observeThreads) {
                    thread.stop();
                }
                updateThread.stop();
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
                    if (!connector.getColibriChannel().getRegistered()) {
                        connector.getColibriChannel().send(ColibriMessage.createRegisterMessage(connector));
                    }
                } else {
                    if (connector.getColibriChannel().getRegistered()) {
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
                    representationRows.add(new RepresentationRow(o, chooseCheckBox));
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
                List<ObixObject> chosenObjects = new ArrayList<>();
                for (RepresentationRow r : GuiUtility.this.getRepresentationRows()) {
                    if (r.getChooseCheckbox().isSelected()) {
                        chosenObjects.add(r.getObixObject());
                    }
                }
                //         updateThread.stop();
                representationRows.clear();
                cards.removeAll();
                //TODO: Change to chooseComponents..
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
        return panel;
    }

    private Container chooseParameters(List<ObixObject> chosenComponents) {
        Container pane = new Container();
        pane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        Font titelF = new Font("Courier", Font.BOLD, 30);
        titel = new JLabel("Please choose the appropriate Parameters for the datapoints");
        titel.setFont(titelF);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 10;
        c.gridx = 0;
        c.gridy = 0;
        pane.add(titel, c);

        List<String> parametersList = new Configurator().getAvailableParameterTypes();
        String[] parameterTypes = new String[parametersList.size()];
        parameterTypes = parametersList.toArray(parameterTypes);

        for (ObixObject o : chosenComponents) {
            c.gridy++;
            c.insets = new Insets(30, 10, 0, 0);

            JLabel uriLabel = new JLabel(o.getObixUri());
            uriLabel.setFont(new Font("Courier", Font.ITALIC, 15));
            c.gridx = 0;
            c.gridwidth = 10;
            pane.add(uriLabel, c);

            c.gridwidth = 1;
            c.insets = new Insets(5, 10, 0, 0);

            JLabel parameter1Label = new JLabel("Parameter 1 Type:");
            c.gridy++;
            pane.add(parameter1Label, c);

            JLabel parameter1ObixUnitLabel = new JLabel("oBIX unit: " + o.getObixUnitUri());
            c.gridx++;
            pane.add(parameter1ObixUnitLabel, c);

            JComboBox parameter1comboBox = new JComboBox(parameterTypes);
            c.gridx++;
            pane.add(parameter1comboBox, c);

            JLabel parameter1UnitLabel = new JLabel("Set Parameter 1 Unit:");
            c.gridx++;
            pane.add(parameter1UnitLabel, c);

            JTextField parameter1UnitTextField = new JTextField(o.getParameter1().getParameterUnit());
            parameter1UnitTextField.setPreferredSize(new Dimension(500, 20));
            parameter1UnitTextField.setMinimumSize(new Dimension(500, 20));
            c.gridx++;
            pane.add(parameter1UnitTextField, c);

            c.gridy++;
            c.gridx = 0;
            JLabel parameter2Label = new JLabel("Parameter 2 Type:");
            pane.add(parameter2Label, c);

            JComboBox parameter2comboBox = new JComboBox(parameterTypes);
            c.gridx++;
            c.gridx++;
            pane.add(parameter2comboBox, c);

            JLabel parameter2UnitLabel = new JLabel("Set Parameter 2 Unit: ");
            c.gridx++;
            pane.add(parameter2UnitLabel, c);

            JTextField parameter2UnitTextField = new JTextField(o.getParameter2().getParameterUnit());
            parameter2UnitTextField.setPreferredSize(new Dimension(500, 20));
            parameter2UnitTextField.setMinimumSize(new Dimension(500, 20));
            c.gridx++;
            pane.add(parameter2UnitTextField, c);

            parameter1comboBox.setSelectedItem(o.getParameter1().getParameterType());
            parameter2comboBox.setSelectedItem(o.getParameter2().getParameterType());

            representationRows.add(new RepresentationRow(o, parameter1comboBox, parameter2comboBox));
        }

        JButton acceptButton = new JButton("Accept");
        c.insets = new Insets(50, 0, 0, 0);
        c.gridwidth = 10;
        c.gridx = 0;
        c.gridy++;
        pane.add(acceptButton, c);

        acceptButton.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            }
            public void mousePressed(MouseEvent e) {
            }
            public void mouseReleased(MouseEvent e) {
                List<ObixObject> chosenObjects = new ArrayList<>();
                for (RepresentationRow r : GuiUtility.this.getRepresentationRows()) {
                    r.getObixObject().getParameter1().setParameterType((String) r.getParam1TypeComboBox().getSelectedItem());
                    r.getObixObject().getParameter2().setParameterType((String) r.getParam2TypeComboBox().getSelectedItem());
                     chosenObjects.add(r.getObixObject());
                }
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
        return pane;
    }


    private Container displayObixData(List<ObixObject> chosenComponents) {
        Container pane = new Container();
        pane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 10;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(30, 20, 0, 0);

        c.gridy++;
        pane.add(registeredColibriChannelCheckBox, c);

        JLabel label = new JLabel("oBIX Components");
        Font headerF = new Font("Courier", Font.BOLD, 25);
        label.setFont(headerF);
        c.gridy++;
        pane.add(label, c);

        /*
            Print lobby Data
         */
        for (ObixObject o : chosenComponents) {
            if (connector.getColibriChannel().getRegistered()) {
                connector.getColibriChannel().send(ColibriMessage.createAddServiceMessage(o));
            }

            c.gridy++;
            c.insets = new Insets(30, 10, 0, 0);

            JLabel uriLabel = new JLabel(o.getObixUri());
            uriLabel.setFont(new Font("Courier", Font.ITALIC, 15));
            c.gridx = 0;
            c.gridwidth = 10;
            pane.add(uriLabel, c);

            c.gridwidth = 1;
            c.insets = new Insets(5, 10, 0, 0);

            final JTextField textField = new JTextField("NOT OBSERVED", 20);
            Font tempF = new Font("Courier", Font.PLAIN, 15);
            textField.setFont(tempF);
            c.gridy++;
            pane.add(textField, c);

            JLabel unitLabel = new JLabel();
            if (o.hasUnit()) {
                String unitString = o.getUnit().symbol().get();
                int unitCode = unitString.codePointAt(0);
                if (unitCode == 65533) {
                    unitString = "\u2103";
                }
                unitLabel.setText(unitString);
            }
            c.gridx++;
            pane.add(unitLabel, c);


            final JButton getButton = new JButton("GET");
            c.gridx++;
            pane.add(getButton, c);

            final JCheckBox writableCheckBox = new JCheckBox("Writable");
            writableCheckBox.setSelected(o.getObj().isWritable());
            writableCheckBox.setEnabled(false);
            c.gridx++;
            pane.add(writableCheckBox, c);

            final JCheckBox observeObixCheckBox = new JCheckBox("observe Obix Data");
            observeObixCheckBox.setMargin(new Insets(0, 20, 0, 20));
            c.gridx++;
            pane.add(observeObixCheckBox, c);

            final JCheckBox observedByColibriCheckBox = new JCheckBox("Colibri observes Data");
            observedByColibriCheckBox.setEnabled(false);
            commandFactory.addCommand(() -> observedByColibriCheckBox.setSelected(o.getObservedByColibri()));
            c.gridx++;
            pane.add(observedByColibriCheckBox, c);

            final JCheckBox addServiceCheckbox = new JCheckBox("Service Added to Colibri");
            commandFactory.addCommand(() -> addServiceCheckbox.setSelected(o.getAddedAsService()));
            commandFactory.addCommand(() -> addServiceCheckbox.setEnabled(connector.getColibriChannel().getRegistered()));
            c.gridx++;
            pane.add(addServiceCheckbox, c);

            final JCheckBox observeColibriActionsCheckbox = new JCheckBox("Observe Colibri Actions");
            if (o.getObj().isWritable()) {
                commandFactory.addCommand(() -> observeColibriActionsCheckbox.setEnabled(o.getObservedByColibri()));
                commandFactory.addCommand(() -> observeColibriActionsCheckbox.setSelected(o.getObservesColibriActions()));
            } else {
                observeColibriActionsCheckbox.setEnabled(false);
            }
            c.gridx++;
            pane.add(observeColibriActionsCheckbox, c);

            representationRows.add(new RepresentationRow(uriLabel, observeObixCheckBox, textField, o, writableCheckBox,
                    getButton, addServiceCheckbox, observedByColibriCheckBox, observeColibriActionsCheckbox));
            ObserveThread thread = new ObserveThread(observeObixCheckBox, textField, o, connector);
            executor.execute(thread);
            observeThreads.add(thread);
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
                        obixChannel.observe(object);
                        // commandFactory.addCommand("observe", () -> finalTextF.setText(finalObject.toString()));


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
                        if (!object.getAddedAsService()) {
                            connector.getColibriChannel().send(ColibriMessage.createAddServiceMessage(object));
                        }
                    } else {
                        if (object.getAddedAsService()) {
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
                        if (!object.getObservesColibriActions()) {
                            connector.getColibriChannel().send(ColibriMessage.createObserveServiceMessage(object));
                        }
                    } else {
                        if (object.getObservesColibriActions()) {
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
                    if (o.getObservedByColibri()) {
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
                        object.setValueParameter1(textField.getText());
                        textField.setText("");
                        object = obixChannel.put(object);
                        textField.setText(object.toString());
                    }
                }
            });

        }
        JTextField receivedMessagesTextField = new JTextField("Received Messages");
   //     panel.add(receivedMessagesTextField);
        receivedMessagesTextField.setEnabled(false);
        commandFactory.addCommand(() -> receivedMessagesTextField.setText(connector.getColibriChannel().getLastMessageReceived()));
        return pane;
    }

    private List<RepresentationRow> getRepresentationRows() {
        return representationRows;
    }

}
