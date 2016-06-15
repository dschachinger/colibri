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
import org.eclipse.californium.core.network.CoapEndpoint;

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

    public ObixObject observe(ObixObject obj) {
        return super.observe(obj);
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

    public ObixObject observe(ObixObject obj, int mediaType) {
        getObservedObjects().put(obj.getUri(), obj);
        if(APPLICATION_XML == mediaType) {
            obj.setObjectAsString(this.observeAsXml(obj, mediaType));
        }
        return obj;
    }

    private String getAsString(String uri, int mediaType){
        CoapClient coapClient;
            coapClient = new CoapClient(CoapChannel.normalizeUri(uri, this.baseUri));
            return coapClient.get(mediaType).getResponseText();
    }

    private String observeAsXml(ObixObject obj, int mediaType){
        CoapClient coapClient;
        final String uri = obj.getUri();
        coapClient = new CoapClient(CoapChannel.normalizeUri(obj.getUri(), this.baseUri));
        String content = "";
        CoapObserveRelation relation = coapClient.observeAndWait(
                new CoapHandler() {
                    public void onLoad(CoapResponse response) {
                        String content = response.getResponseText();
                        ObixObject o = getObservedObjects().get(uri);
                        o.setObj(ObixXmlChannelDecorator.decode(content));
                        getObservedObjects().remove(uri);
                        getObservedObjects().put(uri, o);
                        synchronized (o) {
                            o.notify();
                        }
                    }
                    public void onError() {
                        System.err.println("OBSERVING FAILED");
                    }

                }, mediaType);

        synchronized (obj) {
            try {
                obj.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        obj.setRelation(relation);
        return content;
    }

    public String normalizeUri(String uri) {
        return CoapChannel.normalizeUri(uri, this.baseUri);
    }

    public static String normalizeUri(String uri, String baseUri) {
        return "coap://" + ObixChannel.normalizeUri(uri, baseUri);
    }

}
