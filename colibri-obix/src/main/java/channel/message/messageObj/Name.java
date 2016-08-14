package channel.message.messageObj;

import javax.xml.bind.annotation.*;

/**
 * This class represents names used in {@link channel.message.colibriMessage.ColibriMessage}.
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Name {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    @XmlAttribute
    private String datatype = "&xsd;string";

    @XmlValue
    private String name;

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
