package channel.obix;

import model.obix.ObixLobby;
import model.obix.ObixObject;
import org.eclipse.californium.core.coap.CoAP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.californium.core.coap.MediaTypeRegistry.APPLICATION_XML;

/**
 * This Interface is implemented by plain channels, for example the {@link CoapChannel}.
 * Each plain channel has to be decorated by at least one channel decorator, for example the {@link ObixXmlChannelDecorator}.
 */
public abstract class ObixChannel {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    private String baseUri;
    private String lobbyUri;

    /**
     * A list of obix types which can be observed, for example obix.Bool or obix.Real.
     */
    private List<String> observedTypes;
    private Map<String, ObixObject> observedObjects = new HashMap<String, ObixObject>();
    private Integer port;

    private static final Logger logger = LoggerFactory.getLogger(ObixChannel.class);

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    public ObixChannel() {
    }

    public ObixChannel(String baseUri, String lobbyUri, List<String> observedTypes) {
        this.baseUri = baseUri;
        this.lobbyUri = lobbyUri;
        this.observedTypes = observedTypes;
        this.port = CoAP.DEFAULT_COAP_PORT;
    }

    public ObixChannel(String baseUri, String lobbyUri, Integer port, List<String> observedTypes) {
        this.baseUri = baseUri;
        this.lobbyUri = lobbyUri;
        this.port = port;
        this.observedTypes = observedTypes;
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
    public abstract String normalizeUri(String uri);

    /**
     * This method takes an relative URI and a base URI and returns an absolute URI.
     *
     * @param uri       The URI which should be normalized.
     * @param baseUri   The base URI which is used to create an absolute URI.
     * @return          The normalized URI as a String.
     */
    public static String normalizeUri(String uri, String baseUri) {
        if (uri.contains(":")) {
            uri = uri.split(":")[1];
        }

        // % replaced with ------ because of problems with californium
        if (uri.contains("%") || baseUri.contains("%")) {
            uri = uri.replace("%", "--");
            baseUri = baseUri.replace("%", "------");
        }

        if(uri.startsWith("/")) {
            return uri;
        }

        String[] baseUriPaths = baseUri.split("/");
        String retUri = "";
        for (int i = 0; i < baseUriPaths.length - 1; i++) {
            retUri += baseUriPaths[i] + "/";
        }
        return retUri + uri;
    }

    /**
     * This method sends a GET message with the obix lobby-URI of this channel and returns the response as an {@link ObixLobby}.
     *
     * @return      The requested {@link ObixLobby}.
     */
    public ObixLobby getLobby() {
        return this.getLobby(lobbyUri, APPLICATION_XML);
    }

    /**
     * This method sends a GET message with the given obix lobby-URI and returns the response as an {@link ObixLobby}.
     *
     * @param uri   The URI of the requested oBIX lobby.
     * @return      The requested {@link ObixLobby}.
     */
    public ObixLobby getLobby(String uri) {
        return this.getLobby(uri, APPLICATION_XML);
    }

    /**
     * This method sends a GET message with the given obix lobby-URI and returns the response as an {@link ObixLobby}.
     *
     * @param uri               The URI of the requested oBIX lobby.
     * @param mediaType         The media type which is used for the GET message.
     * @return                  The requested {@link ObixLobby}.
     */
    public abstract ObixLobby getLobby(String uri, int mediaType);

    /**
     * This method sends a GET message to the CoAP endpoint with the given URI and returns
     * the response as an {@link ObixObject}.
     *
     * @param uri   The uri which is used for the GET message.
     * @return      The response to the GET message.
     */
    public ObixObject get(String uri) {
        return this.get(uri, APPLICATION_XML);
    }

    /**
     * This method sends a GET message to the CoAP endpoint with the given URI and returns
     * the response as an {@link ObixObject}.
     *
     * @param uri       The uri which is used for the GET message.
     * @param mediaType The media type which is used for the GET message.
     * @return          The response to the GET message.
     */
    public abstract ObixObject get(String uri, int mediaType);

    /**
     * This method observes an {@link ObixObject}.
     *
     * @param obj   The oBIXObject which is observed.
     * @return      The observed {@link ObixObject} in form of an {@link ObixObject}.
     */
    public ObixObject observe(ObixObject obj) {
        return this.observe(obj, APPLICATION_XML);
    }

    /**
     * This method observes an {@link ObixObject}.
     *
     * @param obj       The oBIXObject which is observed.
     * @param mediaType The media type which is used for the OBS message.
     * @return          The observed {@link ObixObject} in form of an {@link ObixObject}.
     */
    public abstract ObixObject observe(ObixObject obj, int mediaType);

    /**
     * This method sends a PUT message to the CoAP endpoint with the given URI and returns
     * the response as an {@link ObixObject}.
     *
     * @param obj   The {@link ObixObject} which is used for th PUT message.
     * @return      The response to the PUT message.
     */
    public ObixObject put(ObixObject obj) {
        return this.put(obj, APPLICATION_XML);
    }

    /**
     * This method sends a PUT message to the CoAP endpoint with the given URI and returns
     * the response as an {@link ObixObject}.
     *
     * @param obj       The {@link ObixObject} which is used for th PUT message.
     * @param mediaType The media type which is used for the PUT message.
     * @return          The response to the PUT message.
     */
    public abstract ObixObject put(ObixObject obj, int mediaType);

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public String getBaseUri() {
        return this.baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public String getLobbyUri() {
        return lobbyUri;
    }

    public void setLobbyUri(String lobbyUri) {
        this.lobbyUri = lobbyUri;
    }

    public List<String> getObservedTypes() {
        return observedTypes;
    }

    public Map<String, ObixObject> getObservedObjects() {
        return observedObjects;
    }

    public Integer getPort() {
        return port;
    }
}
