package model.obix.parameter;

public class StringParameter extends  Parameter{
    private String value;

    public StringParameter(String parameterUri, String value) {
        super(parameterUri);
        this.value = value;
    }

    @Override
    public String getValueAsString() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getValueType() {
        return "&xsd;string";
    }
}
