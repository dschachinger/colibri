package semanticCore.MsgObj.ContentMsgObj;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by georg on 15.07.16.
 * This class is used to describe a service object within the semantic core
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceDescription extends Description{
    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/res/colibri.owl")
    private Address serviceAddress;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/res/colibri.owl")
    private Address identifier;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/res/colibri.owl")
    private  HasProperty isPrecededBy;

    @XmlElement(namespace = "https://raw.githubusercontent.com/dschachinger/colibri/master/res/colibri.owl")
    private HasProperty hasTechnologyConnector;

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

    public HasProperty getIsPrecededBy() {
        return isPrecededBy;
    }

    public void setIsPrecededBy(HasProperty isPrecededBy) {
        this.isPrecededBy = isPrecededBy;
    }

    public HasProperty getHasTechnologyConnector() {
        return hasTechnologyConnector;
    }

    public void setHasTechnologyConnector(HasProperty hasTechnologyConnector) {
        this.hasTechnologyConnector = hasTechnologyConnector;
    }
}
