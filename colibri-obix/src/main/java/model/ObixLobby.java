package model;

import obix.Obj;

import java.util.ArrayList;
import java.util.List;

public class ObixLobby {

    /**
     * This String represents the oBIX lobby.
     * For example, the lobby can be in XML-Format. In this case, the lobbyAsString variable will be the XML-String
     * which represents the lobby.
     * The lobbyAsString variable can be null.
     */

    private String lobbyAsString;

    private String lobbyUri;

    private List<ObixObject> points;

    private Obj obj;

    public ObixLobby(String uri) {
        this.lobbyUri = uri;
        this.points = new ArrayList<ObixObject>();
    }

    public String getLobbyUri() {
        return lobbyUri;
    }

    public void setLobbyUri(String uri) {
        this.lobbyUri = uri;
    }

    public String getLobbyAsString() {
        return lobbyAsString;
    }

    public void setLobbyAsString(String lobbyAsString) {
        this.lobbyAsString = lobbyAsString;
    }

    public List<ObixObject> getPoints() {
        return points;
    }

    public void setPoints(List<ObixObject> points) {
        this.points = points;
    }

    public Obj getObj() {
        return obj;
    }

    public void setObj(Obj obj) {
        this.obj = obj;
    }
}
