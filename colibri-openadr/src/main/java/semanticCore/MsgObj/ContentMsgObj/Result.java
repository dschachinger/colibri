package semanticCore.MsgObj.ContentMsgObj;

import Utils.Pair;

import java.util.HashMap;

/**
 * Created by georg on 25.07.16.
 * Objects from this class represent one query solution within a query result.
 */
public class Result {
    /* This map stores a pair for each property (=key).
        The first element of the pair is the associated value and the second one is the associated type.*/
    HashMap<String, Pair<String, String>> bindings;

    public Result(){
        bindings = new HashMap<>();
    }

    public String getTripleValue(String property){
        return bindings.get(property).getFst();
    }

    public String getTripleType(String property){
        return bindings.get(property).getSnd();
    }

    public void addBinding(String property, String value, String type){
        bindings.put(property, new Pair<String, String>(value, type));
    }

    public String toString(){
        String out = "Result:\n";
        for (String prop : bindings.keySet()){
            String value = bindings.get(prop).getFst();
            String type = bindings.get(prop).getSnd();

            out += "\tKEY: "+ prop + " VALUE: " + value + " TYPE: " + type+"\n";
        }
        return out;
    }
}
