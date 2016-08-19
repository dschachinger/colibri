package channel.message.messageObj;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents the content of ADD messages used in {@link channel.message.colibriMessage.ColibriMessage}.
 */
@XmlRootElement(name = "RDF" ,namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
@XmlAccessorType(XmlAccessType.FIELD)
public class AddServiceMessageContent {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    @XmlElement(name="Description", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private List<Description> descriptions;

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    public AddServiceMessageContent() {
        this.descriptions = Collections.synchronizedList(new ArrayList<>());;
    }

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public List<Description> getDescriptions() {
        return descriptions;
    }

    public void addDescription(Description description) {
        this.descriptions.add(description);
    }
}
