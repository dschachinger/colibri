import channel.CoapChannel;
import channel.ObixChannel;
import channel.ObixXmlChannelDecorator;
import model.ObixLobby;
import model.ObixObject;
import obix.Obj;
import obix.Val;
import service.Configurator;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        /*
            Load configuration from config.properties file. For example all oBIX Lobbies
         */
        Configurator configurator = new Configurator();
        List<ObixChannel> channels = configurator.getObixCoapChannels();

        /*
            Create an oBIX CoAP-XML Channel
         */
        ObixChannel channel = new ObixXmlChannelDecorator(channels.get(0));

        /*
            Load the relevant oBIX Objects from the (only) oBIX lobby
         */
        ObixLobby lobby = channel.getLobby(channel.getLobbyUri());
        System.out.println("Relevant Lobby Objects:");

        /*
            Print lobby Data, store 1 sensor for further processes
         */
        ObixObject tempSens = null;
        for(String s : lobby.getObservedObjectsLists().keySet()) {
            List<ObixObject> objects =  lobby.getObservedObjectsLists().get(s);
            System.out.println("List of " + s + ":");
            for(ObixObject o : objects) {
                if(o.getUri().equals("VirtualDevices/virtualBrightnessActuator/value")) {
                    tempSens = o;
                }
                System.out.println(o.getUri());
            }

        }



        /*
            Observe data of the stored sensor (only proof of concept!)
         */
        channel.observe(tempSens.getUri());
        while(true);

    }
}
