package model.obix.parameter;

public class LongParameter extends Parameter {
    private Long value;

    public LongParameter(String uri, int paramNumber,  Long value) {
        super(uri, paramNumber);
        this.value = value;
    }

    @Override
    public String getValueAsString() {
        return value.toString();
    }

    public void setValue(Long value) {
        this.value = value;
    }

    @Override
    public String getValueType() {
        return "&xsd;integer";
    }

    @Override
    public Boolean hasBooleanStates() {
        return false;
    }
}
