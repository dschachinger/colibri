package com.colibri.server;
//This class is the demo server backend which handles the incoming and outgoing messages
import com.colibri.Header.ContentType;
import com.colibri.Header.Header;
import com.colibri.Header.Identifier;
import com.colibri.Header.StatusCodes;
import com.colibri.Validator.Validate;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author codelife
 */
@ServerEndpoint("/chat")
public class Colibri {
    static String sta = "";
    static String temp = "";
    static String addt = "";
    static String obst = "";
    static String addl = "";
    static String obsl = "";
    static String tc = "";
    static String que = "";
    static String qre = "";
    static String put = "";
    static String get = "";
    static String id = "";
    static String err = "";
    static Session conId;
    Validate v = new Validate();
    static Set<Session> chatUsers = Collections.synchronizedSet(new HashSet<Session>());
    @OnOpen
    public void handleOpen (Session userSession) throws IOException
    {
        chatUsers.add(userSession);
        conId = userSession;
    }
    @OnMessage
    public void handleMessage (String message, Session userSession) throws IOException
    {
        if (message.equalsIgnoreCase("DRE")) // DRE stands for deregistering the connector
        {
            if (tc.equalsIgnoreCase("TC")) // This checks if a connector is registered
            {
                id = Header.getId();
                Header.setId(id);
                String msg;
                msg = Identifier.DRE + "<br>Content-Type:" + ContentType.TEXT_PLAIN + "<br>Message-Id: " + id + "<br>Date:"+ Header.getDate() +"<br><br>http://www.colibri-samples.org/TC"+ "<br><br>";
                breakStatus(msg);
                tc = "";
                chatUsers.remove(conId);
                get = "";
                put = "";
            }
            else
            {
                breakStatus("Error: Cannot Deregister the connector as Technology Connecter is not registered");
                err = "err";
            }
        }
        else if (message.equalsIgnoreCase("REMT")) // REMT stands for removing the temperature service
        {
            if (tc.equalsIgnoreCase("TC")) // This checks if a connector is registered
            {
                id = Header.getId();
                Header.setId(id);
                String msg;
                msg = Identifier.REM + "<br>Content-Type:" + ContentType.TEXT_PLAIN + "<br>Message-Id: " + id + "<br>Date:"+ Header.getDate() +"<br><br>http://www.colibri-samples.org/temperatureservice"+ "<br><br>";
                if (addt.equalsIgnoreCase("addt")) // This checks if a temperature service is added
                {
                    breakStatus(msg);
                    addt = "";
                    sta = "";
                    get = "";
                    put = "";
                }
                else
                {
                    breakStatus("Error: Cannot remove service as service is not added");
                    err = "err";
                }
            }
            else
            {
                breakStatus("Error: Cannot Remove service as Technology Connecter is not registered");
                err = "err";
            }
        }
        else if (message.equalsIgnoreCase("OBST")) // OBST stands for observing the temperature service
        {
            if (tc.equalsIgnoreCase("TC")) // This checks if a connector is registered
            {
                id = Header.getId();
                Header.setId(id);
                String msg;
                msg = "OBST<br>" + Identifier.OBS + "<br>Content-Type:" + ContentType.TEXT_PLAIN + "<br>Message-Id: " + id + "<br>Date:"+ Header.getDate() +"<br><br>http://www.colibri-samples.org/temperatureservice"+ "<br><br>";
                if (addt.equalsIgnoreCase("addt")) // This checks if a temperature service is added
                {
                    breakStatus(msg);
                    obst = "obst";
                    sta = "";
                    get = "";
                    put = "";
                }
                else
                {
                    breakStatus("Error: Cannot observe service as service is not added");
                    err = "err";
                }
            }
            else
            {
                breakStatus("Error: Cannot Observe service as Technology Connecter is not registered");
                err = "err";
            }
        }
        else if (message.equalsIgnoreCase("DETT")) // DETT stands for dettaching the temperature service
        {
            if (tc.equalsIgnoreCase("TC")) // This checks if a connector is registered
            {
                id = Header.getId();
                Header.setId(id);
                String msg;
                msg = "DETT<br>" + Identifier.DET + "<br>Content-Type:" + ContentType.TEXT_PLAIN + "<br>Message-Id: " + id + "<br>Date:"+ Header.getDate() +"<br><br>http://www.colibri-samples.org/temperatureservice"+ "<br><br>";
                if (obst.equalsIgnoreCase("obst") && addt.equalsIgnoreCase("addt")) // This checks if a temperature service is added and observed
                {
                    breakStatus(msg);
                    obst = "";
                    sta = "";
                    get = "";
                    put = "";
                }
                else
                {
                    breakStatus("Error: Cannot dettach service as service is not observed");
                    err = "err";
                }
            }
            else
            {
                breakStatus("Error: Cannot Dettach service as Technology Connecter is not registered");
                err = "err";
            }
        }
        else if (message.equalsIgnoreCase("GETT")) // GETT stands for getting the temperature value
        {
            if (tc.contentEquals("TC")) // This checks if a connector is registered
            {
                if (addt.equalsIgnoreCase("addt")) // This checks if a temperature service is added
                {
                    id = Header.getId();
                    Header.setId(id);
                    breakStatus("GETT<br>GET" + "<br>Content-Type:" + ContentType.TEXT_PLAIN + "<br>Message-Id: " + id + "<br>Date:"+ Header.getDate() +"<br>http://www.colibri-samples.org/temperatureservice"+ "<br><br>");
                    obst = "";
                    sta = "";
                    get = "get";
                    put = "";
                }
                else
                {
                    breakStatus("Error: Cannot get the service as service is not added");
                    err = "err";
                }
            }
            else
            {
                breakStatus("Error: Cannot Get the service as Technology Connecter is not registered");
                err = "err";
            }
        }
        else if (message.equalsIgnoreCase("REML")) // REML stands for removing the light service
        {
            if (tc.equalsIgnoreCase("TC")) // This checks if a connector is registered
            {
                id = Header.getId();
                Header.setId(id);
                String msg;
                msg = Identifier.REM + "<br>Content-Type:" + ContentType.TEXT_PLAIN + "<br>Message-Id: " + id + "<br>Date:"+ Header.getDate() +"<br><br>http://www.colibri-samples.org/lightservice"+ "<br>";
                if (addl.equalsIgnoreCase("addl")) // This checks if a light service is added
                {
                    breakStatus(msg);
                    addl = "";
                    sta = "";
                    get = "";
                    put = "";
                }
                else
                {
                    breakStatus("Error: Cannot remove service as service is not added");
                    err = "err";
                }
            }
            else
            {
                breakStatus("Error: Cannot Remove service as Technology Connecter is not registered");
                err = "err";
            }
        }
        else if (message.equalsIgnoreCase("OBSL")) //OBSL stands for observing the light service
        {
            if (tc.equalsIgnoreCase("TC")) // This checks if a connector is registered
            {
                id = Header.getId();
                Header.setId(id);
                String msg;
                msg = "OBSL<br>" + Identifier.OBS + "<br>Content-Type:" + ContentType.TEXT_PLAIN + "<br>Message-Id: " + id + "<br>Date:"+ Header.getDate() +"<br><br>http://www.colibri-samples.org/lightservice"+ "<br>";
                if (addl.equalsIgnoreCase("addl")) // This checks if a light service is added
                {
                    breakStatus(msg);
                    obsl = "obsl";
                    sta = "";
                    get = "";
                    put = "";
                }
                else
                {
                    breakStatus("Error: Cannot observe service as service is not added");
                    err = "err";
                }
            }
            else
            {
                breakStatus("Error: Cannot Observe service as Technology Connecter is not registered");
                err = "err";
            }
        }
        else if (message.equalsIgnoreCase("DETL")) // DETL stands for dettaching the light service
        {
            if (tc.equalsIgnoreCase("TC")) // This checks if a connector is registered
            {
                id = Header.getId();
                Header.setId(id);
                String msg;
                msg = "DETL" + Identifier.DET + "<br>Content-Type:" + ContentType.TEXT_PLAIN + "<br>Message-Id: " + id + "<br>Date:"+ Header.getDate() +"<br><br>http://www.colibri-samples.org/lightservice"+ "<br><br>";
                if (obsl.equalsIgnoreCase("obsl") && addl.equalsIgnoreCase("addl")) // This checks if a light service is added and observed
                {
                    breakStatus(msg);
                    obsl = "";
                    sta = "";
                    get = "";
                    put = "";
                }
                else
                {
                    breakStatus("Error: Cannot dettach service as service is not observed");
                    err = "err";
                }
            }
            else
            {
                breakStatus("Error: Cannot Dettach service as Technology Connecter is not registered");
                err = "err";
            }
        }
        else if (message.equalsIgnoreCase("GETL")) // GETL stands for getting the light value
        {
            if (tc.equalsIgnoreCase("TC")) // This checks if a connector is registered
            {
                if (addl.equalsIgnoreCase("addl")) // This checks if a light service is added
                {
                    id = Header.getId();
                    Header.setId(id);
                    breakStatus("GETL<br>GET" + "<br>Content-Type:" + ContentType.TEXT_PLAIN + "<br>Message-Id: " + id + "<br>Date:"+ Header.getDate() +"<br><br>http://www.colibri-samples.org/lightservice"+ "<br><br>");
                    obsl = "";
                    sta = "";
                    get = "get";
                    put = "";
                }
                else
                {
                    breakStatus("Error: Cannot get the service as service is not added");
                    err = "err";
                }
            }
            else
            {
                breakStatus("Error: Cannot Get the service as Technology Connecter is not registered");
                err = "err";
            }
        }
	else if (message.equalsIgnoreCase("PUTON")) // PUTON stands for PUT message to ON the light
        {
            if (tc.equalsIgnoreCase("TC")) // This checks if a connector is registered
            {
		if (addl.equalsIgnoreCase("addl")) // This checks if a light service is added
                {
		breakStatus("PUTON");
		}
		else
		{
			breakStatus("Error: Cannot put message as service is not added");
			err = "err";
		}
            }
            else
            {
                breakStatus("Error: Cannot Put the message as Technology Connecter is not registered");
                err = "err";
            }
        }
	else if (message.equalsIgnoreCase("PUTOFF")) // PUTOFF stands for PUT message to off the light
        {
            if (tc.equalsIgnoreCase("TC")) // This checks if a connector is registered
            {
		if (addl.equalsIgnoreCase("addl")) // This checks if a light service is added
                {
		breakStatus("PUTOFF");
		}
		else
		{
			breakStatus("Error: Cannot put message as service is not added");
			err = "err";
		}
            }
            else
            {
                breakStatus("Error: Cannot put message as Technology Connecter is not registered");
                err = "err";
            }
        }
        else if (message.equalsIgnoreCase("QRE")) // QRE stands for query string
        {
            if (tc.equalsIgnoreCase("TC")) // This checks if a connector is registered
            {
                if (que.equalsIgnoreCase("QUE") && qre.equalsIgnoreCase("")) // This checks if a query has been received and result is to be sent
                {
                    String msg;
                    msg = Identifier.QRE + "<br>Content-Type: " + ContentType.APPLICATION_RESULT + "<br>Message-Id: random26<br>Reference-Id: random25<br>";
                    msg += "{<br>\"head\": { \"vars\": [\"service\"] },<br>\"results\": {<br>\"bindings\": [<br>{<br>\"service\" : { \"type\": \"uri\", \"value\": \"http://www.colibri-samples.org/service1\" },<br>\"identifier\" : { \"type\": \"literal\", \"value\": \"temp_monitoring_17\" }<br>}<br>]<br>}<br>}<br><br>";
                    breakStatus(msg);
                }
                else
                {
                    breakStatus("Error: Result without query");
                    err = "err";
                }
            }
            else
            {
                breakStatus("Error: Cannot send QRE result as Technology Connecter is not registered");
                err = "err";
            }
        }
        else
        {
        String[] lines = message.split("<br>");
            for(String ss:lines)
            {
                if (ss.equals("REG"))
                {
                    temp = "REG";
                    tc = "TC";
                }
                if (ss.equals("PUT"))
                {
                    put = "put";
                }
                if (ss.equals("QUE"))
                {
                    que = "que";
                }
                if (ss.contains("temperatureservice") && addt.equalsIgnoreCase(""))
                    addt = "addt";
                if (ss.contains("lightservice") && addl.equalsIgnoreCase(""))
                    addl = "addl";
                Iterator<Session> iterator = chatUsers.iterator();
                while (iterator.hasNext())
                    iterator.next().getBasicRemote().sendText(buildJSONData(ss));
                if (ss.contains("Message-Id:"))
                {
                    StringTokenizer st = new StringTokenizer(ss, ":");
                    while (st.hasMoreTokens())
                        id = st.nextToken();
                }
            }
        }
        if (!id.equals("") && !err.equals("err") && (!get.equals("get") || put.equals("put") ))
        {
            String status = sta(id);
            breakStatus(status);
        }
    }
    @OnClose
    public void handleClose (Session userSession)
    {
        chatUsers.remove(userSession);
    }


// Message is sent in the form of the JSON data
    public String buildJSONData(String message) {
        JsonObject jsonObject = Json.createObjectBuilder().add("message", message).build();
        StringWriter sw = new StringWriter();
        try (JsonWriter jw = Json.createWriter(sw)) {jw.write(jsonObject);}
        return sw.toString();
    }

// breakStatus sends the message to the clients
    private void breakStatus(String message) throws IOException {
        String[] lines = message.split("<br>");
        for(String ss:lines)
        {
            Iterator<Session> iterator = chatUsers.iterator();
            while (iterator.hasNext())
            {
                iterator.next().getBasicRemote().sendText(buildJSONData(ss));
            }
        }
    }

// sta sends the status message
    private String sta(String msg_id) {
        String msg;
        id = "";
        err = "";
        msg = Identifier.STA + "<br>Content-Type:" + ContentType.TEXT_PLAIN + "<br>Message-Id: " + Header.getId() + "<br>Reference-Id:" + msg_id + "<br>Date:" + Header.getDate() + "<br><br>" +StatusCodes.OK + "<br><br>";
        return msg;
    }
}
