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
import service.Configurator;

import java.util.List;

import static org.eclipse.californium.core.coap.MediaTypeRegistry.APPLICATION_XML;

/**
 * This class represents a plain channel which sends data over CoAP. In order to sufficiently parse the
 * sent and received data, the channel has to be decorated by at least one decorator channel,
 * for example the {@link ObixXmlChannelDecorator}.
 */
public class CoapChannel extends ObixChannel {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    private CoapClient coapClient;
    private static final Logger logger = LoggerFactory.getLogger(CoapChannel.class);

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    public CoapChannel(String baseUri, String lobbyUri, List<String> observedTypes) {
        super(baseUri, lobbyUri, observedTypes);
    }

    public CoapChannel(String baseUri, String lobbyUri, Integer port, List<String> observedTypes) {
        super(baseUri, lobbyUri, port, observedTypes);
    }

    /******************************************************************
     *                            Methods                             *
     ******************************************************************/

    /**
     * This method takes an relative URI and returns an absolute URI based on the {@link #getBaseUri()}.
     *
     * @param uri   The URI which should be normalized.
     * @return      The normalized URI as a String.
     */
    public String normalizeUri(String uri) {
        return CoapChannel.normalizeUri(uri, this.getBaseUri());
    }

    /**
     * This method takes an relative URI and a base URI and returns an absolute URI.
     *
     * @param uri       The URI which should be normalized.
     * @param baseUri   The base URI which is used to create an absolute URI.
     * @return          The normalized URI as a String.
     */
    public static String normalizeUri(String uri, String baseUri) {
        return "coap://" + ObixChannel.normalizeUri(uri, baseUri);
    }

    /**
     * This method trims the base URI from an absolute URI and returns the trimmed URI.
     *
     * @param baseUri   The base URI which is trimmed.
     * @param uri       The URI which should be trimmed.
     * @return          The URI as a String which doesn't contain the base URI.
     */
    private static String trimBaseUri(String baseUri, String uri) {
        String base = baseUri.split("/")[0];
        String firstInUri = uri.split("/")[0];
        if (base.equals(firstInUri) && uri.split("/").length > 1) {
            return uri.substring(uri.indexOf("/") + 1);
        }
        return uri;
    }

    /**
     * This method sends a GET message to the CoAP endpoint with the given URI and returns the response as a String.
     *
     * @param uri               The uri which is used for the GET message.
     * @param mediaType         The media type which is used for the GET message.
     * @return                  The response to the GET message.
     * @throws CoapException    Thrown, if no response is received.
     */
    private String getAsString(String uri, int mediaType) throws CoapException {
        coapClient = getCoapClientWithUri(uri);
        CoapResponse r = coapClient.get(mediaType);
        if (r == null) {
            throw new CoapException(HttpStatus.SC_BAD_REQUEST);
        }

        if (r.getResponseText().contains("BadUriErr")) {
            logger.info("BAD URI: " + uri);
        }
        return r.getResponseText();
    }

    /**
     * This method sends a GET message with the given obix lobby-URI and returns the response as an {@link ObixLobby}.
     *
     * @param uri   The URI of the requested oBIX lobby.
     * @return      The requested {@link ObixLobby}.
     */
    public ObixLobby getLobby(String uri) {
        return super.getLobby(uri);
    }

    /**
     * This method sends a GET message with the given obix lobby-URI and returns the response as an {@link ObixLobby}.
     *
     * @param uri               The URI of the requested oBIX lobby.
     * @param mediaType         The media type which is used for the GET message.
     * @return                  The requested {@link ObixLobby}.
     * @throws CoapException    Thrown, if no response is received.
     */
    public ObixLobby getLobby(String uri, int mediaType) throws CoapException {
        ObixLobby lobby = new ObixLobby(uri, getObservedTypes());
        lobby.setLobbyAsString(this.getAsString(uri, mediaType));
        return lobby;
    }

    /**
     * This method sends a GET message to the CoAP endpoint with the given URI and returns
     * the response as an {@link ObixObject}.
     *
     * @param uri   The uri which is used for the GET message.
     * @return      The response to the GET message.
     */
    public ObixObject get(String uri) {
        return super.get(uri);
    }

    /**
     * This method sends a GET message to the CoAP endpoint with the given URI and returns
     * the response as an {@link ObixObject}.
     *
     * @param uri       The uri which is used for the GET message.
     * @param mediaType The media type which is used for the GET message.
     * @return          The response to the GET message.
     */
    public ObixObject get(String uri, int mediaType) {
        ObixObject object = new ObixObject(uri, this.getPort());
        object.setObjectAsString(this.getAsString(uri, mediaType));
        return object;
    }

    /**
     * This method observes an {@link ObixObject} in xml-form.
     *
     * @param obj           The {@link ObixObject} which is observed.
     * @param mediaType     The media type which is used for the observation, in this case xml.
     * @return              The observed {@link ObixObject} in form of a String and in xml-format.
     */
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
                        o.setSetByObix(true);
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

    /**
     * This method observes an {@link ObixObject}.
     *
     * @param obj   The oBIXObject which is observed.
     * @return      The observed {@link ObixObject} in form of an {@link ObixObject}.
     */
    public ObixObject observe(ObixObject obj) {
        return super.observe(obj);
    }

    /**
     * This method observes an {@link ObixObject}.
     *
     * @param obj       The oBIXObject which is observed.
     * @param mediaType The media type which is used for the OBS message.
     * @return          The observed {@link ObixObject} in form of an {@link ObixObject}.
     */
    public ObixObject observe(ObixObject obj, int mediaType) {
        getObservedObjects().put(obj.getObixUri(), obj);
        if (APPLICATION_XML == mediaType) {
            obj.setObjectAsString(this.observeAsXml(obj, mediaType));
        }
        return obj;
    }

    /**
     * This method sends a PUT message to the CoAP endpoint with the given URI and returns
     * the response as an {@link ObixObject}.
     *
     * @param obj   The {@link ObixObject} which is used for th PUT message.
     * @return      The response to the PUT message.
     */
    public ObixObject put(ObixObject obj) {
        return super.put(obj);
    }

    /**
     * This method sends a PUT message to the CoAP endpoint with the given URI and returns
     * the response as an {@link ObixObject}.
     *
     * @param obj       The {@link ObixObject} which is used for th PUT message.
     * @param mediaType The media type which is used for the PUT message.
     * @return          The response to the PUT message.
     */
    public ObixObject put(ObixObject obj, int mediaType) {
        coapClient = getCoapClientWithUri(obj.getObixUri());
        obj.setObjectAsString(coapClient.put(obj.getObjectAsString(), mediaType).getResponseText());
        return obj;
    }

    /**
     * This method returns a {@link CoapClient} which was created using the given URI.
     *
     * @param uri   The URI which is used for the initialisation of the {@link CoapClient}.
     * @return      The new {@link CoapClient} with the given URI.
     */
    private CoapClient getCoapClientWithUri(String uri){
        CoapClient coapClient;
        coapClient = new CoapClient("coap", getBaseUri(), this.getPort(), trimBaseUri(getBaseUri(), uri));
        coapClient.setTimeout(Configurator.getInstance().getTimeWaitingForResponseInMilliseconds());
        return coapClient;
    }
}
