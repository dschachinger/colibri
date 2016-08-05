package channel.message.messageObj;

import java.util.List;

public class StateDescription {

    private String parameterType;
    private String stateDescriptionUri;
    /**
     * Represents the types of the state, for example AbsoluteState, DiscreteState ect.
     */
    private List<String> stateTypes;
    private Value value;
    private Name name;

    public StateDescription(String stateDescriptionUri, List<String> stateTypes, Value value, Name name) {
        this.parameterType = "&colibri;StateParameter";
        this.stateDescriptionUri = stateDescriptionUri;
        this.stateTypes = stateTypes;
        this.value = value;
        this.name = name;
    }

    public String getStateDescriptionUri() {
        return stateDescriptionUri;
    }

    public void setStateDescriptionUri(String stateDescriptionUri) {
        this.stateDescriptionUri = stateDescriptionUri;
    }

    public List<String> getStateTypes() {
        return stateTypes;
    }

    public void setStateTypes(List<String> stateTypes) {
        this.stateTypes = stateTypes;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }
}
