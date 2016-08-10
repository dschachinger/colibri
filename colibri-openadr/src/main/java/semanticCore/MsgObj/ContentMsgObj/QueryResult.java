package semanticCore.MsgObj.ContentMsgObj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by georg on 25.07.16.
 * Objects from this class hold the result of a sparql query
 */
public class QueryResult {
    // all properties within a query result
    List<String> properties;
    List<Result> results;
    // true...result is from an ask query (result is only true or false)
    boolean isFromASKQuery;
    // ask query result, null or otherwise...no result,
    Boolean ASKQueryResult;

    public QueryResult(boolean isFromASKQuery){
        this.isFromASKQuery = isFromASKQuery;
        if(!isFromASKQuery){
            properties = new ArrayList<>();
            results = new ArrayList<>();
        } else {
            ASKQueryResult = null;
        }
    }

    public void addTupel(Result result){
        if(!isFromASKQuery) {
            results.add(result);
        }
    }

    public void setASKQueryResult(boolean ASKQueryResult){
        this.ASKQueryResult = ASKQueryResult;
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
        return ASKQueryResult;
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
