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
        Configurator configurator = new Configurator();
        List<ObixChannel> channels = configurator.getObixCoapChannels();
        ObixChannel channel = new ObixXmlChannelDecorator(channels.get(0));
        ObixLobby lobby = channel.getLobby(channel.getLobbyUri());
        System.out.println("Relevant Lobby Objects:");

        ObixObject tempSens = null;
        for(ObixObject o : lobby.getPoints()) {
            System.out.println(o.getObj().getName() + ": " + o.getUri());
            if(o.getUri().equals("VirtualDevices/virtualBrightnessActuator/value")) {
                tempSens = o;
            }
        }


        while(true) {
            System.out.println("Press Enter to get the value of " + "VirtualDevices/virtualBrightnessActuator/value");
            new Scanner(System.in).nextLine();
            ObixObject o = channel.get(tempSens.getUri());
            System.out.println(((Val) o.getObj()).encodeVal());
        }
    }
}
