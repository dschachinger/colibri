package com.colibri.Connector;

/**
 * Created by codelife on 12/8/16.
 */
import org.xml.sax.SAXException;

import javax.json.Json;
import javax.websocket.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author codelife
 */


/**
 * ChatServer Client
 *
 * @author codelife
 */
@ClientEndpoint
public class Connector {
    Session session;
    String abc = "";
    public Connector() throws URISyntaxException, DeploymentException, IOException
    {
        URI uri = new URI("ws://localhost:8080/Col-1.0/chat");
        ContainerProvider.getWebSocketContainer().connectToServer(this, uri);

    }
    @OnOpen
    public void processOpen (Session session) throws IOException
    {
        this.session = session;
    }
    @OnMessage
    public void processMessage (String message) throws IOException, URISyntaxException, DeploymentException, SAXException, ParserConfigurationException
    {
        String[] lines = message.split("<br>");
        for(String ss:lines)
        {
            if (Json.createReader(new StringReader(ss)).readObject().getString("message").equalsIgnoreCase("GETL"))
            {
                abc = Client.getlight(ss);
                sendMessage(abc);
            }
            if (Json.createReader(new StringReader(ss)).readObject().getString("message").equalsIgnoreCase("GETT"))
            {
                abc = Client.gettemp(ss);
                sendMessage(abc);
            }
            if (Json.createReader(new StringReader(ss)).readObject().getString("message").equalsIgnoreCase("OBST"))
            {
                abc = Client.obstemp(ss);
                sendMessage(abc);
            }
            if (Json.createReader(new StringReader(ss)).readObject().getString("message").equalsIgnoreCase("OBSL"))
            {
                abc = Client.obslight(ss);
                sendMessage(abc);
            }
            if (Json.createReader(new StringReader(ss)).readObject().getString("message").equalsIgnoreCase("DRE"))
            {
                Client.dre(ss);
            }
            System.out.println(Json.createReader(new StringReader(ss)).readObject().getString("message"));
        }
    }
    public void sendMessage(String message) throws IOException
    {
        session.getBasicRemote().sendText(message);
    }
}
