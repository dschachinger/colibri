package model.obix.parameter;

public class DoubleParameter extends Parameter {
    private Double value;

    public DoubleParameter(String parameterUri, Double value) {
        super(parameterUri);
        this.value = value;
    }

    @Override
    public String getValueAsString() {
        return value.toString();
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public String getValueType() {
        return "&xsd;double";
    }
}
