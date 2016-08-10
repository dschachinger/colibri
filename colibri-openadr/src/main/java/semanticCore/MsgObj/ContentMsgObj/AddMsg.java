package semanticCore.MsgObj.ContentMsgObj;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by georg on 01.07.16.
 * Objects from this class represents the content of an ADD Message
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="RDF")
public class AddMsg {
    @XmlElement(name = "Description", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private List<Description> descriptions;

    // This describes a service on top level
    @XmlElement(name = "Description", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private ServiceDescription normalServiceDescriptions;

    // This describes an accept service on top level
    @XmlElement(name = "Description", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private ServiceDescription acceptServiceDescriptions;

    public AddMsg() {
        this.descriptions = new ArrayList<>();
    }

    public List<Description> getDescriptions() {
        return descriptions;
    }

    public ServiceDescription getNormalServiceDescriptions() {
        return normalServiceDescriptions;
    }

    public void setNormalServiceDescriptions(ServiceDescription serviceDescriptions) {
        this.normalServiceDescriptions = serviceDescriptions;
    }

    public ServiceDescription getAcceptServiceDescriptions() {
        return acceptServiceDescriptions;
    }

    public void setAcceptServiceDescriptions(ServiceDescription acceptServiceDescriptions) {
        this.acceptServiceDescriptions = acceptServiceDescriptions;
    }
}
