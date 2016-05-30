package model;

import java.util.List;

public class ObixLobby {

    /**
     * This String represents the oBIX lobby.
     * For example, the lobby can be in XML-Format. In this case, the lobbyAsString variable will be the XML-String
     * which represents the lobby.
     * The lobbyAsString variable can be null.
     */
    private String lobbyAsString;
    private String uri;
    private List<ObixObject> oBIXObjects;

    public ObixLobby(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<ObixObject> getoBIXObjects() {
        return oBIXObjects;
    }

    public void setoBIXObjects(List<ObixObject> oBIXObjects) {
        this.oBIXObjects = oBIXObjects;
    }

    public String getLobbyAsString() {
        return lobbyAsString;
    }

    public void setLobbyAsString(String lobbyAsString) {
        this.lobbyAsString = lobbyAsString;
    }
}
