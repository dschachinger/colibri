package channel.message.messageObj;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "RDF" ,namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
@XmlAccessorType(XmlAccessType.FIELD)
public class PutMessageContent {

    @XmlElement(name="Description", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private List<Description> descriptions;

    public PutMessageContent() {
        this.descriptions = new ArrayList<>();
    }

    public List<Description> getDescriptions() {
        return descriptions;
    }

    public void addDescription(Description description) {
        this.descriptions.add(description);
    }

    public String getServiceUri() {
        if(descriptions.get(0) != null && descriptions.get(0).getAbout() != null) {
            return descriptions.get(0).getAbout();
        } else {
            return "No Service Uri";
        }
    }

    public String getDataValueUri() {
        if(descriptions.get(1) != null && descriptions.get(1).getAbout() != null) {
            return descriptions.get(1).getAbout();
        } else {
            return "No Data Value Uri";
        }
    }

    public String getValue1Uri() {
        if(descriptions.get(2) != null && descriptions.get(2).getAbout() != null) {
            return descriptions.get(2).getAbout();
        } else {
            return "No Value Uri";
        }
    }

    public String getValue2Uri() {
        if(descriptions.get(3) != null && descriptions.get(3).getAbout() != null) {
            return descriptions.get(3).getAbout();
        } else {
            return "No Value Uri";
        }
    }

    public String getValue1HasParameterUri() {
        if(descriptions.get(2) != null && descriptions.get(2).getHasParameters() != null &&
                descriptions.get(2).getHasParameters().get(0) != null) {
            System.out.println(descriptions.get(2).getHasParameters().get(0).getResource());
            return descriptions.get(2).getHasParameters().get(0).getResource();
        } else {
            return "No Parameter Uri";
        }
    }


    public String getValue2HasParameterUri() {
        if(descriptions.get(3) != null && descriptions.get(3).getHasParameters() != null &&
                descriptions.get(3).getHasParameters().get(0) != null) {
            return descriptions.get(3).getHasParameters().get(0).getResource();
        } else {
            return "No Parameter Uri";
        }
    }

    public Value getValue1() {
        if(descriptions.get(2) != null && descriptions.get(2).getValue() != null) {
            return descriptions.get(2).getValue();
        } else {
            return new Value();
        }
    }

    public Value getValue2() {
        if(descriptions.get(3) != null && descriptions.get(3).getValue() != null) {
            return descriptions.get(3).getValue();
        } else {
            return new Value();
        }
    }
}
