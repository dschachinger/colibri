package connectorClient;

import channel.Connector;
import channel.colibri.ColibriChannel;
import channel.obix.ObixXmlChannelDecorator;
import service.Configurator;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        /*
            Load configuration from config.properties file. For example all oBIX Lobbies
         */
        Configurator configurator = new Configurator();
        List<Connector> connectors = configurator.getConnectors();

        for(Connector connector : connectors) {
            ColibriChannel colibriChannel = connector.getColibriChannel();
            connector.setObixChannel(new ObixXmlChannelDecorator(connector.getObixChannel()));
            try {
                colibriChannel.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
            final GuiUtility guiUtility = new GuiUtility(connector);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    guiUtility.runGui();
                }
            });
        }
    }
}
