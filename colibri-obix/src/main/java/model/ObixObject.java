package model;

import obix.Obj;
import obix.Val;
import obix.contracts.Unit;

public class ObixObject {

    /**
     * This String represents the oBIX object.
     * For example, the object can be in XML-Format. In this case, the objectAsString variable will be the XML-String
     * which represents the object.
     * The objectAsString variable can be null.
     */

    private String objAsString;

    private String uri;

    private Obj obj;

    public ObixObject(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getObjectAsString() {
        return objAsString;
    }

    public void setObjectAsString(String objAsString) {
        this.objAsString = objAsString;
    }

    public Obj getObj() {
        return obj;
    }

    public void setObj(Obj obj) {
        this.obj = obj;
    }

}
