package model.obix;

import channel.colibri.PutMessageToColibriTask;
import channel.message.messageObj.Name;
import channel.message.messageObj.StateDescription;
import channel.message.messageObj.Value;
import model.obix.parameter.Parameter;
import obix.*;
import obix.contracts.Unit;
import org.eclipse.californium.core.CoapObserveRelation;
import service.Configurator;

import java.util.ArrayList;
import java.util.Collections;
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
    private String connectorUri;
    private String obixUnitUri;
    private PutMessageToColibriTask putMessageToColibriTask;
    private int obixChannelPort;

    public ObixObject(String uri, int obixChannelPort) {
        this.connectorUri = Configurator.getInstance().getConnectorAddress();
        this.obixChannelPort = obixChannelPort;
        this.colibriBaseUri = connectorUri + "/" + obixChannelPort + "/" + uri + "/";
        this.obixUri = uri;
        this.serviceUri = colibriBaseUri + "service";
        this.configurationUri = colibriBaseUri + "configuration";
        this.dataValueUri = colibriBaseUri + "data-value";
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
        this.obj = obj;
        this.setParameter1();
        this.parameter2 = new Parameter(colibriBaseUri, 2, new Date(), true);
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
                parameter1.setParameterUnit(connectorUri + "/" + "degree-celsius");
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
        if (getObj().isAbstime()) {
            Abstime abstime = (Abstime) getObj();
            parameter1 = new Parameter(colibriBaseUri, 1, new Date(abstime.get()));
            parameter1.setParameterType("&colibri;TimeParameter");
        } else if (getObj().isInt()) {
            Int i = (Int) getObj();
            parameter1 = new Parameter(colibriBaseUri, 1, i.getInt());
        } else if (getObj().isReltime()) {
            Reltime reltime = (Reltime) getObj();
            parameter1 = new Parameter(colibriBaseUri, 1, new Date(reltime.get()));
            parameter1.setParameterType("&colibri;TimeParameter");
        } else if (getObj().isBool()) {
            Bool boo = (Bool) getObj();
            parameter1 = new Parameter(colibriBaseUri, 1, boo.get());
            createBooleanStateDescriptions(parameter1);
        } else if (getObj().isReal()) {
            Real r = (Real) getObj();
            parameter1 = new Parameter(colibriBaseUri, 1, r.get());
        } else if (getObj().isStr()) {
            Str str = (Str) getObj();
            parameter1 = new Parameter(colibriBaseUri, 1, str.get());
        } else {
            parameter1 = new Parameter(colibriBaseUri, 1, obixUri);
        }
        if(parameter1.getParameterType() == null) {
            parameter1.setParameterType("&colibri;DiscreteParameter");
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

    public void setValueParameter1(Value value) {
        if (value.getDatatype().contains("long")) {
            this.getObj().setInt(Long.parseLong(value.getValue()));
        } else if (value.getDatatype().contains("double")) {
            this.getObj().setReal(Double.parseDouble(value.getValue()));
        } else if (value.getDatatype().contains("boolean")) {
            this.getObj().setBool(Boolean.parseBoolean(value.getValue()));
        } else if (value.getDatatype().contains("string")) {
            this.getObj().setStr(value.getValue());
        }
        parameter1.setValue(value);
    }

    public void setValueParameter1(String value) {
        if (parameter1.getValueType().equals("&xsd;long")) {
            this.getObj().setInt(Integer.parseInt(value));
        } else if (parameter1.getValueType().equals("&xsd;double")) {
            this.getObj().setReal(Double.parseDouble(value));
        } else if (parameter1.getValueType().equals("&xsd;boolean")) {
            this.getObj().setBool(Boolean.parseBoolean(value));
        } else if (parameter1.getValueType().equals("&xsd;string")) {
            this.getObj().setStr(value);
        }
        parameter1.setValueAsString(value);
    }

    public void setValueParameter2(Value value) {
        parameter2.setValue(value);
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

    public String getConnectorUri() {
        return connectorUri;
    }

    public String getObixUnitUri() {
        return obixUnitUri;
    }

    public void setObixUnitUri(String obixUnitUri) {
        this.obixUnitUri = obixUnitUri;
    }

    public PutMessageToColibriTask getPutMessageToColibriTask() {
        return putMessageToColibriTask;
    }

    public void setPutMessageToColibriTask(PutMessageToColibriTask putMessageToColibriTask) {
        this.putMessageToColibriTask = putMessageToColibriTask;
    }

    public void createBooleanStateDescriptions(Parameter param) {
        java.util.List<String> types = Collections.synchronizedList(new ArrayList<>());
        types.add("&colibri;AbsoluteState");
        types.add("&colibri;DiscreteState");

        //true state
        String stateTrueUri = connectorUri + "/" + "trueState";
        Value valTrue = new Value();
        valTrue.setDatatype("&xsd;boolean");
        valTrue.setValue("true");
        Name nameTrue = new Name();
        nameTrue.setName("on");
        param.addStateDescription(new StateDescription(stateTrueUri, types, valTrue, nameTrue, true));

        //false state
        String stateFalseUri = connectorUri + "/" + "falseState";
        Value valFalse= new Value();
        valFalse.setDatatype("&xsd;boolean");
        valFalse.setValue("false");
        Name nameFalse = new Name();
        nameFalse.setName("off");
        param.addStateDescription(new StateDescription(stateFalseUri, types, valFalse, nameFalse, true));

        //link states to parameter 1
        param.setParameterType("&colibri;StateParameter");
        param.addStateUri(stateTrueUri);
        param.addStateUri(stateFalseUri);
    }

    public int getObixChannelPort() {
        return obixChannelPort;
    }

    public void setObixChannelPort(int obixChannelPort) {
        this.obixChannelPort = obixChannelPort;
    }
}
