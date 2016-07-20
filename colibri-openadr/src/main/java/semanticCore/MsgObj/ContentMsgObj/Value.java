package semanticCore.MsgObj.ContentMsgObj;

import javax.xml.bind.annotation.*;

/**
 * Created by georg on 08.07.16.
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Value {
    @XmlAttribute(namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    String datatype;

    @XmlValue
    String value;

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

    public Value withValue(String value){
        this.value = value;
        return this;
    }

    public Value withDatatype(String datatype){
        this.datatype = datatype;
        return this;
    }
}
