package semanticCore.MsgObj.ContentMsgObj;

import javax.xml.bind.annotation.*;

/**
 * Created by georg on 01.07.16.
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Address {
    @XmlAttribute
    String datatype;

    @XmlValue
    private String address;

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
