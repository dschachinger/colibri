package channel;

import exception.CoapCommunicationException;
import model.ObixLobby;
import model.ObixObject;

/**
 * This Interface is implemented by plain channels, for example the {@link CoapChannel}.
 * Each plain channel has to be decorated by at least one channel decorator, for example the {@link ObixXmlChannelDecorator}.
 */
public interface ObixChannel {

    /**
     * Requests the oBIX lobby specified URI.
     * Uses XML as messgae format.
     *
     * @param   uri             The URI of the requested oBIX lobby.
     * @return                  The oBIX lobby of the specified URI.
     */
    ObixLobby getLobby(String uri);

    /**
     * Requests data from the oBIX resource with the specified URI.
     *
     * NOTE: ONLY XML SUPPORTED SO FAR.
     *
     * @param   uri             The URI of the requested oBIX resource.
     * @return                  The oBIX Objects with the specified URI.
     */
    ObixObject get(String uri);

    /**
     * Requests the oBIX lobby specified URI.
     * Uses XML as messgae format.
     *
     * @param   uri             The URI of the requested oBIX lobby.
     * @param   mediaType       The requested media type, for example APPLICATION_XML.
     * @return                  The oBIX lobby of the specified URI.
     */
    ObixLobby getLobby(String uri, int mediaType);

    /**
     * Requests data from the oBIX resource with the specified URI.
     *
     * NOTE: ONLY XML SUPPORTED SO FAR.
     *
     * @param   uri             The URI of the requested oBIX resource.
     * @param   mediaType       The requested media type, for example APPLICATION_XML.
     * @return                  The oBIX Objects with the specified URI.
     */
    ObixObject get(String uri, int mediaType);
}
