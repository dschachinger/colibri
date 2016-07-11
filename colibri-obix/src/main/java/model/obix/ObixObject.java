package model.obix;

import model.obix.parameter.*;
import obix.*;
import obix.contracts.Unit;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.coap.CoAP;
import service.Configurator;

import java.util.Date;

public class ObixObject {

    /**
     * This String represents the oBIX object.
     * For example, the object can be in XML-Format. In this case, the objectAsString variable will be the XML-String
     * which represents the object.
     * The objectAsString variable can be null.
     */

    private String objAsString;

    private String obixUri;
    private String colibriBaseUri;
    private Obj obj;
    private CoapObserveRelation relation;
    private Unit unit;
    private String serviceUri;
    private String configurationUri;
    private Parameter parameter1;
    private Parameter parameter2;
    private Boolean addedAsService;
    private Boolean observedByColibri;
    private String dataValueUri;
    private Boolean observesColibriActions;
    private Boolean setByColibri;


    public ObixObject(String uri) {
        this(uri, CoAP.DEFAULT_COAP_PORT);
    }

    public ObixObject(String uri, int obixChannelPort) {
        this.colibriBaseUri = new Configurator().getConnectorAddress() + "/" + obixChannelPort + "/" + uri + "/";
        this.obixUri = uri;
        this.serviceUri = colibriBaseUri + "service";
        this.configurationUri = colibriBaseUri + "configuration";
        this.dataValueUri = colibriBaseUri + "data-value1";
        this.addedAsService = false;
        this.observedByColibri = false;
        this.observesColibriActions = false;
        this.setByColibri = false;
    }

    public String getObixUri() {
        return obixUri;
    }

    public void setObixUri(String uri) {
        this.obixUri = uri;
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
        this.setParameter1();
        this.parameter2 = new DateParameter(colibriBaseUri, 2, new Date());
        this.parameter2.setParameterType("&colibri;TimeParameter");
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
        if (unit != null) {
            if (unit.getName().equals("celsius")) {
                parameter1.setParameterUnit(colibriBaseUri + "degree-celsius");
                parameter1.setParameterType("&colibri;TemperatureParameter");
            } else if (unit.getName().equals("percent")) {
                parameter1.setParameterType("&colibri;EnvironmentalParameter");
            } else if (unit.getName().equals("ppm")) {
                parameter1.setParameterType("&colibri;EnvironmentalParameter");
            } else if (unit.getName().equals("beaufort")) {
                parameter1.setParameterType("&colibri;WindParameter");
            } else if (unit.getName().equals("degree")) {
                parameter1.setParameterType("&colibri;EnvironmentalParameter");
            } else if (unit.getName().equals("hectopascal")) {
                parameter1.setParameterType("&colibri;AirPressureParameter");
            } else if (unit.getName().equals("meter")) {
                parameter1.setParameterType("&colibri;EnvironmentalParameter");
            } else if (unit.getName().equals("millimeter")) {
                parameter1.setParameterType("&colibri;EnvironmentalParameter");
            }
        } else if(parameter1.getParameterType() == null) {
            parameter1.setParameterType("&colibri;DiscreteParameter");
        } else {
            parameter1.setParameterUnit(null);
        }
    }

    public boolean hasUnit() {
        return unit != null;
    }

    public String getConfigurationUri() {
        return configurationUri;
    }

    public void setConfigurationUri(String configurationUri) {
        this.configurationUri = configurationUri;
    }

    @Override
    public String toString() {
        return obj.toString();
    }

    private void setParameter1() {;
        if (getObj().isAbstime()) {
            Abstime abstime = (Abstime) getObj();
            parameter1 = new DateParameter(colibriBaseUri, 1, abstime.getMillis());
            parameter1.setParameterType("&colibri;TimeParameter");
        } else if (getObj().isInt()) {
            Int i = (Int) getObj();
            parameter1 = new LongParameter(colibriBaseUri, 1, i.getInt());
        } else if (getObj().isReltime()) {
            Reltime reltime = (Reltime) getObj();
            parameter1 = new DateParameter(colibriBaseUri, 1, reltime.getMillis());
            parameter1.setParameterType("&colibri;TimeParameter");
        } else if (getObj().isBool()) {
            Bool boo = (Bool) getObj();
            parameter1 = new BooleanParameter(colibriBaseUri, 1, boo.get());
        } else if (getObj().isReal()) {
            Real r = (Real) getObj();
            parameter1 = new DoubleParameter(colibriBaseUri, 1, r.get());
        } else if (getObj().isStr()) {
            Str str = (Str) getObj();
            parameter1 = new StringParameter(colibriBaseUri, 1, str.get());
        } else {
            parameter1 = new StringParameter(colibriBaseUri, 1, obixUri);
        }
    }

    public Parameter getParameter1() {
        return parameter1;
    }

    public void setParameter1(Parameter parameter1) {
        this.parameter1 = parameter1;
    }

    public Parameter getParameter2() {
        return parameter2;
    }

    public void setParameter2(Parameter parameter2) {
        this.parameter2 = parameter2;
    }

    public String getServiceUri() {
        return serviceUri;
    }

    public void setServiceUri(String serviceUri) {
        this.serviceUri = serviceUri;
    }

    public Boolean getAddedAsService() {
        return addedAsService;
    }

    public void setAddedAsService(Boolean addedAsService) {
        this.addedAsService = addedAsService;
    }

    public Boolean getObservedByColibri() {
        return observedByColibri;
    }

    public void setObservedByColibri(Boolean observedByColibri) {
        this.observedByColibri = observedByColibri;
    }

    public String getDataValueUri() {
        return dataValueUri;
    }

    public void setDataValueUri(String dataValueUri) {
        this.dataValueUri = dataValueUri;
    }

    public Boolean getObservesColibriActions() {
        return observesColibriActions;
    }

    public void setObservesColibriActions(Boolean observesColibriActions) {
        this.observesColibriActions = observesColibriActions;
    }

    public void setValueParameter1(String value) {
        if (this.getObj().isInt()) {
            Int i = (Int) this.getObj();
            i.set(Long.parseLong(value));
            this.setObj(i);
        } else if (this.getObj().isBool()) {
            Bool b = (Bool) this.getObj();
            if (value.equals("true")) {
                b.set(true);
            } else {
                b.set(false);
            }
            this.setObj(b);
        } else if (this.getObj().isReal()) {
            Real r = (Real) this.getObj();
            r.set(Double.parseDouble(value));
            this.setObj(r);
        }
    }

    public void setValueParameter2(Date date) {
        new DateParameter(colibriBaseUri, 2, date);
    }

    public String getColibriBaseUri() {
        return colibriBaseUri;
    }

    public Boolean getSetByColibri() {
        return setByColibri;
    }

    public void setSetByColibri(Boolean setByColibri) {
        this.setByColibri = setByColibri;
    }
}
