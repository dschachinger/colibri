package channel;

import model.ObixLobby;
import model.ObixObject;
import obix.Obj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.californium.core.coap.MediaTypeRegistry.APPLICATION_XML;

/**
 * This Interface is implemented by plain channels, for example the {@link CoapChannel}.
 * Each plain channel has to be decorated by at least one channel decorator, for example the {@link ObixXmlChannelDecorator}.
 */
public abstract class ObixChannel {

    protected String baseUri;
    protected String lobbyUri;
    protected List<String> observedTypes;

    public ObixChannel() {
    }

    public ObixChannel(String baseUri, String lobbyUri, List<String> observedTypes) {
        this.baseUri = baseUri;
        this.lobbyUri = lobbyUri;
        this.observedTypes = observedTypes;
    }

    /**
     * Requests the oBIX lobby with the lobby URI of the channel.
     * Uses XML as messgae format.
     *
     * @return                  The oBIX lobby of the channel.
     */
    public ObixLobby getLobby() {
        return this.getLobby(lobbyUri, APPLICATION_XML);
    }

    /**
     * Requests the oBIX lobby specified URI.
     * Uses XML as messgae format.
     *
     * @param   uri             The URI of the requested oBIX lobby.
     * @return                  The oBIX lobby of the specified URI.
     */
    public ObixLobby getLobby(String uri) {
        return this.getLobby(uri, APPLICATION_XML);
    }

    /**
     * Observers data from the oBIX resource with the specified URI.
     *
     * NOTE: ONLY XML SUPPORTED SO FAR.
     *
     * @param   uri             The URI of the observed oBIX resource.
     * @return                  The oBIX Object with the specified URI.
     */
     public ObixObject observe(String uri) {
         return this.observe(uri, APPLICATION_XML);
     }

    /**
     * Requests data from the oBIX resource with the specified URI.
     *
     * NOTE: ONLY XML SUPPORTED SO FAR.
     *
     * @param   uri             The URI of the requested oBIX resource.
     * @return                  The oBIX Object with the specified URI.
     */
    public ObixObject get(String uri) {
        return this.get(uri, APPLICATION_XML);
    }

    /**
     * Requests the oBIX lobby specified URI.
     * Uses XML as messgae format.
     *
     * @param   uri             The URI of the requested oBIX lobby.
     * @param   mediaType       The requested media type, for example APPLICATION_XML.
     * @return                  The oBIX lobby of the specified URI.
     */
    public abstract ObixLobby getLobby(String uri, int mediaType);

    /**
     * Requests data from the oBIX resource with the specified URI.
     *
     * NOTE: ONLY XML SUPPORTED SO FAR.
     *
     * @param   uri             The URI of the requested oBIX resource.
     * @param   mediaType       The requested media type, for example APPLICATION_XML.
     * @return                  The oBIX Objects with the specified URI.
     */
     public abstract ObixObject get(String uri, int mediaType);

    /**
     * Observers data from the oBIX resource with the specified URI.
     *
     * NOTE: ONLY XML SUPPORTED SO FAR.
     *
     * @param   uri             The URI of the observed oBIX resource.
     * @param   mediaType       The requested media type, for example APPLICATION_XML.
     * @return                  The oBIX Object with the specified URI.
     */
    public abstract ObixObject observe(String uri, int mediaType);

    /**
     * Normalizes the given URI against the CoAP-base URI of the channel
     *
     * @param uri   The URI which should be normalized
     * @return      The normalized URI
     */
    public abstract String normalizeUri(String uri);

    public static String normalizeUri(String uri, String baseUri) {
        String[] uriPaths = uri.split("/");
        String newUri = baseUri;
        List<String> notUsedPaths = new ArrayList<String>(Arrays.asList(uriPaths));
        for(String s : uriPaths) {
            if(newUri.contains(s)) {
                notUsedPaths.remove(s);
                break;
            }
        }
        for(String s: notUsedPaths) {
            newUri += "/" + s;
        }
        return newUri;
    }

    /**
     * Returns the base URI of the oBIX Channel
     *
     * @return  The base URI of the oBIX channel.
     */
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

    public void setObservedTypes(List<String> observedTypes) {
        this.observedTypes = observedTypes;
    }
}
