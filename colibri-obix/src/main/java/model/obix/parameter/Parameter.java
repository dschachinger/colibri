package model.obix.parameter;

import channel.message.messageObj.StateDescription;
import channel.message.messageObj.Value;
import service.TimeDurationConverter;
import java.util.*;

/**
 * This class represents a parameter of an {@link model.obix.ObixObject}.
 */
public class Parameter {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    private String parameterUri;
    private String parameterUnit;
    private String parameterType;
    private String valueUri;
    private Value value;
    private List<StateDescription> stateDescriptions;

    /**
     * True, if the parameter is used as a timer to schedule PUT methods.
     */
    private boolean isTimer;

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    /**
     * Creates a parameter which is not a time parameter.
     *
     * @param uri           The URI of the time parameter.
     * @param paramNumber   The number of the parameter.
     */
    private Parameter(String uri, int paramNumber) {
        this.parameterUri = uri + "parameter" + paramNumber;
        this.valueUri = uri + "value" + paramNumber;
        this.value = new Value();
        this.stateDescriptions = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Creates a time parameter. isTimer == false by default.
     *
     * @param uri           The URI of the time parameter.
     * @param paramNumber   The number of the parameter.
     * @param date          The date which is set as timing at the time parameter.
     */
    public Parameter(String uri, int paramNumber, Date date) {
        this(uri, paramNumber);
        this.value.setDatatype("&xsd;dateTime");
        this.isTimer = false;
        this.value.setValue(TimeDurationConverter.date2Ical(date).toString());
    }

    /**
     * Creates a time parameter as a timer.
     *
     * @param uri           The URI of the time parameter.
     * @param paramNumber   The number of the parameter.
     * @param date          The date which is set as timing at the time parameter.
     * @param isTimer       True, if the parameter is used as a timer.
     */
    public Parameter(String uri, int paramNumber, Date date, Boolean isTimer) {
        this(uri, paramNumber);
        this.value.setDatatype("&xsd;dateTime");
        this.value.setValue(TimeDurationConverter.date2Ical(date).toString());
        this.isTimer = isTimer;
    }

    /**
     * Creates a parameter with a long value.
     *
     * @param uri           The URI of the time parameter.
     * @param paramNumber   The number of the parameter.
     * @param i             The value of the parameter.
     */
    public Parameter(String uri, int paramNumber, Long i) {
        this(uri, paramNumber);
        this.value.setDatatype("&xsd;long");
        this.value.setValue(Long.toString(i));
    }

    /**
     * Creates a parameter with a double value.
     *
     * @param uri           The URI of the time parameter.
     * @param paramNumber   The number of the parameter.
     * @param d             The value of the parameter.
     */
    public Parameter(String uri, int paramNumber, Double d) {
        this(uri, paramNumber);
        this.value.setDatatype("&xsd;double");
        this.value.setValue(Double.toString(d));
    }

    /**
     * Creates a parameter with a boolean value.
     *
     * @param uri           The URI of the time parameter.
     * @param paramNumber   The number of the parameter.
     * @param b             The value of the parameter.
     */
    public Parameter(String uri, int paramNumber, Boolean b) {
        this(uri, paramNumber);
        this.value.setDatatype("&xsd;boolean");
        this.value.setValue(Boolean.toString(b));
    }

    /**
     * Creates a parameter with a String value.
     *
     * @param uri           The URI of the time parameter.
     * @param paramNumber   The number of the parameter.
     * @param s             The value of the parameter.
     */
    public Parameter(String uri, int paramNumber, String s) {
        this(uri, paramNumber);
        this.value.setDatatype("&xsd;string");
        this.value.setValue(s);
    }

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public String getParameterUri() {
        return parameterUri;
    }

    public void setParameterUri(String parameterUri) {
        this.parameterUri = parameterUri;
    }

    public void setParameterUnit(String parameterUnit) {
        this.parameterUnit = parameterUnit;
    }

    public String getParameterUnit() {
        return parameterUnit;
    }

    public String getValueAsString() {
        return value.getValue();
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getValueType() {
        return value.getDatatype();
    }

    public String getValueUri() {
        return valueUri;
    }

    public void setValueUri(String valueUri) {
        this.valueUri = valueUri;
    }

    public void setValueAsString(String valueAsString) {
        this.value.setValue(valueAsString);
    }

    public void setValueType(String valueType) {
        this.value.setDatatype(valueType);
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public List<String> getStateUris() {
        List<String> stateUris = new ArrayList<>();
        for (StateDescription description : stateDescriptions) {
            stateUris.add(description.getStateDescriptionUri());
        }
        return stateUris;
    }

    public List<StateDescription> getStateDescriptions() {
        return stateDescriptions;
    }

    public List<StateDescription> addStateDescription(StateDescription desc) {
        stateDescriptions.add(desc);
        return stateDescriptions;
    }

    public boolean isTimer() {
        return isTimer;
    }

    public void setTimer(boolean timer) {
        isTimer = timer;
    }
}
