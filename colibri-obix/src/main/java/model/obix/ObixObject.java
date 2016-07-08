package model.obix;

import model.obix.parameter.*;
import obix.*;
import obix.contracts.Unit;
import org.eclipse.californium.core.CoapObserveRelation;
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

    private String uri;

    private Obj obj;

    private CoapObserveRelation relation;

    private Unit unit;

    private String serviceUri;
    private String configurationUri;
    private Parameter parameter1;
    private Parameter parameter2;
    private Boolean addedAsService;
    private Boolean observedByColibri;

    public ObixObject(String uri) {
        this.uri = uri;
        this.serviceUri = new Configurator().getConnectorAddress() + "/" + uri + "/service";
        this.configurationUri = new Configurator().getConnectorAddress() + "/" + uri + "/configuration";
        this.addedAsService = false;
        this.observedByColibri = false;
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
        this.setParameter1();
        this.parameter2 = new DateParameter(new Configurator().getConnectorAddress() + "/" + uri + "/parameter2", new Date());
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
                parameter1.setParameterUnit("&colibri;degree-celsius");
                parameter1.setParameterType("&colibri;TemperatureParameter");
            } else if (unit.getName().equals("percent")) {
                parameter1.setParameterType("&colibri;PercentParameter");
            } else if (unit.getName().equals("ppm")) {
                parameter1.setParameterType("&colibri;PpmParameter");
            } else if (unit.getName().equals("beaufort")) {
                parameter1.setParameterType("&colibri;BeaufortParameter");
            } else if (unit.getName().equals("ppm")) {
                parameter1.setParameterType("&colibri;ppmParameter");
            } else if (unit.getName().equals("degree")) {
                parameter1.setParameterType("&colibri;DegreeParameter");
            } else if (unit.getName().equals("hectopascal")) {
                parameter1.setParameterType("&colibri;HectopascalParameter");
            } else if (unit.getName().equals("meter")) {
                parameter1.setParameterType("&colibri;MeterParameter");
            } else if (unit.getName().equals("millimeter")) {
                parameter1.setParameterType("&colibri;MillimeterParameter");
            }
        } else if(parameter1.getParameterType() == null) {
            parameter1.setParameterType("&colibri;Typeless");
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

    private void setParameter1() {
        String parameter1Uri = new Configurator().getConnectorAddress() + "/" + uri + "/parameter1";
        if (getObj().isAbstime()) {
            Abstime abstime = (Abstime) getObj();
            parameter1 = new DateParameter(parameter1Uri, abstime.getMillis());
            parameter1.setParameterType("&colibri;TimeParameter");
        } else if (getObj().isInt()) {
            Int i = (Int) getObj();
            parameter1 = new LongParameter(parameter1Uri, i.getInt());
        } else if (getObj().isReltime()) {
            Reltime reltime = (Reltime) getObj();
            parameter1 = new DateParameter(parameter1Uri, reltime.getMillis());
            parameter1.setParameterType("&colibri;TimeParameter");
        } else if (getObj().isBool()) {
            Bool boo = (Bool) getObj();
            parameter1 = new BooleanParameter(parameter1Uri, boo.get());
        } else if (getObj().isReal()) {
            Real r = (Real) getObj();
            parameter1 = new DoubleParameter(parameter1Uri, r.get());
        } else if (getObj().isStr()) {
            Str str = (Str) getObj();
            parameter1 = new StringParameter(parameter1Uri, str.get());
        } else {
            parameter1 = new StringParameter(parameter1Uri, parameter1Uri);
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
}
