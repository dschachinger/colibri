package channel.message.messageObj;

import javax.xml.bind.annotation.*;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Address {

    @XmlValue
    private String address;

    @XmlAttribute
    private String datatype;

    public Address() {
        this.datatype = "&xsd;string";
    }

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
