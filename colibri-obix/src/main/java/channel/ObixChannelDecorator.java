package channel;

import model.ObixLobby;
import model.ObixObject;

public abstract class ObixChannelDecorator implements ObixChannel{
    protected ObixChannel channel;

    protected ObixChannelDecorator(ObixChannel channel) {
        this.channel = channel;
    }

    public ObixLobby getLobby(String uri) {
        return channel.getLobby(uri);
    }

    public ObixObject get(String uri) {
        return channel.get(uri);
    }

    public ObixLobby getLobby(String uri, int mediaType) {
        return channel.getLobby(uri, mediaType);
    }

    public ObixObject get(String uri, int mediaType) {
        return channel.get(uri, mediaType);
    }
}
