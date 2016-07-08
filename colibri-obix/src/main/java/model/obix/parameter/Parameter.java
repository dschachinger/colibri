package model.obix.parameter;

public abstract class Parameter {
    protected String parameterUri;
    protected String parameterUnit;
    private String parameterType;

    public Parameter(String parameterUri) {
        this.parameterUri = parameterUri;
        this.parameterUnit = "Dimensionless";
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
}
