@XmlSchema(
        namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
        elementFormDefault = XmlNsForm.QUALIFIED,
        attributeFormDefault = XmlNsForm.QUALIFIED,
        xmlns = {
                @XmlNs(prefix="rdf", namespaceURI="http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
                @XmlNs(prefix="xsd", namespaceURI="http://www.w3.org/2001/XMLSchema#"),
                @XmlNs(prefix="colibri", namespaceURI="https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl")
        }
)
package channel.message.messageObj;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;