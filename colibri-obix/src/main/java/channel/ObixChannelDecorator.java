package channel;

import model.ObixLobby;
import model.ObixObject;

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

    public ObixObject observe(String uri) {
        return channel.observe(uri);
    }

    public ObixObject observe(String uri, int mediaType) {
        return channel.observe(uri, mediaType);
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
}
