package service;
import exception.ConfigurationException;
import model.OBIXLobby;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Configurator {

    public List<OBIXLobby> getOBIXLobbies(String filePath) throws ConfigurationException {
        List<OBIXLobby> oBIXLobbies = new ArrayList<OBIXLobby>();

        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(filePath);
            prop.load(input);

            int i = 1;
            String uri;
            while((uri = prop.getProperty("oBIXLobby" + i)) != null) {
                OBIXLobby obixLobby = new OBIXLobby(uri);
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
