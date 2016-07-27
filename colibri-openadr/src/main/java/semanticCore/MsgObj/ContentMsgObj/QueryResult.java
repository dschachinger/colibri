package semanticCore.MsgObj.ContentMsgObj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by georg on 25.07.16.
 */
public class QueryResult {
    List<String> properties;
    List<Result> results;
    boolean isFromASKQuery;
    Integer ASKQueryResult;


    public QueryResult(boolean isFromASKQuery){
        this.isFromASKQuery = isFromASKQuery;
        if(!isFromASKQuery){
            properties = new ArrayList<>();
            results = new ArrayList<>();
        } else {
            ASKQueryResult = new Integer(-1);
        }
    }

    public void addTupel(Result result){
        if(!isFromASKQuery) {
            results.add(result);
        }
    }

    public void setASKQueryResult(boolean ASKQueryResult){
        this.ASKQueryResult = ASKQueryResult? 1 : 0;
    }

    public void addProperty(String property) {
        properties.add(property);
    }

    public List<String> getProperties() {
        return properties;
    }

    public List<Result> getResults() {
        return results;
    }

    public Boolean getASKQueryResult() {
        switch (ASKQueryResult){
            case 0: return false;
            case 1: return true;
            default: return null;
        }
    }

    public String toString(){
        String out = "QUERY result\n";

        if(isFromASKQuery){
            out += "boolean value: " + getASKQueryResult();
        } else {
            out += "properties: " + Arrays.toString(properties.toArray()) + "\n";
            for(Result result : results){
                out += result +"\n";
            }
        }

        return out;
    }
}
