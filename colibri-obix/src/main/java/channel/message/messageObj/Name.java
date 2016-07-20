package channel.message.messageObj;

import javax.xml.bind.annotation.*;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Name {

    @XmlAttribute
    private String datatype = "&xsd;string";

    @XmlValue
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
