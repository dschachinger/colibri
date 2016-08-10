package semanticCore.WebSocketHandling;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.wasync.*;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.atmosphere.websocket.WebSocketStreamingHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semanticCore.MsgObj.ColibriMessage;

import java.io.IOException;
import java.util.Date;

/**
 * Created by georg on 29.06.16.
 * This static class is used to establish a websocket connection to the semantic core.
 */
public class InitWebsocket extends WebSocketStreamingHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(InitWebsocket.class);

    /**
     * This static class is used to establish a websocket connection from the given colibri connector to the semantic core.
     * @param colClient given colibri connector
     * @return socket object to communicate with the core
     * @throws IOException
     */
    public static Socket initWebSocket(final ColibriClient colClient) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        Client client;
        RequestBuilder request;

        if(colClient.isLocalAtmosphereClient()){
            client = ClientFactory.getDefault().newClient(AtmosphereClient.class);
            request = ((AtmosphereClient)client).newRequestBuilder()
                    .method(Request.METHOD.GET)
                    .uri(colClient.getColibriCoreURL())
                    .trackMessageLength(true)
                    .encoder(new Encoder<Message, String>() {
                        @Override
                        public String encode(Message data) {
                            try {
                                return mapper.writeValueAsString(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    })
                    .decoder(new Decoder<String, Message>() {
                        @Override
                        public Message decode(Event type, String data) {

                            data = data.trim();

                            // Padding from Atmosphere, skip
                            if (data.length() == 0) {
                                return null;
                            }

                            if (type.equals(Event.MESSAGE)) {
                                try {
                                    return mapper.readValue(data, Message.class);
                                } catch (IOException e) {
                                    logger.info("Invalid message {}", data);
                                    return null;
                                }
                            } else {
                                return null;
                            }
                        }
                    })
                    .transport(Request.TRANSPORT.WEBSOCKET)
                    .transport(Request.TRANSPORT.SSE)
                    .transport(Request.TRANSPORT.LONG_POLLING);
        } else {
            client = ClientFactory.getDefault().newClient();
            request = client.newRequestBuilder()
                    .method(Request.METHOD.GET)
                    .uri(colClient.getColibriCoreURL())
                    .encoder(new Encoder<Message, String>() {
                        @Override
                        public String encode(Message data) {
                            try {
                                return mapper.writeValueAsString(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    })
                    .decoder(new Decoder<String, Message>() {
                        @Override
                        public Message decode(Event type, String data) {

                            data = data.trim();

                            // Padding from Atmosphere, skip
                            if (data.length() == 0) {
                                return null;
                            }

                            if (type.equals(Event.MESSAGE)) {
                                try {
                                    return mapper.readValue(data, Message.class);
                                } catch (IOException e) {
                                    logger.info("Invalid message {}", data);
                                    return null;
                                }
                            } else {
                                return null;
                            }
                        }
                    })
                    .transport(Request.TRANSPORT.WEBSOCKET)
                    .transport(Request.TRANSPORT.SSE)
                    .transport(Request.TRANSPORT.LONG_POLLING);
        }



        final Socket socket = client.create();

        socket.on(Event.MESSAGE, new Function<Message>() {
            @Override
            public void on(Message t) {
                Date d = new Date(t.getTime());

                ColibriMessage msg = ColibriMsgMapper.msgToPOJO(t.getMessage());

                if(msg != null){
                    colClient.processReceivedMsg(msg);
                }

                if(colClient.isLocalAtmosphereClient()){
                    logger.info("Author {}: {}", t.getAuthor() + "@ " + d.getHours() + ":" + d.getMinutes(), t.getMessage(
                    ));
                } else {
                    logger.info("content : {}"/*, t.getAuthor() + "@ " + d.getHours() + ":" + d.getMinutes()*/, t.getMessage());
                }

            }
        }).on(new Function<Throwable>() {

            @Override
            public void on(Throwable t) {
                t.printStackTrace();
            }

        }).on(Event.CLOSE.name(), new Function<String>() {
            @Override
            public void on(String t) {
                logger.info("Connection closed");
            }
        }).open(request.build());


        return socket;
    }
}
