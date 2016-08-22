package com.connector;

// This class is the connector backend to handle the messages from the demo server

import com.colibri.Header.Header;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;
import javax.json.Json;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author codelife
 */
@ClientEndpoint // This line shows that Connector acts as the ClientEndpoint for the demo server
public class Connector {
    Session session;
    String abc = "";
    String token = "";
    int commandCode = 0;
    public Connector() throws URISyntaxException, DeploymentException, IOException // called from Client.java
    {
        URI uri = new URI("ws://localhost:8080/colibri-simulink-1.0/chat"); // connects to demo server using WebSocket communication
        ContainerProvider.getWebSocketContainer().connectToServer(this, uri);
    }
    @OnOpen
    public void processOpen (Session session) throws IOException
    {
        this.session = session;
    }
    @OnMessage
    public void processMessage (String message) throws IOException, URISyntaxException, DeploymentException, SAXException, ParserConfigurationException // This message handles the different types of message
    {
        String[] lines = message.split("<br>"); //Every line in the XML message is separated by <br> so when message is received consisting of <br> then we use split method to display the message without <br>
            for(String ss:lines)
            {
                if (Json.createReader(new StringReader(ss)).readObject().getString("message").equalsIgnoreCase("GETL"))
                {
                    commandCode = 1;
                }
                if (Json.createReader(new StringReader(ss)).readObject().getString("message").equalsIgnoreCase("DRE"))
                {
                    commandCode = 2;
                }
                if (Json.createReader(new StringReader(ss)).readObject().getString("message").equalsIgnoreCase("GETT"))
                {
                    commandCode = 3;
                }
                if (Json.createReader(new StringReader(ss)).readObject().getString("message").equalsIgnoreCase("OBST"))
                {
                    commandCode = 4;
                }
                if (Json.createReader(new StringReader(ss)).readObject().getString("message").equalsIgnoreCase("OBSL"))
                {
                    commandCode = 5;
                }
                if (Json.createReader(new StringReader(ss)).readObject().getString("message").equalsIgnoreCase("DETT"))
                {
                    commandCode = 6;
                }
                if (Json.createReader(new StringReader(ss)).readObject().getString("message").equalsIgnoreCase("DETL"))
                {
                    commandCode = 7;
                }
		if (Json.createReader(new StringReader(ss)).readObject().getString("message").equalsIgnoreCase("PUTON"))
                {
                    commandCode = 8;
                }
		if (Json.createReader(new StringReader(ss)).readObject().getString("message").equalsIgnoreCase("PUTOFF"))
                {
                    commandCode = 9;
                }
                if (Json.createReader(new StringReader(ss)).readObject().getString("message").contains("Message-Id:"))
                {
                    StringTokenizer st = new StringTokenizer(ss, ":");
                    while (st.hasMoreTokens())
                    {
                        token = st.nextToken();
                    }
                }
                System.out.println(Json.createReader(new StringReader(ss)).readObject().getString("message")); // Prints the message received
            }
        switch (commandCode) { // based upon the commandCode values, different functions are called in the connector class
            case 1:
                abc = Client.getlight(token);
                sendMessage(abc);
                commandCode = 0;
                break;
            case 2:
                Client.dre();
                commandCode = 0;
                break;
            case 3:
                abc = Client.gettemp(token);
                sendMessage(abc);
                commandCode = 0;
                break;
            case 4:
                abc = Client.obstemp(token);
                sendMessage(abc);
                commandCode = 0;
                break;
            case 5:
                abc = Client.obslight(token);
                sendMessage(abc);
                commandCode = 0;
                break;
            case 6:
                Client.terminate();
                commandCode = 0;
                break;
            case 7:
                Client.terminate();
                commandCode = 0;
                break;
	    case 8:
                Client.sendPut("true");
                commandCode = 0;
                break;
	    case 9:
                Client.sendPut("false");
                commandCode = 0;
                break;
            default:
                break;
        }
    }

    public void sendMessage(String message) throws IOException // sends the messages back to the demo server
    {
        session.getBasicRemote().sendText(message);
    }
}
