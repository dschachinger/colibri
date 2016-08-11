package com.colibri.Connector;

/**
 * Created by codelife on 12/8/16.
 */
import com.colibri.Header.ContentType;
import com.colibri.Header.Identifier;
import com.colibri.Validator.Validate;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.websocket.DeploymentException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author codelife
 */
public class Client {
    static ServerSocket serverSocket;
    static int port = 8888;
    private static Socket socket = new Socket();
    public static void main(String args[]) throws URISyntaxException, DeploymentException, IOException, SAXException, ParserConfigurationException
    {
        Connector con = new Connector();
        Validate v = new Validate();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String message = null;
        serverSocket = new ServerSocket(port);
        System.out.println("Server Started and listening to the port 8888");
        socket = serverSocket.accept();
        System.out.println("MATLAB Connected");
        //System.out.println("Please enter your username");
        //message = br.readLine();
        //con.sendMessage(message);
        String msg;
        msg = Identifier.REG + "<br>Content-Type: " + ContentType.APPLICATION_RDF_XML + "<br>Message-Id: random1<br>";
        msg += v.get("reg.xml", "", "");
        con.sendMessage(msg);
        while (true)
        {
            message = br.readLine();
            if (message.equalsIgnoreCase("ADDT"))
            {
                String msgs;
                msgs = Identifier.ADD + "<br>Content-Type: " + ContentType.APPLICATION_RDF_XML + "<br>Message-Id: random8<br>";
                msgs += v.get("addtemp.xml", "", "");
                con.sendMessage(msgs);
            }
            if (message.equalsIgnoreCase("ADDL"))
            {
                String msgs;
                msgs = Identifier.ADD + "<br>Content-Type: " + ContentType.APPLICATION_RDF_XML + "<br>Message-Id: random8<br>";
                msgs += v.get("addlight.xml", "", "");
                con.sendMessage(msgs);
            }
            if (message.equalsIgnoreCase("QUE"))
            {
                String msgs;
                msgs = Identifier.QUE + "<br>Content-Type: " + ContentType.APPLICATION_SPARQL + "<br>Message-Id: random25<br>";
                msgs += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#><br>PREFIX owl: <http://www.w3.org/2002/07/owl#><br>PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#><br>PREFIX xsd: <http://www.w3.org/2001/XMLSchema#><br>PREFIX colibri: <https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#><br>SELECT ?service ?identifier<br>WHERE { ?service rdf:type colibri:DataService.<br>?service colibri:hasDataConfiguration ?y.<br>?y colibri:hasParameter ?z.<br>?z rdf:type colibri:TemperatureParameter.<br>?service colibri:identifier ?identifier}";
                con.sendMessage(msgs);
            }
            if (message.equalsIgnoreCase("UPD"))
            {
                String msgs;
                msgs = Identifier.UPD + "<br>Content-Type: " + ContentType.APPLICATION_SPARQL + "<br>Message-Id: random27<br>";
                msgs += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#><br>PREFIX colibri: <https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#><br>INSERT DATA { <http://www.colibri-samples.org/main-building> rdf:type<br>colibri:OfficeBuilding }";
                con.sendMessage(msgs);
            }
            //message = br.readLine();
            //con.sendMessage(message);
        }
    }
    public static String gettemp(String message) throws IOException, URISyntaxException, DeploymentException, SAXException, ParserConfigurationException
    {
        Validate v = new Validate();
        OutputStream os1 = socket.getOutputStream();
        OutputStreamWriter osw1 = new OutputStreamWriter(os1);
        BufferedWriter bw1 = new BufferedWriter(osw1);
        bw1.write("1"+"\n");
        System.out.println("Sent 1");
        bw1.flush();
        InputStream is = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String number = br.readLine();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String msg;
        msg = Identifier.PUT+"<br>Message-Id: random13<br>Reference-Id: random12<br>Content-Type: "+ContentType.TEXT_PLAIN+"<br>";
        return msg + v.get("puttemp.xml", number, dateFormat.format(date).toString());
    }

    static String getlight(String ss) throws IOException, SAXException, ParserConfigurationException {
        Validate v = new Validate();
        OutputStream os1 = socket.getOutputStream();
        OutputStreamWriter osw1 = new OutputStreamWriter(os1);
        BufferedWriter bw1 = new BufferedWriter(osw1);
        bw1.write("2"+"\n");
        System.out.println("Sent 1");
        bw1.flush();
        InputStream is = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String number = br.readLine();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String msg;
        msg = Identifier.PUT+"<br>Message-Id: random18<br>Reference-Id: random17<br>Content-Type: "+ContentType.TEXT_PLAIN+"<br>";
        return msg + v.get("putlight.xml", number, dateFormat.format(date));
    }

    static String obstemp(String ss) throws IOException, SAXException, ParserConfigurationException {
        Validate v = new Validate();
        OutputStream os1 = socket.getOutputStream();
        OutputStreamWriter osw1 = new OutputStreamWriter(os1);
        BufferedWriter bw1 = new BufferedWriter(osw1);
        bw1.write("1"+"\n");
        System.out.println("Sent 1");
        bw1.flush();
        InputStream is = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String number = br.readLine();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String msg;
        msg = Identifier.PUT+"<br>Message-Id: random13<br>Reference-Id: random12<br>Content-Type: "+ContentType.TEXT_PLAIN+"<br>";
        return msg + v.get("puttemp.xml", number, dateFormat.format(date).toString());
    }

    static String obslight(String ss) throws IOException, SAXException, ParserConfigurationException {
        Validate v = new Validate();
        OutputStream os1 = socket.getOutputStream();
        OutputStreamWriter osw1 = new OutputStreamWriter(os1);
        BufferedWriter bw1 = new BufferedWriter(osw1);
        bw1.write("2"+"\n");
        System.out.println("Sent 1");
        bw1.flush();
        InputStream is = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String number = br.readLine();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String msg;
        msg = Identifier.PUT+"<br>Message-Id: random18<br>Reference-Id: random17<br>Content-Type: "+ContentType.TEXT_PLAIN+"<br>";
        return msg + v.get("putlight.xml", number, dateFormat.format(date));
    }

    static void dre(String ss) throws IOException {
        socket.close();
    }
}
