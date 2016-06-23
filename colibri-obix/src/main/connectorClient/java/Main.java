import channel.ObixChannel;
import channel.ObixXmlChannelDecorator;
import service.Configurator;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        /*
            Load configuration from config.properties file. For example all oBIX Lobbies
         */
        Configurator configurator = new Configurator();
        List<ObixChannel> channels = configurator.getObixCoapChannels();

        for(ObixChannel channel : channels) {
            final GuiUtility guiUtility = new GuiUtility(new ObixXmlChannelDecorator(channel));
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    guiUtility.runGui();
                }
            });
        }
    }
}
