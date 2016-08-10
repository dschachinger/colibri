package semanticCore.MsgObj.ContentMsgObj;

import javax.xml.bind.annotation.*;

/**
 * Created by georg on 08.07.16.
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Value {
    @XmlAttribute(namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    String dataType;

    @XmlValue
    String value;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
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

    public Value withDataType(String dataType){
        this.dataType = dataType;
        return this;
    }
}
