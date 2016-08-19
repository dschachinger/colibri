package model.obix;

import obix.Obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to represent obix lobbies which consist of {@link ObixObject}.
 */
public class ObixLobby {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    /**
     * This String represents the {@link ObixLobby}.
     * The lobby can be in XML-format. In this case, the lobbyAsString variable will be the xml-String
     * which represents the lobby.
     * The lobbyAsString variable can be null.
     */
    private String lobbyAsString;
    private String lobbyUri;

    /**
     * Lists for {@link ObixObject} which contain the objects depending on their type.
     */
    private List<ObixObject> obixObjects = new ArrayList<ObixObject>();
    private List<ObixObject> abstimes = new ArrayList<ObixObject>();
    private List<ObixObject> reltimes = new ArrayList<ObixObject>();
    private List<ObixObject> vals = new ArrayList<ObixObject>();
    private List<ObixObject> bools = new ArrayList<ObixObject>();
    private List<ObixObject> ints = new ArrayList<ObixObject>();
    private List<ObixObject> reals = new ArrayList<ObixObject>();
    private List<ObixObject> enums = new ArrayList<ObixObject>();
    private List<ObixObject> feeds = new ArrayList<ObixObject>();
    private List<ObixObject> lists = new ArrayList<ObixObject>();
    private List<ObixObject> ops = new ArrayList<ObixObject>();
    private List<ObixObject> refs = new ArrayList<ObixObject>();
    private List<ObixObject> uris = new ArrayList<ObixObject>();
    private List<ObixObject> strs = new ArrayList<ObixObject>();
    private List<ObixObject> errs = new ArrayList<ObixObject>();
    private Map<String, List<ObixObject>> observedObjectsLists = new HashMap<String, List<ObixObject>>();
    private Obj obj;

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    private ObixLobby(List<String> observedObjects) {
        if(observedObjects != null) {
            for (String s : observedObjects) {
                if (s.equals("obix.Abstime")) {
                    observedObjectsLists.put(s, abstimes);
                } else if (s.equals("obix.Reltime")) {
                    observedObjectsLists.put(s, reltimes);
                } else if (s.equals("obix.Bool")) {
                    observedObjectsLists.put(s, bools);
                } else if (s.equals("obix.Int")) {
                    observedObjectsLists.put(s, ints);
                } else if (s.equals("obix.Real")) {
                    observedObjectsLists.put(s, reals);
                } else if (s.equals("obix.Enum")) {
                    observedObjectsLists.put(s, enums);
                } else if (s.equals("obix.Feed")) {
                    observedObjectsLists.put(s, feeds);
                } else if (s.equals("obix.List")) {
                    observedObjectsLists.put(s, lists);
                } else if (s.equals("obix.Op")) {
                    observedObjectsLists.put(s, ops);
                } else if (s.equals("obix.Ref")) {
                    observedObjectsLists.put(s, refs);
                } else if (s.equals("obix.Uri")) {
                    observedObjectsLists.put(s, uris);
                } else if (s.equals("obix.Str")) {
                    observedObjectsLists.put(s, strs);
                } else if (s.equals("obix.Err")) {
                    observedObjectsLists.put(s, errs);
                } else if (s.equals("obix.Val")) {
                    observedObjectsLists.put(s, vals);
                } else {
                    observedObjectsLists.put(s, errs);
                }
            }
            observedObjectsLists.put("all", obixObjects);
        }
    }

    public ObixLobby(String uri, List<String> observedObjects) {
        this(observedObjects);
        this.lobbyUri = uri;
    }

    /******************************************************************
     *                            Methods                             *
     ******************************************************************/

    /**
     * This method parses the type of all {@link ObixObject} in the {@link ObixLobby} and puts the objects in the
     * according list.
     */
    private void matchObixObjectsToLists() {
        for(ObixObject obj : obixObjects) {
            if(obj.getObj().isAbstime()) {
                abstimes.add(obj);
            } else if(obj.getObj().isInt()) {
                ints.add(obj);
            }
            else if(obj.getObj().isReltime()) {
                reltimes.add(obj);
            }
            else if(obj.getObj().isBool()) {
                bools.add(obj);
            }
            else if(obj.getObj().isReal()) {
                reals.add(obj);
            }
            else if(obj.getObj().isEnum()) {
                enums.add(obj);
            }
            else if(obj.getObj().isFeed()) {
                feeds.add(obj);
            }
            else if(obj.getObj().isList()) {
                lists.add(obj);
            }
            else if(obj.getObj().isRef()) {
                refs.add(obj);
            }
            else if(obj.getObj().isUri()) {
                uris.add(obj);
            }
            else if(obj.getObj().isStr()) {
                strs.add(obj);
            }
            else if(obj.getObj().isVal()) {
                vals.add(obj);
            }
            else if(obj.getObj().isNull()) {
                errs.add(obj);
            }
            else if(obj.getObj().isErr()) {
                errs.add(obj);
            }
            else {
                errs.add(obj);
            }
        }
    }

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

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

    public List<ObixObject> getObixObjects() {
        return obixObjects;
    }

    public void setObixObjects(List<ObixObject> obixObjects) {
        this.obixObjects.clear();
        this.obixObjects.addAll(obixObjects);
        matchObixObjectsToLists();
    }

    public Obj getObj() {
        return obj;
    }

    public void setObj(Obj obj) {
        this.obj = obj;
    }

    public List<ObixObject> getAbstimes() {
        return abstimes;
    }

    public List<ObixObject> getReltimes() {
        return reltimes;
    }

    public List<ObixObject> getVals() {
        return vals;
    }

    public List<ObixObject> getBools() {
        return bools;
    }

    public List<ObixObject> getInts() {
        return ints;
    }

    public List<ObixObject> getReals() {
        return reals;
    }

    public List<ObixObject> getEnums() {
        return enums;
    }

    public List<ObixObject> getFeeds() {
        return feeds;
    }

    public List<ObixObject> getLists() {
        return lists;
    }

    public List<ObixObject> getOps() {
        return ops;
    }

    public List<ObixObject> getRefs() {
        return refs;
    }

    public List<ObixObject> getUris() {
        return uris;
    }

    public List<ObixObject> getStrs() {
        return strs;
    }

    public List<ObixObject> getErrs() {
        return errs;
    }

    public Map<String, List<ObixObject>> getObservedObjectsLists() {
        return observedObjectsLists;
    }
}
