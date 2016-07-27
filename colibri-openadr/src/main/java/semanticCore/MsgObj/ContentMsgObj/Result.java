package semanticCore.MsgObj.ContentMsgObj;

import Utils.Pair;

import java.util.HashMap;

/**
 * Created by georg on 25.07.16.
 */
public class Result {
    HashMap<String, Pair<String, String>> bindings;

    public Result(){
        bindings = new HashMap<>();
    }

    public String getTripelValue(String property){
        return bindings.get(property).getFst();
    }

    public String getTripelType(String property){
        return bindings.get(property).getFst();
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
