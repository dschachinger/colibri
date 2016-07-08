package model.obix.parameter;

public class BooleanParameter extends Parameter{
    private Boolean value;

    public BooleanParameter(String parameterUri, Boolean value) {
        super(parameterUri);
        this.value = value;
    }

    @Override
    public String getValueAsString() {
        return value.toString();
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public String getValueType() {
        return "&xsd;boolean";
    }

}
