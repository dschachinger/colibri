package semanticCore.del;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class FakeSocketEndpoint {

    private BufferedReader in;
    private PrintWriter out;

    public FakeSocketEndpoint() {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            Socket endpointSocket = serverSocket.accept();
            in = new BufferedReader(new InputStreamReader(endpointSocket.getInputStream()));
            out = new PrintWriter(endpointSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() throws IOException {
        String s = "";
        while ((s = in.readLine()) != null) {
            System.out.println("Got " + s);
            out.write( "REG\n" +
                    "Message-Id: 7871ixv\n" +
                    "Content-Type: text/plain\n" +
                    "Date: 2016-06-28T09:48:53Z\n" +
                    "Expires: 2016-06-28T09:48:53Z\n" +
                    "Reference-Id: 7871ixv\n" +
                    "\n" +
                    "hero");
            out.flush();
        }
    }

    public static void main(String[] args) {
        FakeSocketEndpoint endpoint = new FakeSocketEndpoint();
        try {
            endpoint.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}