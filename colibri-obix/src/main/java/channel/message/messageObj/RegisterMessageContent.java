package channel.message.messageObj;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the content of REG messages used in {@link channel.message.colibriMessage.ColibriMessage}.
 */
@XmlRootElement(name = "RDF" ,namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
@XmlAccessorType(XmlAccessType.FIELD)
public class RegisterMessageContent {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    @XmlElement(name="Description", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private Description description;

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public Description getDescription() {
        return description;
        }

    public void setDescription(Description description) {
        this.description = description;
        }


}
