package channel.obix;

import exception.CoapException;
import model.obix.ObixLobby;
import model.obix.ObixObject;
import org.apache.http.HttpStatus;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.eclipse.californium.core.coap.MediaTypeRegistry.APPLICATION_XML;

/**
 * This is a plain channel which sends data over CoAP. In order to sufficiently parse the sent and received data,
 * the channel has to be decorated by at least one decorator channel, for example the {@link ObixXmlChannelDecorator}.
 */
public class CoapChannel extends ObixChannel {

    private final String SCHEME = "coap";
    private CoapClient coapClient;
    private static final Logger logger = LoggerFactory.getLogger(CoapChannel.class);

    public CoapChannel(String baseUri, String lobbyUri, List<String> observedTypes) {
        super(baseUri, lobbyUri, observedTypes);
    }

    public CoapChannel(String baseUri, String lobbyUri, Integer port, List<String> observedTypes) {
        super(baseUri, lobbyUri, port, observedTypes);
    }

    public ObixLobby getLobby(String uri) {
        return super.getLobby(uri);
    }

    public ObixObject get(String uri) {
        return super.get(uri);
    }

    public ObixObject put(ObixObject obj) {
        return super.put(obj);
    }

    public ObixObject observe(ObixObject obj) {
        return super.observe(obj);
    }

    public ObixLobby getLobby(String uri, int mediaType) throws CoapException {
        ObixLobby lobby = new ObixLobby(uri, getObservedTypes());
        lobby.setLobbyAsString(this.getAsString(lobbyUri, mediaType));
        return lobby;
    }

    public ObixObject get(String uri, int mediaType) {
        ObixObject object = new ObixObject(uri, port);
        object.setObjectAsString(this.getAsString(uri, mediaType));
        return object;
    }

    public ObixObject put(ObixObject obj, int mediaType) {
        coapClient = getCoapClientWithUri(obj.getObixUri());
        obj.setObjectAsString(coapClient.put(obj.getObjectAsString(), mediaType).getResponseText());
        return obj;
    }

    public ObixObject observe(ObixObject obj, int mediaType) {
        getObservedObjects().put(obj.getObixUri(), obj);
        if (APPLICATION_XML == mediaType) {
            obj.setObjectAsString(this.observeAsXml(obj, mediaType));
        }
        return obj;
    }

    private String getAsString(String uri, int mediaType) throws CoapException {
        coapClient = getCoapClientWithUri(uri);
        CoapResponse r = coapClient.get();
        if (r == null) {
            throw new CoapException(HttpStatus.SC_BAD_REQUEST);
        }
        if (r.getResponseText().contains("obix:BadUriErr")) {
            logger.info("BAD URI: " + uri);
        }
        return r.getResponseText();
    }

    private String observeAsXml(ObixObject obj, int mediaType) {
        final String uri = obj.getObixUri();
        coapClient = getCoapClientWithUri(obj.getObixUri());
        String content = "";
        Object notifier = new Object();
        CoapObserveRelation relation = coapClient.observeAndWait(
                new CoapHandler() {
                    public void onLoad(CoapResponse response) {
                        String content = response.getResponseText();
                        ObixObject o = getObservedObjects().get(uri);
                        o.setObj(ObixXmlChannelDecorator.decode(content));
                        getObservedObjects().remove(uri);
                        getObservedObjects().put(uri, o);
                        synchronized (notifier) {
                            notifier.notify();
                        }
                        synchronized (o) {
                            o.notify();
                        }
                    }

                    public void onError() {
                        logger.error("OBSERVING " + obj.getObixUri() + " FAILED");
                    }

                }, mediaType);

        synchronized (notifier) {
            try {
                notifier.wait();
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

    public static String trimBaseUri(String baseUri, String uri) {
        String base = baseUri.split("/")[0];
        String firstInUri = uri.split("/")[0];
        if (base.equals(firstInUri) && uri.split("/").length > 1) {
            return uri.substring(uri.indexOf("/") + 1);
        }
        return uri;
    }

    private CoapClient getCoapClientWithUri(String uri) throws IllegalArgumentException {
        CoapClient coapClient;
        coapClient = new CoapClient("coap", this.baseUri, this.port, trimBaseUri(this.baseUri, uri));
        coapClient.setTimeout(2000);
        return coapClient;
    }
}
