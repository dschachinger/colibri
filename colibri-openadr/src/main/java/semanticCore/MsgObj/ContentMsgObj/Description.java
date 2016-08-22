package semanticCore.MsgObj.ContentMsgObj;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by georg on 01.07.16.
 * This class is used to describe a semantic core object
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Description {

    @XmlElement(name = "type")
    private List<Type> types;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private Address connectorAddress;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private HasProperty hasDataConfiguration;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private HasProperty hasTechnologyProtocol;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private  List<HasProperty> hasParameter;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private  HasProperty hasUnit;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private  HasProperty hasCurrency;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private List<HasProperty> hasValue;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private  List<HasProperty> hasState;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private  List<HasProperty> hasDataValue;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private Value name;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private Value value;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private Value min;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private Value max;

    @XmlAttribute
    String about;

    public Description(){
        types = new ArrayList<>();
        hasParameter = new ArrayList<>();
        hasValue = new ArrayList<>();
        hasDataValue = new ArrayList<>();
        hasState = new ArrayList<>();
    }

    public HasProperty getHasDataConfiguration() {
        return hasDataConfiguration;
    }

    public void setHasDataConfiguration(HasProperty hasDataConfiguration) {
        this.hasDataConfiguration = hasDataConfiguration;
    }

    public List<HasProperty> getHasState() {
        return hasState;
    }

    public Value getName() {
        return name;
    }

    public void setName(Value name) {
        this.name = name;
    }

    public Value getMin() {
        return min;
    }

    public void setMin(Value min) {
        this.min = min;
    }

    public Value getMax() {
        return max;
    }

    public void setMax(Value max) {
        this.max = max;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public List<HasProperty> getHasValue() {
        return hasValue;
    }

    public List<HasProperty> getHasDataValue() {
        return hasDataValue;
    }

    public void setHasDataValue(List<HasProperty> hasDataValue) {
        this.hasDataValue = hasDataValue;
    }

    public HasProperty getHasCurrency() {
        return hasCurrency;
    }

    public void setHasCurrency(HasProperty hasCurrency) {
        this.hasCurrency = hasCurrency;
    }

    public List<Type> getTypes() {
        return types;
    }

    public void setTypes(List<Type> types) {
        this.types = types;
    }

    public List<HasProperty> getHasParameter() {
        return hasParameter;
    }

    public void setHasParameter(List<HasProperty> hasParameter) {
        this.hasParameter = hasParameter;
    }

    public HasProperty getHasUnit() {
        return hasUnit;
    }

    public void setHasUnit(HasProperty hasUnit) {
        this.hasUnit = hasUnit;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public List<Type> getType() {
        return types;
    }

    public void setType(List<Type> type) {
        this.types = type;
    }

    public Address getConnectorAddress() {
        return connectorAddress;
    }

    public void setConnectorAddress(Address connectorAddress) {
        this.connectorAddress = connectorAddress;
    }

    public HasProperty getHasTechnologyProtocol() {
        return hasTechnologyProtocol;
    }

    public void setHasTechnologyProtocol(HasProperty hasTechnologyProtocol) {
        this.hasTechnologyProtocol = hasTechnologyProtocol;
    }

    public void setHasValue(List<HasProperty> hasValue) {
        this.hasValue = hasValue;
    }

    public void setHasState(List<HasProperty> hasState) {
        this.hasState = hasState;
    }
}
