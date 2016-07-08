package model.obix.parameter;

public class LongParameter extends Parameter {
    private Long value;

    public LongParameter(String parameterUri, Long value) {
        super(parameterUri);
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
        return "&xsd;long";
    }
}
