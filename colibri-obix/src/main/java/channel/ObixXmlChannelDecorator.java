package channel;

import model.ObixLobby;
import model.ObixObject;

import static org.eclipse.californium.core.coap.MediaTypeRegistry.APPLICATION_XML;

public class ObixXmlChannelDecorator extends ObixChannelDecorator{
    public ObixXmlChannelDecorator(ObixChannel channel) {
        super(channel);
    }

    @Override
    public ObixLobby getLobby(String uri) {
        ObixLobby lobby = channel.getLobby(uri, APPLICATION_XML);

        //TODO: Decode XML
        return lobby;
    }

    @Override
    public ObixObject get(String uri) {
        ObixObject object = channel.get(uri, APPLICATION_XML);
        //TODO: Decode XML
        return object;
    }

    public static ObixObject decode(String objectAsXml) {
        return null;
    }
}
