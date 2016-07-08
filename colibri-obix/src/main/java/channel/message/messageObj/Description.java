package channel.message.messageObj;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Description {

    @XmlElement(name = "type", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private List<Type> types;

    @XmlElement(name = "hasParameter", namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/res/colibri.owl#")
    private List<Type> hasParameters;

    @XmlAttribute
    private String about;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/res/colibri.owl#")
    private Address connectorAddress;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/res/colibri.owl#")
    private Address serviceAddress;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/res/colibri.owl#")
    private Address identifier;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/res/colibri.owl#")
    private HasProperty hasDataConfiguration;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/res/colibri.owl#")
    private HasProperty hasTechnologyProtocol;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/res/colibri.owl#")
    private HasProperty hasTechnologyConnector;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/res/colibri.owl#")
    private HasProperty hasUnit;

    public Description() {
        this.types = new ArrayList<>();
        this.hasParameters = new ArrayList<>();
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

    public List<Type> getHasParameters() {
        return types;
    }

    public void addHasParamater(String resource) {
        Type type = new Type();
        type.setResource(resource);
        this.hasParameters.add(type);
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


}
