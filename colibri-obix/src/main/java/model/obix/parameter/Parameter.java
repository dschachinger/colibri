package model.obix.parameter;

public abstract class Parameter {
    private String parameterUri;
    private String parameterUnit;
    private String parameterType;
    private String valueUri;

    public Parameter(String uri, int paramNumber) {
        this.parameterUri = uri + "parameter" + paramNumber;
        this.valueUri = uri + "value" + paramNumber;
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

    public abstract String getValueAsString();

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public abstract String getValueType();

    public String getValueUri() {
        return valueUri;
    }

    public void setValueUri(String valueUri) {
        this.valueUri = valueUri;
    }

    public abstract Boolean hasBooleanStates();
}
