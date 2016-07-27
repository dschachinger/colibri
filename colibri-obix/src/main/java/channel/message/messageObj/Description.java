package channel.message.messageObj;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Description {

    @XmlElement(name = "type", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private List<Type> types;

    @XmlElement(name = "hasParameter", namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private List<HasProperty> hasParameters;

    @XmlAttribute
    private String about;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private Address connectorAddress;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private Address serviceAddress;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private Address identifier;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private HasProperty hasDataConfiguration;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private HasProperty hasTechnologyProtocol;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private HasProperty hasTechnologyConnector;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private HasProperty hasUnit;

    @XmlElement(name = "hasDataValue", namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private List<HasProperty> hasDataValues;

    @XmlElement(name = "hasValue", namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private List<HasProperty> hasValues;

    @XmlElement(name = "hasState", namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private List<HasProperty> hasStates;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private Value value;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
    private Name name;

    public Description() {
        this.types = Collections.synchronizedList(new ArrayList<>());
        this.hasParameters = Collections.synchronizedList(new ArrayList<>());
        this.hasValues = Collections.synchronizedList(new ArrayList<>());
        this.hasStates = Collections.synchronizedList(new ArrayList<>());
        this.hasDataValues = Collections.synchronizedList(new ArrayList<>());
    }

    public HasProperty getHasTechnologyProtocol() {
        return hasTechnologyProtocol;
    }

    public void setHasTechnologyProtocol(String hasTechnologyProtocol) {
        this.hasTechnologyProtocol = new HasProperty();
        this.hasTechnologyProtocol.setResource(hasTechnologyProtocol);
    }

    public HasProperty getHasDataConfiguration() {
        return hasDataConfiguration;
    }

    public void setHasDataConfiguration(String hasDataConfiguration) {
        this.hasDataConfiguration = new HasProperty();
        this.hasDataConfiguration.setResource(hasDataConfiguration);
    }

    public HasProperty getHasUnit() {
        return hasUnit;
    }

    public void setHasUnit(String hasUnit) {
        this.hasUnit = new HasProperty();
        this.hasUnit.setResource(hasUnit);
    }

    public HasProperty getHasTechnologyConnector() {
        return hasTechnologyConnector;
    }

    public void setHasTechnologyConnector(String hasTechnologyConnector) {
        this.hasTechnologyConnector = new HasProperty();
        this.hasTechnologyConnector.setResource(hasTechnologyConnector);
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public List<Type> getTypes() {
            return types;
    }

    public void addType(String resource) {
        Type type = new Type();
        type.setResource(resource);
        this.types.add(type);
    }

    public List<HasProperty> getHasParameters() {
        return hasParameters;
    }

    public void addHasParamater(String resource) {
        HasProperty hasParameter = new HasProperty();
        hasParameter.setResource(resource);
        this.hasParameters.add(hasParameter);
    }

    public void setConnectorAddress(Address connectorAddress) {
        this.connectorAddress = connectorAddress;
    }

    public Address getConnectorAddress() {
        return connectorAddress;
    }

    public Address getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(Address serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public Address getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Address identifier) {
        this.identifier = identifier;
    }

    public List<HasProperty> getHasDataValue() {
        return hasDataValues;
    }

    public void addHasDataValue(String resource) {
        HasProperty hasDataValue = new HasProperty();
        hasDataValue.setResource(resource);
        this.hasDataValues.add(hasDataValue);
    }

    public List<HasProperty> getHasValues() {
        return hasValues;
    }

    public void addHasValue(String resource) {
        HasProperty hasValue = new HasProperty();
        hasValue.setResource(resource);
        this.hasValues.add(hasValue);
    }

    public List<HasProperty> getHasStates() {
        return hasStates;
    }

    public void addHasStates(String resource) {
        HasProperty hasState = new HasProperty();
        hasState.setResource(resource);
        this.hasStates.add(hasState);
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
}
