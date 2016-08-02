package semanticCore.WebSocketHandling;

import Utils.EventType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by georg on 19.07.16.
 */
public class ServiceDataConfig {
    EventType eventType;

    String serviceName;
    String serviceConfig;

    List<Parameter> parameters;

    ServiceDataConfig nestedServiceDataConfig;

    ServiceDataConfig followUpServiceDataConfig;

    String serviceBaseURL;

    String serviceType;

    private ServiceDataConfig(EventType eventType, String serviceBaseURL, boolean normalServiceFirstLevel, boolean followUpService){
        this.eventType = eventType;
        serviceType = (followUpService ? "accept" : "") + eventType;
        this.serviceBaseURL = serviceBaseURL + "/"+serviceType+"/";

        String postlude = !normalServiceFirstLevel && !followUpService ? "2" : "";


        serviceName = "Service"+postlude;
        serviceConfig = "ServiceConfiguration"+postlude;

        parameters = new ArrayList<>();

        if(normalServiceFirstLevel){
            addParameter( "ServiceParameter1-1", "&colibri;InformationParameter");
            addParameter("ServiceParameter1-2", "&colibri;TimeParameter");
        }
    }

    public EventType getEventType() {
        return eventType;
    }

    private Parameter addParameter(String name, String... type){
        Parameter parameter = new Parameter(serviceType, name, type);
        parameters.add(parameter);
        return parameter;
    }

    public static ServiceDataConfig initService(EventType eventType, String baseURL){
        ServiceDataConfig serviceDataConfig = new ServiceDataConfig(eventType, baseURL, true, false);
        serviceDataConfig.nestedServiceDataConfig = new ServiceDataConfig(eventType, baseURL, false, false);

        Parameter parameter;
        switch (eventType){
            case PRICE: addPriceServiceParameter(serviceDataConfig);
                break;
            case LOAD: addLoadServiceParameter(serviceDataConfig);
                break;
        }

        serviceDataConfig.nestedServiceDataConfig.
                addParameter("ServiceParameter2-2", "&colibri;IntervalParameter", "&colibri;TimeParameter");

        serviceDataConfig.followUpServiceDataConfig = new ServiceDataConfig(eventType, baseURL, false, true);

        serviceDataConfig.followUpServiceDataConfig.
                addParameter("ServiceParameter1-1", "&colibri;InformationParameter");

        parameter = serviceDataConfig.followUpServiceDataConfig.
                addParameter("ServiceParameter1-2", "&colibri;StateParameter");
        parameter.getStates().add("http://www.colibri.org/openADRConnector/OptIn");
        parameter.getStates().add("http://www.colibri.org/openADRConnector/OptOut");

        return serviceDataConfig;
    }

    public static void addPriceServiceParameter(ServiceDataConfig serviceDataConfig){
        Parameter parameter;
        parameter = serviceDataConfig.nestedServiceDataConfig.
                addParameter("ServiceParameter2-1", "&colibri;MoneyParameter");
        parameter.setCurrency("http://www.colibri.org/Euro");
        parameter.setUnit("http://www.colibri.org/KiloWattHour");
    }

    public static void addLoadServiceParameter(ServiceDataConfig serviceDataConfig){
        Parameter parameter;
        parameter = serviceDataConfig.nestedServiceDataConfig.
                addParameter("ServiceParameter2-1", "&colibri;EnergyParameter");
        parameter.setUnit("http://www.colibri.org/KiloWattHour");
    }

    public String getServiceName() {
        return serviceBaseURL + serviceName;
    }

    public String getServiceConfig() {
        return serviceBaseURL + serviceConfig;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public ServiceDataConfig getNestedServiceDataConfig() {
        return nestedServiceDataConfig;
    }

    public ServiceDataConfig getFollowUpServiceDataConfig() {
        return followUpServiceDataConfig;
    }

    public String getServiceType() {
        return serviceType;
    }

    public class Parameter{
        String name;
        List<String> types;

        String unit;
        String currency;
        List<String> states;

        public Parameter(String serviceType, String name, String... type) {
            this.name = name;
            this.types = new ArrayList<>(Arrays.asList(type));
            this.states = new ArrayList<>();
        }

        public String getName() {
            return serviceBaseURL + name;
        }

        public List<String> getStates() {
            return states;
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
