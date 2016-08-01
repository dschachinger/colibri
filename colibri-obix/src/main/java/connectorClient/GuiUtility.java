package connectorClient;

import channel.Connector;
import channel.commandPattern.CommandFactory;
import channel.message.colibriMessage.ColibriMessage;
import channel.message.messageObj.Name;
import channel.message.messageObj.StateDescription;
import channel.message.messageObj.Value;
import channel.obix.ObixChannel;
import exception.CoapException;
import model.obix.ObixLobby;
import model.obix.ObixObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.Configurator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class GuiUtility {
    private Connector connector;
    private ObixChannel obixChannel;
    private JFrame mainFrame;
    private List<RepresentationRow> representationRows = new ArrayList<RepresentationRow>();
    private ExecutorService executor;
    private ObixLobby lobby;
    private JPanel cards;
    private CommandFactory commandFactory;
    private JLabel titel;
    private JCheckBox registeredColibriChannelCheckBox;
    private UpdateThread updateThread;
    private List<ObserveThread> observeThreads;
    private List<StateRepresentation> listOfStateRepresentations;

    private static final Logger logger = LoggerFactory.getLogger(GuiUtility.class);

    public GuiUtility(Connector connector) {
        this.connector = connector;
        this.obixChannel = connector.getObixChannel();
        this.commandFactory = new CommandFactory();
        this.observeThreads = Collections.synchronizedList(new ArrayList<>());;
        this.listOfStateRepresentations = Collections.synchronizedList(new ArrayList<>());;
        this.updateThread = new UpdateThread(commandFactory);
        this.executor = connector.getExecutor();
    }

    public void runGui() throws CoapException {
        this.lobby = obixChannel.getLobby(obixChannel.getLobbyUri());
        //Create and set up the window.
        mainFrame = new JFrame("ObixConnector at " + obixChannel.getBaseUri() + ": " + obixChannel.getPort());
        mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        mainFrame.setUndecorated(false);

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });


        Container contentPane = mainFrame.getContentPane();

        //Create and set up the content pane.
        this.addComponentToPane(contentPane);

        //Display the window.
        mainFrame.pack();
        executor.execute(updateThread);
        connector.addRunAndStopAble(updateThread);
        mainFrame.setVisible(true);
    }

    private void addComponentToPane(Container pane) {

        //Create the panel that contains the "cards".
        cards = new JPanel(new CardLayout());
        JScrollPane scrollPane = new JScrollPane(chooseComponents());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        //     scrollPane.setBorder(new EmptyBorder(20, 20, 0, 10));

        cards.add(scrollPane);

        pane.add(cards, BorderLayout.CENTER);
    }

    private JPanel chooseComponents() {
        int numRows = lobby.getObixObjects().size() + 2;
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
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
                List<ObixObject> chosenObjects = Collections.synchronizedList(new ArrayList<>());;
                for (RepresentationRow r : GuiUtility.this.getRepresentationRows()) {
                    if (r.getChooseCheckbox().isSelected()) {
                        chosenObjects.add(r.getObixObject());
                    }
                }
                //         updateThread.stop();
                representationRows.clear();
                cards.removeAll();
                JScrollPane scrollPane = new JScrollPane(chooseParameters(chosenObjects));
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

        List<String> parametersList = Configurator.getInstance().getAvailableParameterTypes();
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
            c.insets = new Insets(10, 10, 0, 0);

            JLabel parameter1Label = new JLabel("Parameter 1 Type:");
            c.gridy++;
            pane.add(parameter1Label, c);

            JLabel parameter1ObixUnitLabel = new JLabel("oBIX unit: " + o.getObixUnitUri());
            c.gridx++;
            pane.add(parameter1ObixUnitLabel, c);

            JComboBox parameter1comboBox = new JComboBox(parameterTypes);
            c.gridx++;
            pane.add(parameter1comboBox, c);

            JButton param1AddStateButton = new JButton("Add State");
            Box vBox1 = Box.createVerticalBox();
            vBox1.setBorder(BorderFactory.createLineBorder(Color.black));

            JLabel parameter1UnitLabel = new JLabel("Set Parameter 1 Unit:");
            c.gridx++;
            pane.add(parameter1UnitLabel, c);

            JTextField parameter1UnitTextField = new JTextField(o.getParameter1().getParameterUnit());
            parameter1UnitTextField.setPreferredSize(new Dimension(500, 20));
            parameter1UnitTextField.setMinimumSize(new Dimension(500, 20));
            c.gridx++;
            pane.add(parameter1UnitTextField, c);

            JLabel parameter1ValueTypeLabel = new JLabel("valueType: " + o.getParameter1().getValueType());
            c.gridx++;
            pane.add(parameter1ValueTypeLabel, c);

            int param1UnitLabelxPosition = c.gridx;
            int param1UnitLabelyPosition = c.gridy;

            for (StateDescription s : o.getParameter1().getStateDescriptions()) {
                JLabel stateNameLabel = new JLabel("State Name: ");
                JTextField stateNameTextfield = new JTextField(20);
                stateNameTextfield.setText(s.getName().getName());
                vBox1.add(stateNameLabel);
                vBox1.add(stateNameTextfield);

                JLabel stateValueLabel = new JLabel("State Value: ");
                JTextField stateValueTextfield = new JTextField(20);
                stateValueTextfield.setText(s.getValue().getValue());
                vBox1.add(stateValueLabel);
                vBox1.add(stateValueTextfield);

                JLabel stateURILabel = new JLabel("State URI: ");
                JTextField stateURITextfield = new JTextField(20);
                stateURITextfield.setText(s.getStateDescriptionUri());
                vBox1.add(stateURILabel);
                vBox1.add(stateURITextfield);

                JButton deleteStateButton = new JButton("Delete State");
                vBox1.add(deleteStateButton);

                JSeparator horizontalLine = new JSeparator(JSeparator.HORIZONTAL);
                vBox1.add(horizontalLine);
                vBox1.add(horizontalLine);

                StateRepresentation stateRepresentation = (new StateRepresentation(param1AddStateButton, deleteStateButton,
                        stateNameLabel, stateNameTextfield,
                        stateValueLabel, stateValueTextfield,
                        stateURILabel, stateURITextfield, horizontalLine, vBox1, o.getParameter1()));

                addDeleteStateListener(stateRepresentation);
                listOfStateRepresentations.add(stateRepresentation);

                pane.revalidate();
                pane.repaint();
            }

            addParameterBoxListener(parameter1comboBox, param1UnitLabelxPosition, param1UnitLabelyPosition,
                    parameter1UnitLabel, parameter1UnitTextField, pane, param1AddStateButton, vBox1);

            param1AddStateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JLabel stateNameLabel = new JLabel("State Name: ");
                    JTextField stateNameTextfield = new JTextField(20);
                    vBox1.add(stateNameLabel);
                    vBox1.add(stateNameTextfield);
                    JLabel stateValueLabel = new JLabel("State Value: ");

                    JTextField stateValueTextfield = new JTextField(20);
                    vBox1.add(stateValueLabel);
                    vBox1.add(stateValueTextfield);

                    JLabel stateURILabel = new JLabel("State URI: ");
                    JTextField stateURITextfield = new JTextField(20);
                    vBox1.add(stateURILabel);
                    vBox1.add(stateURITextfield);

                    JButton deleteStateButton = new JButton("Delete State");
                    vBox1.add(deleteStateButton);

                    JSeparator horizontalLine = new JSeparator(JSeparator.HORIZONTAL);
                    vBox1.add(horizontalLine);
                    vBox1.add(horizontalLine);

                    StateRepresentation stateRepresentation = (new StateRepresentation(param1AddStateButton, deleteStateButton,
                            stateNameLabel, stateNameTextfield,
                            stateValueLabel, stateValueTextfield,
                            stateURILabel, stateURITextfield, horizontalLine, vBox1, o.getParameter1()));

                    addDeleteStateListener(stateRepresentation);
                    listOfStateRepresentations.add(stateRepresentation);
                    pane.revalidate();
                    pane.repaint();
                }
            });

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

            JLabel parameter2ValueTypeLabel = new JLabel("valueType: " + o.getParameter2().getValueType());
            c.gridx++;
            pane.add(parameter2ValueTypeLabel, c);

            JButton param2AddStateButton = new JButton("Add State");
            Box vBox2 = Box.createVerticalBox();
            vBox2.setBorder(BorderFactory.createLineBorder(Color.black));

            int param2UnitLabelxPosition = c.gridx;
            int param2UnitLabelyPosition = c.gridy;

            addParameterBoxListener(parameter2comboBox, param2UnitLabelxPosition, param2UnitLabelyPosition,
                    parameter2UnitLabel, parameter2UnitTextField, pane, param2AddStateButton, vBox2);

            param2AddStateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JLabel stateNameLabel = new JLabel("State Name: ");
                    JTextField stateNameTextfield = new JTextField(20);
                    vBox2.add(stateNameLabel);
                    vBox2.add(stateNameTextfield);
                    JLabel stateValueLabel = new JLabel("State Value: ");

                    JTextField stateValueTextfield = new JTextField(20);
                    vBox2.add(stateValueLabel);
                    vBox2.add(stateValueTextfield);

                    JLabel stateURILabel = new JLabel("State URI: ");
                    JTextField stateURITextfield = new JTextField(20);
                    vBox2.add(stateURILabel);
                    vBox2.add(stateURITextfield);

                    JButton deleteStateButton = new JButton("Delete State");
                    vBox2.add(deleteStateButton);

                    JSeparator horizontalLine = new JSeparator(JSeparator.HORIZONTAL);
                    vBox2.add(horizontalLine);
                    vBox2.add(horizontalLine);

                    StateRepresentation stateRepresentation = (new StateRepresentation(param2AddStateButton, deleteStateButton,
                            stateNameLabel, stateNameTextfield,
                            stateValueLabel, stateValueTextfield,
                            stateURILabel, stateURITextfield, horizontalLine, vBox2, o.getParameter2()));

                    addDeleteStateListener(stateRepresentation);
                    listOfStateRepresentations.add(stateRepresentation);

                    pane.revalidate();
                    pane.repaint();
                }
            });

            parameter1comboBox.setSelectedItem(o.getParameter1().getParameterType());
            parameter2comboBox.setSelectedItem(o.getParameter2().getParameterType());

            representationRows.add(new RepresentationRow(o, parameter1comboBox, parameter2comboBox,
                    parameter1UnitTextField, parameter2UnitTextField));
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
                for (StateRepresentation s : listOfStateRepresentations) {
                    if (s.getStateNameTextField().getText().isEmpty() ||
                            s.getStateUriTextField().getText().isEmpty() ||
                            s.getStateValueTextField().getText().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Each state parameter field must contain a text!");
                        return;
                    }
                    //TODO: Check if states have same URI --> ERROR-Msg!

                    //Save created State
                    ArrayList<String> types = new ArrayList<String>();
                    types.add("&colibri;AbsoluteState");
                    types.add("&colibri;DiscreteState");


                    Value val = new Value();
                    val.setValue(s.getStateValueTextField().getText());
                    val.setDatatype(s.getParameter().getValueType());

                    Name name = new Name();
                    name.setName(s.getStateNameTextField().getText());

                    StateDescription state = new StateDescription(s.getStateUriTextField().getText(),
                            types, val, name, false);

                    s.getParameter().addStateDescription(state);
                }
                List<ObixObject> chosenObjects = Collections.synchronizedList(new ArrayList<>());
                for (RepresentationRow r : GuiUtility.this.getRepresentationRows()) {
                    r.getObixObject().getParameter1().setParameterType((String) r.getParam1TypeComboBox().getSelectedItem());
                    r.getObixObject().getParameter2().setParameterType((String) r.getParam2TypeComboBox().getSelectedItem());
                    chosenObjects.add(r.getObixObject());
                    if (!r.getParam1UnitTextField().getText().isEmpty()) {
                        r.getObixObject().getParameter1().setParameterUnit(r.getParam1UnitTextField().getText());
                    }
                    if (!r.getParam2UnitTextField().getText().isEmpty()) {
                        r.getObixObject().getParameter2().setParameterUnit(r.getParam2UnitTextField().getText());
                    }
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


            final JButton getObixButton = new JButton("GET from oBIX");
            c.gridx++;
            pane.add(getObixButton, c);

            final JButton getColibriButton = new JButton("GET from Colibri");
            c.gridx++;
            pane.add(getColibriButton, c);

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
                commandFactory.addCommand(() -> observeColibriActionsCheckbox.setEnabled(o.getAddedAsService()));
                commandFactory.addCommand(() -> observeColibriActionsCheckbox.setSelected(o.getObservesColibriActions()));
            } else {
                observeColibriActionsCheckbox.setEnabled(false);
            }
            c.gridx++;
            pane.add(observeColibriActionsCheckbox, c);

            representationRows.add(new RepresentationRow(uriLabel, observeObixCheckBox, textField, o, writableCheckBox,
                    getObixButton, getColibriButton, addServiceCheckbox, observedByColibriCheckBox, observeColibriActionsCheckbox));
            ObserveThread thread = new ObserveThread(observeObixCheckBox, textField, o, connector);
            executor.execute(thread);
            connector.addRunAndStopAble(thread);
            observeThreads.add(thread);
            observeObixCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    ObixObject object = new ObixObject("", o.getObixChannelPort());
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
                    ObixObject object = new ObixObject("", o.getObixChannelPort());
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
                    ObixObject object = new ObixObject("", o.getObixChannelPort());
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
                    ObixObject object = new ObixObject("", o.getObixChannelPort());
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

            getObixButton.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                }

                public void mousePressed(MouseEvent e) {
                }

                public void mouseReleased(MouseEvent e) {
                    ObixObject object = new ObixObject("", o.getObixChannelPort());
                    JTextField textF = null;
                    for (RepresentationRow r : GuiUtility.this.getRepresentationRows()) {
                        if (r.getGetObixButton().equals(getObixButton)) {
                            object = r.getObixObject();
                            textF = r.getValueTextField();
                        }
                    }
                    textField.setText("");
                    object = obixChannel.get(object.getObixUri());
                    textF.setText(object.toString());
                    if (o.getObservedByColibri()) {
                        o.getPutMessageToColibriTask().execute(o);
                    }
                }

                public void mouseEntered(MouseEvent e) {
                }

                public void mouseExited(MouseEvent e) {
                }
            });

            getColibriButton.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                }

                public void mousePressed(MouseEvent e) {
                }

                public void mouseReleased(MouseEvent e) {
                    ObixObject object = new ObixObject("", o.getObixChannelPort());
                    JTextField textF = null;
                    for (RepresentationRow r : GuiUtility.this.getRepresentationRows()) {
                        if (r.getGetColibriButton().equals(getColibriButton)) {
                            object = r.getObixObject();
                            textF = r.getValueTextField();
                        }
                    }
                    connector.getColibriChannel().send(ColibriMessage.createGetMessage(object));
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
                        ObixObject object = new ObixObject("", o.getObixChannelPort());
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

        JTextArea receivedMessagesTextArea = new JTextArea("Received Messages");
        receivedMessagesTextArea.setLineWrap(true);
        receivedMessagesTextArea.setWrapStyleWord(true);
        c.gridy++;
        c.gridx = 0;
        c.insets = new Insets(50, 0, 0, 0);
        c.gridwidth = 10;
        pane.add(receivedMessagesTextArea, c);
        receivedMessagesTextArea.setEnabled(false);
        commandFactory.addCommand(() -> receivedMessagesTextArea.setText(connector.getColibriChannel().getLastMessageReceived()));

        c.gridy++;
        c.gridwidth = 1;

        JLabel sendMessageLabel = new JLabel("Send Query Message to Colibri Semantic Core:");
        pane.add(sendMessageLabel, c);

        c.gridy++;
        c.gridwidth = 10;
        JTextArea sendMessageArea = new JTextArea("");
        pane.add(sendMessageArea, c);

        c.gridy++;
        c.gridwidth = 1;
        JButton sendMessageButton = new JButton("Send Query Message");
        pane.add(sendMessageButton, c);
        commandFactory.addCommand(() -> sendMessageButton.setEnabled(connector.getColibriChannel().getRegistered()));

        sendMessageButton.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
                connector.getColibriChannel().send(ColibriMessage.createQueryMessage(sendMessageArea.getText()));
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });

        return pane;
    }

    private List<RepresentationRow> getRepresentationRows() {
        return representationRows;
    }

    public void close() {
        observeThreads.forEach(ObserveThread::stop);
        updateThread.stop();
        connector.setRunning(false);
        connector.stop();
    }

    private void addDeleteStateListener(StateRepresentation s) {
        s.getDeleteButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                s.getContainerVBox().remove(s.getStateUriLabel());
                s.getContainerVBox().remove(s.getStateUriTextField());
                s.getContainerVBox().remove(s.getStateNameLabel());
                s.getContainerVBox().remove(s.getStateNameTextField());
                s.getContainerVBox().remove(s.getStateValueLabel());
                s.getContainerVBox().remove(s.getStateValueTextField());
                s.getContainerVBox().remove(s.getDeleteButton());
                s.getContainerVBox().remove(s.getHorizontalLine());
                s.getContainerVBox().revalidate();
                s.getContainerVBox().revalidate();
                listOfStateRepresentations.remove(s);
            }
        });
    }

    private void addParameterBoxListener(JComboBox parameterComboBox, int paramUnitLabelxPosition,
                                         int paramUnitLabelyPosition, JLabel parameterUnitLabel,
                                         JTextField parameterUnitTextField, Container pane,
                                         JButton paramAddStateButton, Box vbox) {
        parameterComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                GridBagConstraints cTemp = new GridBagConstraints();
                cTemp.gridx = paramUnitLabelxPosition + 1;
                cTemp.gridy = paramUnitLabelyPosition;
                if (parameterComboBox.getSelectedItem().equals("&colibri;StateParameter")) {
                    parameterUnitLabel.setEnabled(false);
                    parameterUnitTextField.setEnabled(false);
                    //stateDescriptionPanel.add(vBox1);
                    pane.add(paramAddStateButton, cTemp);
                    vbox.repaint();
                    vbox.revalidate();
                    cTemp.gridx++;
                    pane.add(vbox, cTemp);
                } else {
                    pane.remove(paramAddStateButton);

                    Iterator<StateRepresentation> iter = listOfStateRepresentations.iterator();
                    while (iter.hasNext()) {
                        StateRepresentation s = iter.next();
                        if (paramAddStateButton.equals(s.getAddButon())) {
                            vbox.removeAll();
                            pane.remove(s.getContainerVBox());
                            iter.remove();
                        }
                    }
                    parameterUnitLabel.setEnabled(true);
                    parameterUnitTextField.setEnabled(true);
                }
                pane.revalidate();
                pane.repaint();
            }
        });
    }
}
