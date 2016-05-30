package channel;

import exception.CoapCommunicationException;
import model.ObixLobby;
import model.ObixObject;
import org.eclipse.californium.core.CoapClient;

import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

/**
 * This is a plain channel which sends data over CoAP. In order to sufficiently parse the sent and received data,
 * the channel has to be decorated by at least one decorator channel, for example the {@link ObixXmlChannelDecorator}.
 */
public class CoapChannel implements ObixChannel {

    public ObixLobby getLobby(String uri) throws CoapCommunicationException {
        return this.getLobby(uri, APPLICATION_XML);
    }

    public ObixObject get(String uri) throws CoapCommunicationException {
        return this.get(uri, APPLICATION_XML);
    }

    public ObixLobby getLobby(String uri, int mediaType) throws CoapCommunicationException {
        ObixLobby lobby = new ObixLobby(uri);
        lobby.setLobbyAsString(this.getAsString(uri, mediaType));
        return lobby;
    }

    public ObixObject get(String uri, int mediaType) throws CoapCommunicationException {
        ObixObject object = new ObixObject(uri);
        object.setObjectAsString(this.getAsString(uri, mediaType));
        return object;
    }

    private String getAsString(String uri, int mediaType) throws CoapCommunicationException {
        CoapClient coapClient;
        try {
            coapClient = new CoapClient("coap://" + uri);
            return coapClient.get(mediaType).getResponseText();
        } catch (IllegalArgumentException e) {
            throw new CoapCommunicationException(e);
        }
    }
}
