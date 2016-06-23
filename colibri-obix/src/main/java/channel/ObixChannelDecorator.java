package channel;

import model.ObixLobby;
import model.ObixObject;
import obix.Obj;

import java.util.Map;

public abstract class ObixChannelDecorator extends ObixChannel{
    protected ObixChannel channel;

    protected ObixChannelDecorator(ObixChannel channel) {
        this.channel = channel;
    }

    public ObixLobby getLobby() {
        return this.getLobby(channel.getLobbyUri());
    }

    public ObixLobby getLobby(String uri) {
        return channel.getLobby(uri);
    }

    public ObixLobby getLobby(String uri, int mediaType) {
        return channel.getLobby(uri, mediaType);
    }

    public ObixObject get(String uri) {
        return channel.get(uri);
    }

    public ObixObject get(String uri, int mediaType) {
        return channel.get(uri, mediaType);
    }

    public ObixObject put(ObixObject obj) {
        return channel.put(obj);
    }

    public ObixObject put(ObixObject obj, int mediaType) {
        return channel.put(obj, mediaType);
    }

    public ObixObject observe(ObixObject obj) {
        return channel.observe(obj);
    }

    public ObixObject observe(ObixObject obj, int mediaType) {
        return channel.observe(obj, mediaType);
    }

    public void setLobbyUri(String lobbyUri) {
        channel.setLobbyUri(lobbyUri);
    }

    public String getLobbyUri() {
        return channel.getLobbyUri();
    }


    public void setBaseUri(String baseUri) {
        channel.setBaseUri(baseUri);
    }

    public String getBaseUri() {
        return channel.getBaseUri();
    }

    public String normalizeUri(String uri) {
        return channel.normalizeUri(uri);
    }

    public Map<String, ObixObject> getObservedObjects() {
        return channel.getObservedObjects();
    }

    public Integer getPort() {
        return channel.getPort();
    }
}
