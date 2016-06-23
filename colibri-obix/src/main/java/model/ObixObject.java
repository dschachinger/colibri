package model;

import obix.*;
import obix.contracts.Unit;
import org.eclipse.californium.core.CoapObserveRelation;

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

    private CoapObserveRelation relation;

    private Unit unit;

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

    public CoapObserveRelation getRelation() {
        return relation;
    }

    public void setRelation(CoapObserveRelation relation) {
        this.relation = relation;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public boolean hasUnit() {
        return unit != null;
    }

    @Override
    public String toString() {
        return obj.toString();
    }
}
