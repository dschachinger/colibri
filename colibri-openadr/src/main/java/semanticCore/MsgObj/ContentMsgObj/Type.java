package semanticCore.MsgObj.ContentMsgObj;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by georg on 01.07.16.
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Type{
    @XmlAttribute
    String resource;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Type withRessource(String resource){
        this.resource = resource;
        return this;
    }
}
