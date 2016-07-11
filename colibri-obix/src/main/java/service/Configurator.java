package service;

import channel.Connector;
import channel.obix.CoapChannel;
import channel.colibri.ColibriChannel;
import channel.obix.ObixChannel;
import exception.ConfigurationException;

import java.util.*;

/**
 * This class is used to operate on properties files.
 */
public class Configurator {

    private ResourceBundle bundle;

    public Configurator() {
        this.bundle = ResourceBundle.getBundle("config");
    }

    /**
     * This method returns the obix lobbies URIs which are specified in the .properties file of the given bundle.
     *
     * @return The parsed obix lobby URIs.
     * @throws ConfigurationException Is thrown, if there is no oBIX Lobby provided in the parsed .properties file.
     */
    private List<ObixChannel> getObixCoapChannels() throws ConfigurationException {
        List<ObixChannel> oBIXchannels = new ArrayList<ObixChannel>();
        int i = 1;
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.contains("oBIXLobby")) {
                String uri = bundle.getString(key);
                uri = uri.replaceAll("\\s+", "");
                String baseUri = uri.substring(0, uri.lastIndexOf("/"));
                ObixChannel channel;
                if (uri.contains(",")) {
                    Integer port = Integer.parseInt(uri.split(",")[1]);
                    uri = uri.substring(uri.lastIndexOf("/") + 1, uri.lastIndexOf(","));//.substring(0, uri.lastIndexOf(","));
                    channel = new CoapChannel(baseUri, uri, port, getObservedTypes());
                } else {
                    channel = new CoapChannel(baseUri, uri, getObservedTypes());
                }
                oBIXchannels.add(channel);
            }
        }

        if (oBIXchannels.size() == 0) {
            throw new ConfigurationException("No oBIXLobby URI in config file!");
        }

        return oBIXchannels;
    }

    /**
     * This method returns a list of Strings which represent the types which will be observed from oBIX.
     *
     * @return The list of observed types.
     * @throws ConfigurationException Is thrown, if there are no types provided in the parsed .properties file.
     */
    private List<String> getObservedTypes() throws ConfigurationException {
        List<String> observedTypes = new ArrayList<String>();
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.contains("observedTypes")) {
                String[] types = bundle.getString(key).replaceAll("\\s+", "").split(",");
                Collections.addAll(observedTypes, types);
            }
        }

        if (observedTypes.size() == 0) {
            throw new ConfigurationException("No observed types in config file!");
        }

        return observedTypes;
    }

    /**
     * This method returns the URI of the Colibri channel in the .properties file of the given bundle.
     *
     * @return The parsed colibri channel URI.
     * @throws ConfigurationException Is thrown, if there is no colibri channel URI provided in the parsed .properties file.
     */
    private ColibriChannel getColibriChannel() throws ConfigurationException {
        if (bundle.containsKey("colibriChannel")){
            String[] channelParts = bundle.getString("colibriChannel").replaceAll("\\s+", "").split(",");
            return new ColibriChannel("obixConnector", channelParts[0], Integer.parseInt(channelParts[1]));
        }else{
            throw new ConfigurationException("No colibri channel URI in config file!");
        }
    }

    /**
     * This method returns the obix lobbies URIs which are specified in the .properties file of the given bundle.
     *
     * @return The parsed obix lobby URIs.
     * @throws ConfigurationException Is thrown, if there is no oBIX Lobby provided in the parsed .properties file.
     */
    public List<Connector> getConnectors() throws ConfigurationException {
        List<Connector> connectors = new ArrayList<>();
        ColibriChannel colibriChannel = getColibriChannel();
        for(ObixChannel obixChannel : getObixCoapChannels()) {
            connectors.add(new Connector(obixChannel, colibriChannel, getConnectorAddress()));
        }

        if (connectors.size() == 0) {
            throw new ConfigurationException("Cannot parse connectors of config file!");
        }
        return connectors;
    }

    /**
     * This method returns the address of the oBIX Connector in the .properties file of the given bundle.
     *
     * @return The parsed address of the obix Connector.
     * @throws ConfigurationException Is thrown, if there is no obix Connector address provided in the parsed .properties file.
     */
    public String getConnectorAddress() throws ConfigurationException {
        if (bundle.containsKey("connectorAddress")){
            return bundle.getString("connectorAddress");
        }else{
            throw new ConfigurationException("No obix Connector address URI in config file!");
        }
    }
}