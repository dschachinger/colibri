package model.obix.parameter;

import channel.message.messageObj.Value;
import service.TimeDurationConverter;

import java.util.Date;

public class Parameter {
    private String parameterUri;
    private String parameterUnit;
    private String parameterType;
    private String valueUri;
    private Value value;

    private Parameter(String uri, int paramNumber) {
        this.parameterUri = uri + "parameter" + paramNumber;
        this.valueUri = uri + "value" + paramNumber;
        this.value = new Value();
    }

    public Parameter(String uri, int paramNumber, Date date) {
        this(uri, paramNumber);
        this.value.setDatatype("&xsd;dateTime");
        this.value.setValue(TimeDurationConverter.date2Ical(date).toString());
    }

    public Parameter(String uri, int paramNumber, Long i) {
        this(uri, paramNumber);
        this.value.setDatatype("&xsd;integer");
        this.value.setValue(Long.toString(i));
    }

    public Parameter(String uri, int paramNumber, Double d) {
        this(uri, paramNumber);
        this.value.setDatatype("&xsd;double");
        this.value.setValue( Double.toString(d));
    }

    public Parameter(String uri, int paramNumber, Boolean b) {
        this(uri, paramNumber);
        this.value.setDatatype("&xsd;boolean");
        this.value.setValue(Boolean.toString(b));
    }

    public Parameter(String uri, int paramNumber, String s) {
        this(uri, paramNumber);
        this.value.setDatatype("&xsd;string");
        this.value.setValue(s);
    }

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

    public Boolean hasBooleanStates() {
        return false;
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
}
