package Bridge;

import Utils.Pair;
import Utils.SortedDateIntervalList;
import openADR.OADRHandling.OADR2VEN;
import openADR.OADRMsgInfo.MsgInfo_OADRDistributeEvent;
import openADR.OADRMsgInfo.OADRMsgInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semanticCore.MsgObj.ColibriMessage;
import semanticCore.MsgObj.ContentMsgObj.QueryResult;
import semanticCore.WebSocketHandling.ColibriClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by georg on 02.07.16.
 * Objects from this class are used to bridge the colibri part with the openADR part.
 * This is for both directions for the direction from colibri to openADR and vice-versa.
 */
public class OpenADRColibriBridge {

    private OADR2VEN oadrVEN;
    private ColibriClient colClient;

    private ColibriToOpenADR colibriToOpenADR;
    private OpenADRToColibri openADRToColibri;

    // This map stores for every service (key: service URL) a sorted list which contains all received events. They are ordered by their active time.
    private HashMap<String, SortedDateIntervalList<MsgInfo_OADRDistributeEvent.Event>> serviceSortedReceivedEvents;

    // This map stores for every eventID (=key) the associated service URL (=value)
    private HashMap<String, String> eventsTyp;

    private HashMap<String, List<QueryResult>> openADRReportData;

    private Logger logger = LoggerFactory.getLogger(OpenADRColibriBridge.class);

    public OpenADRColibriBridge(){
        colibriToOpenADR = new ColibriToOpenADR();
        openADRToColibri = new OpenADRToColibri();

        serviceSortedReceivedEvents = new HashMap<>();
        eventsTyp = new HashMap<>();
        openADRReportData = new HashMap<>();
    }

    public OpenADRToColibri getOpenADRToColibri() {
        return openADRToColibri;
    }

    public void setOadrVEN(OADR2VEN oadrVEN) {
        this.oadrVEN = oadrVEN;
    }

    public void setColClient(ColibriClient colClient) {
        this.colClient = colClient;
    }

    public void informationFlowFromOpenADRToColibri(OADRMsgInfo info){
        List<ColibriMessage> colibriMessages = openADRToColibri.convertOpenADRMsg(info, this);
        if(colibriMessages != null){
            for(ColibriMessage colibriMessage : colibriMessages){
                colClient.sendColibriMsg(colibriMessage);
            }
        }
    }

    public void informationFlowFromColibriToOpenADR(ColibriMessage info){
        Pair<ColibriMessage, OADRMsgInfo> result = colibriToOpenADR.convertColibriMsg(info, this);
        ColibriMessage reply = result.getFst();
        OADRMsgInfo oadrMsgInfo = result.getSnd();

        if(oadrMsgInfo !=null){
            oadrVEN.getChannel().sendMsg(oadrMsgInfo);
        }

        if(reply != null){
            colClient.sendColibriMsg(reply);
        }
    }

    public HashMap<String, List<QueryResult>> getOpenADRReportData() {
        return openADRReportData;
    }

    public void queryColibriCoreForOpenADRReportData(List<QueryResult> reportInfos){
        // TODO insert proper query
        String query = "SELECT ?service ?identifier \n" +
                "WHERE { ?service rdf:type colibri:DataService. \n" +
                "?service colibri:hasDataConfiguration ?y. \n" +
                "?y colibri:hasParameter ?z. \n" +
                "?z rdf:type colibri:TemperatureParameter. \n" +
                "?service colibri:identifier ?identifier} ";
        openADRReportData.put(query, reportInfos);
        colClient.sendQueryMessage(query);
    }

    public List<MsgInfo_OADRDistributeEvent.Event> getOpenADREvents(String serviceName, Pair<Date, Date> timeInterval) {
        SortedDateIntervalList<MsgInfo_OADRDistributeEvent.Event> sortedEvents = serviceSortedReceivedEvents.get(serviceName);
        List<MsgInfo_OADRDistributeEvent.Event> events = new ArrayList<>();

        if(sortedEvents==null){
            return events;
        }

        List<Pair<Pair<Date, Date>, MsgInfo_OADRDistributeEvent.Event>> subSet = sortedEvents.getAllFittingIntervals(timeInterval);

        for(Pair<Pair<Date, Date>, MsgInfo_OADRDistributeEvent.Event> elem : subSet){
            events.add(elem.getSnd());
            logger.info("found: " + elem.getSnd().getEventID());
        }

        return events;
    }

    private int getOpenADREventIndex (String serviceName, String eventID){
        SortedDateIntervalList<MsgInfo_OADRDistributeEvent.Event> sortedPUTMessages = serviceSortedReceivedEvents.get(serviceName);

        if(sortedPUTMessages != null){
            int i = 0;
            for(Pair<Pair<Date, Date>, MsgInfo_OADRDistributeEvent.Event> elem : sortedPUTMessages){
                MsgInfo_OADRDistributeEvent.Event event = elem.getSnd();
                if(event.getEventID().equals(eventID)){
                    return i;
                }
                i++;
            }
        }

        return -1;
    }

    private synchronized Pair<Pair<Date, Date>, MsgInfo_OADRDistributeEvent.Event> removeOpenADREvent(String eventID){
        String serviceName = eventsTyp.get(eventID);
        if(serviceName!=null){
            SortedDateIntervalList<MsgInfo_OADRDistributeEvent.Event> sortedPUTMessages = serviceSortedReceivedEvents.get(serviceName);
            int index = getOpenADREventIndex(serviceName, eventID);
            if(index >= 0){
                return sortedPUTMessages.remove(index);
            }
        }
        return null;
    }

    public synchronized Pair<Pair<Date, Date>, MsgInfo_OADRDistributeEvent.Event> getOpenADREvent(String eventID){
        String serviceName = eventsTyp.get(eventID);
        SortedDateIntervalList<MsgInfo_OADRDistributeEvent.Event> sortedPUTMessages = serviceSortedReceivedEvents.get(serviceName);
        int index = getOpenADREventIndex(serviceName, eventID);
        if(index >= 0){
            return sortedPUTMessages.get(index);
        } else {
            return null;
        }
    }

    public synchronized void addOpenADREvent(String serviceName, Pair<Date, Date> timeInterval, MsgInfo_OADRDistributeEvent.Event event) {
        SortedDateIntervalList<MsgInfo_OADRDistributeEvent.Event> sortedPUTMessages = serviceSortedReceivedEvents.get(serviceName);

        if(sortedPUTMessages==null){
            sortedPUTMessages = new SortedDateIntervalList<>();

            serviceSortedReceivedEvents.put(serviceName, sortedPUTMessages);
        }

        Pair<Pair<Date, Date>, MsgInfo_OADRDistributeEvent.Event> oldEvent = removeOpenADREvent(event.getEventID());

        sortedPUTMessages.add(new Pair<>(timeInterval, event));
        eventsTyp.put(event.getEventID(), serviceName);
    }

    public OADR2VEN getOadrVEN() {
        return oadrVEN;
    }

    public ColibriClient getColClient() {
        return colClient;
    }
}
