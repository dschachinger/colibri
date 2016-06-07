package service;

import channel.CoapChannel;
import channel.ObixChannel;
import exception.ConfigurationException;
import model.ObixLobby;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * This class is used to operate on properties files.
 */
public class Configurator {

    public List<ObixChannel> getObixCoapChannels() throws ConfigurationException {
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        return getObixCoapChannels(bundle);
    }

    /**
     * This method returns the obix lobbies URIs which are specified in the .properties file of the given path.
     *
     * @param filePath The path of the .properties file.
     * @return The parsed obix lobby URIs.
     * @throws ConfigurationException Is thrown, if there is no oBIX Lobby provided in the parsed .properties file.
     */
    public List<ObixChannel> getObixCoapChannels(String filePath) throws ConfigurationException {
        List<ObixChannel> oBIXchannels = new ArrayList<ObixChannel>();

        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(filePath);
            prop.load(input);

            int i = 1;
            String uri;
            while ((uri = prop.getProperty("oBIXLobby" + i)) != null) {
                String baseUri = uri.substring(0, uri.lastIndexOf("/"));

                ObixChannel channel = new CoapChannel(baseUri, uri);
                oBIXchannels.add(channel);
                i++;
            }

            if (oBIXchannels.size() == 0) {
                throw new ConfigurationException("No oBIXLobby URI in config file!");
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return oBIXchannels;
    }

    /**
     * This method returns the obix lobbies URIs which are specified in the .properties file of the given bundle.
     *
     * @param bundle                    The bundle of the .properties file.
     * @return                          The parsed obix lobby URIs.
     * @throws ConfigurationException   Is thrown, if there is no oBIX Lobby provided in the parsed .properties file.
     */
    public List<ObixChannel> getObixCoapChannels(ResourceBundle bundle) throws ConfigurationException {
        List<ObixChannel> oBIXchannels = new ArrayList<ObixChannel>();
        int i = 1;
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            String uri = bundle.getString(key);
            String baseUri = uri.substring(0, uri.lastIndexOf("/"));

            ObixChannel channel = new CoapChannel(baseUri, uri);
            oBIXchannels.add(channel);
        }

        if (oBIXchannels.size() == 0) {
            throw new ConfigurationException("No oBIXLobby URI in config file!");
        }

        return oBIXchannels;
    }


}
