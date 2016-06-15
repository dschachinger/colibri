package channel;

import model.ObixLobby;
import model.ObixObject;
import obix.Obj;
import obix.Uri;
import obix.Val;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

import java.util.List;

import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

/**
 * This is a plain channel which sends data over CoAP. In order to sufficiently parse the sent and received data,
 * the channel has to be decorated by at least one decorator channel, for example the {@link ObixXmlChannelDecorator}.
 */
public class CoapChannel extends ObixChannel {

    public CoapChannel(String baseUri, String lobbyUri, List<String> observedTypes) {
        super(baseUri, lobbyUri, observedTypes);
    }

    public ObixLobby getLobby(String uri)  {
        return super.getLobby(uri);
    }

    public ObixObject get(String uri) {
        return super.get(uri);
    }

    public ObixObject observe(String uri) {
        return super.observe(uri);
    }

    public ObixLobby getLobby(String uri, int mediaType) {
        ObixLobby lobby = new ObixLobby(uri, getObservedTypes());
        lobby.setLobbyAsString(this.getAsString(uri, mediaType));
        return lobby;
    }

    public ObixObject get(String uri, int mediaType) {
        ObixObject object = new ObixObject(uri);
        object.setObjectAsString(this.getAsString(uri, mediaType));
        return object;
    }

    public ObixObject observe(String uri, int mediaType) {
        ObixObject object = new ObixObject(uri);
        object.setObjectAsString(this.observeAsString(uri, mediaType));
        return object;
    }

    private String getAsString(String uri, int mediaType){
        CoapClient coapClient;
            coapClient = new CoapClient(CoapChannel.normalizeUri(uri, this.baseUri));
            return coapClient.get(mediaType).getResponseText();
    }

    private String observeAsString(String uri, int mediaType){
        CoapClient coapClient;

        coapClient = new CoapClient(CoapChannel.normalizeUri(uri, this.baseUri));

        String content = "";

        CoapObserveRelation relation = coapClient.observe(
                new CoapHandler() {
                    public void onLoad(CoapResponse response) {
                        String content = response.getResponseText();
                        Val val = (Val) ObixXmlChannelDecorator.decode(content);
                        System.out.println("OBSERVE Notification: "
                               + "Display Name = " + val.getDisplayName() + ", "
                               + " Value = " + val.encodeVal());
                    }

                    public void onError() {
                        System.err.println("OBSERVING FAILED ()");
                    }
                }, mediaType);
        return content;
    }

    public String normalizeUri(String uri) {
        return CoapChannel.normalizeUri(uri, this.baseUri);
    }

    public static String normalizeUri(String uri, String baseUri) {
        return "coap://" + ObixChannel.normalizeUri(uri, baseUri);
    }

}
