package semanticCore.del;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Class to transmit messages over tcp
 */
public class TCPChannel implements Channel {

    private Socket socket;

    private BufferedReader serverReader;

    private PrintWriter serverWriter;

    /**
     * Initialize an TCPChannel object with the given socket.
     * @param socket
     */
    public TCPChannel(Socket socket){
        this.socket = socket;

        // create a reader to retrieve messages send by the server
        try {
            serverReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            // create a writer to send messages to the server
            serverWriter = new PrintWriter(
                    socket.getOutputStream(), true);
        } catch (IOException e) {
            return;
        }
    }

    /**
     * This method sends the given message over TCP.
     * @param msg Message
     */
    @Override
    public void sendMessage(byte[] msg) {
        // write provided user input to the socket
        String buffer = new String(msg);
        serverWriter.println(buffer);
    }

    /**
     * This method returns a received TCP message.
     * @return
     */
    @Override
    public byte[] receiveMessage() {
        try {
            String buffer = serverReader.readLine();
            byte[] out = buffer!=null ? buffer.getBytes() : null;
            return out;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * This method closes the tcp socket.
     */
    @Override
    public void close() {
        if (socket != null && !socket.isClosed())
            try {
                socket.close();
            } catch (IOException e) {
                // Ignored because we cannot handle it
            }
    }


}
