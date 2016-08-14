package channel.message.messageObj;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents types used in {@link channel.message.colibriMessage.ColibriMessage}.
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Type {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    @XmlAttribute
    private String resource;

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
