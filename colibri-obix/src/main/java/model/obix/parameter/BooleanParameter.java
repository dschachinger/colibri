package model.obix.parameter;

public class BooleanParameter extends Parameter{
    private Boolean value;

    public BooleanParameter(String uri, int paramNumber, Boolean value) {
        super(uri, paramNumber);
        this.value = value;
        this.setParameterType("&colibri;StateParameter");
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

    @Override
    public Boolean hasBooleanStates() {
        return true;
    }
}
