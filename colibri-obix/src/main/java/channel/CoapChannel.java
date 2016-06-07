package channel;

import model.ObixLobby;
import model.ObixObject;
import obix.Obj;
import obix.Uri;
import org.eclipse.californium.core.CoapClient;

import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

/**
 * This is a plain channel which sends data over CoAP. In order to sufficiently parse the sent and received data,
 * the channel has to be decorated by at least one decorator channel, for example the {@link ObixXmlChannelDecorator}.
 */
public class CoapChannel extends ObixChannel {

    public CoapChannel(String baseUri, String lobbyUri) {
        super(baseUri, lobbyUri);
    }

    public ObixLobby getLobby(String uri)  {
        return super.getLobby(uri);
    }

    public ObixObject get(String uri) {
        return super.get(uri);
    }

    public ObixLobby getLobby(String uri, int mediaType) {
        ObixLobby lobby = new ObixLobby(uri);
        lobby.setLobbyAsString(this.getAsString(uri, mediaType));
        return lobby;
    }

    public ObixObject get(String uri, int mediaType) {
        ObixObject object = new ObixObject(uri);
        object.setObjectAsString(this.getAsString(uri, mediaType));
        return object;
    }

    private String getAsString(String uri, int mediaType){
        CoapClient coapClient;
            coapClient = new CoapClient(CoapChannel.normalizeUri(uri, this.baseUri));
            return coapClient.get(mediaType).getResponseText();
    }

    public String normalizeUri(String uri) {
        return CoapChannel.normalizeUri(uri, this.baseUri);
    }

    public static String normalizeUri(String uri, String baseUri) {
        return "coap://" + ObixChannel.normalizeUri(uri, baseUri);
    }
}
