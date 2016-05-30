package service;
import exception.ConfigurationException;
import model.ObixLobby;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This class is used to operate on properties files.
 */
public class Configurator {

    /**
     *  This method returns the obix lobbies URIs which are specified in the .properties file of the given path.
     *
     * @param filePath                  The path of the .properties file.
     * @return                          The parsed obix lobby URIs.
     * @throws ConfigurationException   Is thrown, if there is no oBIX Lobby provided in the parsed .properties file.
     */
    public List<ObixLobby> getObixLobbiesURIs(String filePath) throws ConfigurationException {
        List<ObixLobby> oBIXLobbies = new ArrayList<ObixLobby>();

        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(filePath);
            prop.load(input);

            int i = 1;
            String uri;
            while((uri = prop.getProperty("oBIXLobby" + i)) != null) {
                ObixLobby obixLobby = new ObixLobby(uri);
                oBIXLobbies.add(obixLobby);
                i++;
            }

            if(oBIXLobbies.size() == 0) {
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
        return oBIXLobbies;
    }
}
