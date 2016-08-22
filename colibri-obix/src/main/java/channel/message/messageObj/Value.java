package channel.message.messageObj;

import javax.xml.bind.annotation.*;

/**
 * This class represents values used in {@link channel.message.colibriMessage.ColibriMessage}.
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Value {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    @XmlAttribute
    private String datatype;

    @XmlValue
    private String value;

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
