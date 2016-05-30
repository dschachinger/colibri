package model;

public class ObixObject {

    private String uri;
    private Integer id;
    private String name;
    /**
     * This String represents the oBIX Object.
     * For example, the object can be in XML-Format. In this case, the objectAsString variable will be the XML-String
     * which represents the object.
     * The objectAsString variable can be null.
     */
    private String objectAsString;

    public ObixObject(String uri) {
        this.uri = uri;
    }

    public ObixObject(String uri, Integer id, String name) {
        this.uri = uri;
        this.id = id;
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObjectAsString() {
        return objectAsString;
    }

    public void setObjectAsString(String objectAsString) {
        this.objectAsString = objectAsString;
    }
}