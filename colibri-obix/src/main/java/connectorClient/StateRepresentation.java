package connectorClient;

import model.obix.parameter.Parameter;

import javax.swing.*;

public class StateRepresentation {
    private JButton addButon;
    private JLabel stateNameLabel;
    private JTextField stateNameTextField;
    private JLabel stateValueLabel;
    private JTextField stateValueTextField;
    private JLabel stateUriLabel;
    private JTextField stateUriTextField;
    private Parameter parameter;
    private Box containerVBox;

    public StateRepresentation(JButton addButon, JLabel stateNameLabel, JTextField stateNameTextField,
                               JLabel stateValueLabel, JTextField stateValueTextField,
                               JLabel stateUriLabel, JTextField stateUriTextField, Box containerVBox, Parameter parameter) {
        this.addButon = addButon;
        this.stateNameLabel = stateNameLabel;
        this.stateNameTextField = stateNameTextField;
        this.stateValueLabel = stateValueLabel;
        this.stateValueTextField = stateValueTextField;
        this.stateUriLabel = stateUriLabel;
        this.stateUriTextField = stateUriTextField;
        this.containerVBox = containerVBox;
        this.parameter = parameter;
    }

    public JButton getAddButon() {
        return addButon;
    }

    public void setAddButon(JButton addButon) {
        this.addButon = addButon;
    }

    public JTextField getStateNameTextField() {
        return stateNameTextField;
    }

    public void setStateNameTextField(JTextField stateNameTextField) {
        this.stateNameTextField = stateNameTextField;
    }

    public JTextField getStateValueTextField() {
        return stateValueTextField;
    }

    public void setStateValueTextField(JTextField stateValueTextField) {
        this.stateValueTextField = stateValueTextField;
    }

    public JTextField getStateUriTextField() {
        return stateUriTextField;
    }

    public void setStateUriTextField(JTextField stateUriTextField) {
        this.stateUriTextField = stateUriTextField;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public JLabel getStateNameLabel() {
        return stateNameLabel;
    }

    public void setStateNameLabel(JLabel stateNameLabel) {
        this.stateNameLabel = stateNameLabel;
    }

    public JLabel getStateValueLabel() {
        return stateValueLabel;
    }

    public void setStateValueLabel(JLabel stateValueLabel) {
        this.stateValueLabel = stateValueLabel;
    }

    public JLabel getStateUriLabel() {
        return stateUriLabel;
    }

    public void setStateUriLabel(JLabel stateUriLabel) {
        this.stateUriLabel = stateUriLabel;
    }

    public Box getContainerVBox() {
        return containerVBox;
    }

    public void setContainerVBox(Box containerVBox) {
        this.containerVBox = containerVBox;
    }
}
