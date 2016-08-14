package channel.message.messageObj;

import javax.xml.bind.annotation.*;

/**
 * This class represents addresses used in {@link channel.message.colibriMessage.ColibriMessage}.
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Address {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    @XmlValue
    private String address;

    @XmlAttribute
    private String datatype;

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    public Address() {
        this.datatype = "&xsd;string";
    }

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String connectorAddress) {
        this.address = connectorAddress;
    }
}
