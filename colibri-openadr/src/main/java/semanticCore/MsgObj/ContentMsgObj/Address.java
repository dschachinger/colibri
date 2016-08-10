package semanticCore.MsgObj.ContentMsgObj;

import javax.xml.bind.annotation.*;

/**
 * Created by georg on 01.07.16.
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Address {
    @XmlAttribute
    String dataType;

    @XmlValue
    private String address;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String connectorAddress) {
        this.address = connectorAddress;
    }
}
