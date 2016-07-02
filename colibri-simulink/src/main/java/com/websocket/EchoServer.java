package com.websocket;

/**
 *
 * @author codelife
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * @ServerEndpoint gives the relative name for the end point
 * This will be accessed via ws://localhost:8080/EchoChamber/echo
 * Where "localhost" is the address of the host,
 * "EchoChamber" is the name of the package
 * and "echo" is the address to access this class from the server
 */
@ServerEndpoint("/echo")
public class EchoServer {
    public static Socket socket;
    public static BufferedWriter bw;
    public static BufferedReader br;
    public static OutputStream os;
    public static OutputStreamWriter osw;
    public static InputStream is;
    public static InputStreamReader isr;

    /**
     * @OnOpen allows us to intercept the creation of a new session.
     * The session class allows us to send data to the user.
     * In the method onOpen, we'll let the user know that the handshake was
     * successful.
     */
    @OnOpen
    public void onOpen(Session session) {
        System.out.println(session.getId() + " has opened a connection");
        try {
            try {
                int port = 7777;
                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println("Server Started and listening to the port 7777");
                socket = serverSocket.accept();
                System.out.println("Client connected");
                os = socket.getOutputStream();
                osw = new OutputStreamWriter(os);
                bw = new BufferedWriter(osw);

                is = socket.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);


            } catch (Exception e) {
                e.printStackTrace();
            }
            session.getBasicRemote().sendText("Connection Established");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * When a user sends a message to the server, this method will intercept the message
     * and allow us to react to it. For now the message is read as a String.
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        String msg = "";
        System.out.println("Message from " + session.getId() + ": " + message);
        try {
            if (message.equalsIgnoreCase("get"))
                msg = String.valueOf(1);
            else if (message.equalsIgnoreCase("on"))
                msg = String.valueOf(2);
            else
                msg = String.valueOf(3);
            bw.write(msg + "\n");
            bw.flush();
            String number = br.readLine();
            session.getBasicRemote().sendText(number);
        } catch (Exception e) {

        }
    }

    /**
     * The user closes the connection.
     * <p>
     * Note: you can't send messages to the client from this method
     */
    @OnClose
    public void onClose(Session session) {
        try {
            socket.close();
        } catch (Exception e) {
        }
        System.out.println("Session " + session.getId() + " has ended");
    }
}
