/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.connector;

import com.colibri.Header.ContentType;
import com.colibri.Header.Header;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    static String dre = "";
    private static Socket socket = new Socket();
    static String reconnect = "temp";
    public static volatile boolean running = true;
    public static String temperature = "";
    public static String light = "";
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
        String msg;
        msg = Identifier.REG + "<br>Content-Type:" + ContentType.APPLICATION_RDF_XML + "<br>Message-Id:"+ Header.getId() +"<br>Date:" + Header.getDate() + "<br><br>";
        msg += v.get("reg.xml", "", "");
        dre = "dre";
        con.sendMessage(msg);
        while (true)
        {
            message = br.readLine();
            if (message.equalsIgnoreCase("ADDT"))
            {
                String msgs;
                msgs = Identifier.ADD + "<br>Content-Type:" + ContentType.APPLICATION_RDF_XML + "<br>Message-Id:"+ Header.getId() +"<br>Date:" + Header.getDate() + "<br><br>";
                msgs += v.get("addtemp.xml", "", "");
                con.sendMessage(msgs);
            }
            if (message.equalsIgnoreCase("ADDL"))
            {
                String msgs;
                msgs = Identifier.ADD + "<br>Content-Type:" + ContentType.APPLICATION_RDF_XML + "<br>Message-Id:"+ Header.getId() +"<br>Date:" + Header.getDate() + "<br><br>";
                msgs += v.get("addlight.xml", "", "");
                con.sendMessage(msgs);
            }
            if (message.equalsIgnoreCase("QUE"))
            {
                String msgs;
                msgs = Identifier.QUE + "<br>Content-Type: " + ContentType.APPLICATION_SPARQL + "<br>Message-Id: random25<br><br>";
                msgs += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#><br>PREFIX owl: <http://www.w3.org/2002/07/owl#><br>PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#><br>PREFIX xsd: <http://www.w3.org/2001/XMLSchema#><br>PREFIX colibri: <https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#><br>SELECT ?service ?identifier<br>WHERE { ?service rdf:type colibri:DataService.<br>?service colibri:hasDataConfiguration ?y.<br>?y colibri:hasParameter ?z.<br>?z rdf:type colibri:TemperatureParameter.<br>?service colibri:identifier ?identifier}";
                con.sendMessage(msgs);
            }
            if (message.equalsIgnoreCase("UPD"))
            {
                String msgs;
                msgs = Identifier.UPD + "<br>Content-Type: " + ContentType.APPLICATION_SPARQL + "<br>Message-Id: random27<br><br>";
                msgs += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#><br>PREFIX colibri: <https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#><br>INSERT DATA { <http://www.colibri-samples.org/main-building> rdf:type<br>colibri:OfficeBuilding }";
                con.sendMessage(msgs);
            }
            if (message.equalsIgnoreCase("REG") && dre.equals("") && reconnect.equals("temp"))
            {
                String msgs;
                msgs = Identifier.REG + "<br>Content-Type:" + ContentType.APPLICATION_RDF_XML + "<br>Message-Id:"+ Header.getId() +"<br>Date:" + Header.getDate() + "<br><br>";
                msgs += v.get("reg.xml", "", "");
                dre = "dre";
                con.sendMessage(msgs);
            }
            if (message.equalsIgnoreCase("REG") && reconnect.equals(""))
            {
                Connector c = new Connector();
                String msgs;
                msgs = Identifier.REG + "<br>Content-Type:" + ContentType.APPLICATION_RDF_XML + "<br>Message-Id:"+ Header.getId() +"<br>Date:" + Header.getDate() + "<br><br>";
                msgs += v.get("reg.xml", "", "");
                reconnect = "temp";
                con.sendMessage(msgs);
            }
        }
    }
    public static String gettemp(String token) throws IOException, URISyntaxException, DeploymentException, SAXException, ParserConfigurationException
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
        msg = Identifier.PUT+"<br>Message-Id:"+ Header.getId() +"<br>Reference-Id:" + token + "<br>Content-Type: "+ContentType.APPLICATION_RDF_XML+"<br>Date:" + Header.getDate() +"<br><br>";
        return msg + v.get("puttemp.xml", number, dateFormat.format(date).toString()) + "<br><br>";
    }

    static String getlight(String token) throws IOException, SAXException, ParserConfigurationException {
        Validate v = new Validate();
        OutputStream os1 = socket.getOutputStream();
        OutputStreamWriter osw1 = new OutputStreamWriter(os1);
        BufferedWriter bw1 = new BufferedWriter(osw1);
	bw1.write("4"+"\n");
	bw1.flush();
	InputStream is = socket.getInputStream();
	InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String number = br.readLine();
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
        String msg;
        msg = Identifier.PUT+"<br>Message-Id:"+ Header.getId() +"<br>Reference-Id:" + token + "<br>Content-Type: "+ContentType.APPLICATION_RDF_XML+"<br>Date:" + Header.getDate() +"<br>";
        return msg + v.get("putlight.xml", number, dateFormat.format(date)) + "<br><br>";
    }

    static String obstemp(String token) throws IOException, SAXException, ParserConfigurationException, URISyntaxException, DeploymentException {
	running = true;
        Validate v = new Validate();
        ObsTempThread(token);
	return "";
    }

    static String obslight(String token) throws IOException, SAXException, ParserConfigurationException, URISyntaxException, DeploymentException {
	running = true;
        Validate v = new Validate();
        ObsLightThread(token);
        return "";
        
    }
    public static void ObsTempThread(final String token) throws URISyntaxException, DeploymentException, IOException
    {
        final Connector con = new Connector();
        final Validate v = new Validate();
        Timer t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                try {
                    OutputStream os1 = socket.getOutputStream();
                    OutputStreamWriter osw1 = new OutputStreamWriter(os1);
                    BufferedWriter bw1 = new BufferedWriter(osw1);
                    bw1.write("1"+"\n");
                    bw1.flush();
                    InputStream is = socket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String number;
                    number = br.readLine();
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    Date date = new Date();
                    String msg;
		    if (!temperature.equalsIgnoreCase(number))
		    {
                    msg = Identifier.PUT+"<br>Message-Id:"+ Header.getId() +"<br>Reference-Id:" + token + "<br>Content-Type: "+ContentType.APPLICATION_RDF_XML+"<br>Date:" + Header.getDate() +"<br>";
                    msg = msg + v.get("puttemp.xml", number, dateFormat.format(date).toString()) + "<br><br>";
                    con.sendMessage(msg);
		    temperature = number;
		    }
                } catch (SAXException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        };
        while (running)
	{
		tt.run();
		try {
                Thread.sleep(15000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
	}
}

    static void dre() throws URISyntaxException, DeploymentException, IOException {
        dre = "";
        reconnect = "";
    }

    static void ObsLightThread(final String token) throws URISyntaxException, DeploymentException, IOException {
        final Connector con = new Connector();
        final Validate v = new Validate();
        Timer t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                try {
                        OutputStream os1 = socket.getOutputStream();
                        OutputStreamWriter osw1 = new OutputStreamWriter(os1);
                        BufferedWriter bw1 = new BufferedWriter(osw1);
                        bw1.write("4"+"\n");
                        bw1.flush();
                        InputStream is = socket.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        String number = br.readLine();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        String msg;
			if (!light.equalsIgnoreCase(number))
			{
                        msg = Identifier.PUT+"<br>Message-Id:"+ Header.getId() +"<br>Reference-Id:" + token + "<br>Content-Type: "+ContentType.APPLICATION_RDF_XML+"<br>Date:" + Header.getDate() +"<br>";
                        msg =  msg + v.get("putlight.xml", number, dateFormat.format(date)) + "<br><br>";
                        con.sendMessage(msg);
			light = number;
			}
                    } catch (SAXException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ParserConfigurationException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
        };
        while (running)
	{
		tt.run();
		try {
                Thread.sleep(15000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
	}
    }
	static void terminate()
	{
		running = false;	
	}
	static void sendPut(String message) throws IOException, URISyntaxException, ParserConfigurationException, DeploymentException, SAXException
	{
		if (message.equalsIgnoreCase("true"))
		{
			Validate v = new Validate();
			Connector con = new Connector();
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        String msg;
                        msg = Identifier.PUT+"<br>Message-Id:"+ Header.getId() + "<br>Content-Type: "+ContentType.APPLICATION_RDF_XML+"<br>Date:" + Header.getDate() +"<br>";
                        msg =  msg + v.get("puton.xml", "", "") + "<br><br>";
                        con.sendMessage(msg);
			OutputStream os1 = socket.getOutputStream();
                        OutputStreamWriter osw1 = new OutputStreamWriter(os1);
                        BufferedWriter bw1 = new BufferedWriter(osw1);
                        bw1.write("2"+"\n");
                        bw1.flush();
		}
		if (message.equalsIgnoreCase("false"))
		{
			Validate v = new Validate();
			Connector con = new Connector();
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        String msg;
                        msg = Identifier.PUT+"<br>Message-Id:"+ Header.getId() + "<br>Content-Type: "+ContentType.APPLICATION_RDF_XML+"<br>Date:" + Header.getDate() +"<br>";
                        msg =  msg + v.get("putoff.xml", "", "") + "<br><br>";
                        con.sendMessage(msg);
			OutputStream os1 = socket.getOutputStream();
                        OutputStreamWriter osw1 = new OutputStreamWriter(os1);
                        BufferedWriter bw1 = new BufferedWriter(osw1);
                        bw1.write("3"+"\n");
                        bw1.flush();
		}
	}
}
