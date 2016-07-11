package model.obix.parameter;

public class StringParameter extends  Parameter{
    private String value;

    public StringParameter(String uri, int paramNumber,  String value) {
        super(uri, paramNumber);
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

    @Override
    public Boolean hasBooleanStates() {
        return false;
    }
}
