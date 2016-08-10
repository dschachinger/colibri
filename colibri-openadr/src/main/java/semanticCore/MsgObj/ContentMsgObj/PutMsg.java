package semanticCore.MsgObj.ContentMsgObj;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by georg on 01.07.16.
 * Objects from this class represent the content of a PUT Message
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="RDF")
public class PutMsg {
    @XmlElement(name = "Description", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private List<Description> descriptions;

    public PutMsg() {
        this.descriptions = new ArrayList<>();
    }

    public List<Description> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<Description> descriptions) {
        this.descriptions = descriptions;
    }
}
