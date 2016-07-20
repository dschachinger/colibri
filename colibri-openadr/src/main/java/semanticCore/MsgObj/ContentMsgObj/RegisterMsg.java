package semanticCore.MsgObj.ContentMsgObj;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by georg on 01.07.16.
 * Objects from this class represents the content of an ADD Message
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="RDF")
public class RegisterMsg {
    @XmlElement(name = "Description", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private Description description;

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }
}
