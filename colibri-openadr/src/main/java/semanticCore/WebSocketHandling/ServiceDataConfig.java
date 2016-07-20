package semanticCore.WebSocketHandling;

import Utils.EventType;
import openADR.OADRMsgInfo.MsgInfo_OADRDistributeEvent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by georg on 19.07.16.
 */
public class ServiceDataConfig {
    // TODO implement later is this class useful?

    String serviceName;
    String serviceConfig;

    List<Parameter> parameters;

    ServiceDataConfig nestedServiceDataConfig;

    ServiceDataConfig followUpServiceDataConfig;

    String serviceType;

    private ServiceDataConfig(EventType eventType, boolean normalServiceFirstLevel, boolean followUpService){
        serviceType = (followUpService ? "accept" : "") + eventType;

        serviceName = "/"+serviceType+"/"+"Service";
        serviceConfig = "/"+serviceType+"/"+"ServiceConfiguration";

        parameters = new ArrayList<>();

        if(normalServiceFirstLevel){
            addParameter( "ServiceParameter1-1", "&colibri;InformationParameter");
            addParameter("ServiceParameter1-2", "&colibri;TimeParameter");
        }

    }

    private Parameter addParameter(String name, String... type){
        Parameter parameter = new Parameter(serviceType, name, type);
        parameters.add(parameter);
        return parameter;
    }

    public static ServiceDataConfig initPriceService(){
        EventType eventType = EventType.PRICE;

        ServiceDataConfig serviceDataConfig = new ServiceDataConfig(eventType, true, false);
        serviceDataConfig.nestedServiceDataConfig = new ServiceDataConfig(eventType, false, false);

        Parameter parameter;
        parameter = serviceDataConfig.nestedServiceDataConfig.
                addParameter("ServiceParameter2-1", "&colibri;MoneyParameter");
        parameter.setCurrency("http://www.colibri.org/Euro");
        parameter.setUnit("http://www.colibri.org/KiloWattHour");

        serviceDataConfig.nestedServiceDataConfig.
                addParameter("ServiceParameter2-2", "&colibri;IntervalParameter", "&colibri;TimeParameter");

        serviceDataConfig.followUpServiceDataConfig = new ServiceDataConfig(eventType, false, true);

        parameter = serviceDataConfig.followUpServiceDataConfig.
                addParameter("ServiceParameter2-1", "&colibri;MoneyParameter");
        parameter.setCurrency("http://www.colibri.org/Euro");
        parameter.setUnit("http://www.colibri.org/KiloWattHour");

        serviceDataConfig.followUpServiceDataConfig.
                addParameter("ServiceParameter1-1", "&colibri;InformationParameter");

        serviceDataConfig.followUpServiceDataConfig.
                addParameter("ServiceParameter1-2", "&colibri;StateParameter");


        return serviceDataConfig;

    }

    class Parameter{
        String name;
        List<String> types;

        String unit;
        String currency;

        public Parameter(String serviceType, String name, String... type) {
            this.name = "/"+serviceType+"/"+name;
            this.types = new ArrayList<>(Arrays.asList(type));
        }

        public String getName() {
            return name;
        }

        public List<String> getTypes() {
            return types;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }
}
