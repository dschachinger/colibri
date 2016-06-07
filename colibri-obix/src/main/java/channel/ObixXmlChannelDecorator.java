package channel;

import model.ObixLobby;
import model.ObixObject;
import obix.Err;
import obix.Int;
import obix.Obj;
import obix.Uri;
import obix.io.ObixDecoder;
import obix.xml.XException;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.CoAP;

import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.californium.core.coap.MediaTypeRegistry.APPLICATION_XML;

public class ObixXmlChannelDecorator extends ObixChannelDecorator {
    public ObixXmlChannelDecorator(ObixChannel channel) {
        super(channel);
    }

    @Override
    public ObixLobby getLobby(String uri) {
        ObixLobby lobby = channel.getLobby(uri, APPLICATION_XML);
        Obj root = ObixXmlChannelDecorator.decode(lobby.getLobbyAsString());
        lobby.setObj(root);
        for(Obj o : root.list()) {
            List<ObixObject> listOfObjects = new ArrayList<ObixObject>();;
            lobby.getPoints().addAll(getNeededObixLobbyObjectsrecusively(o.getHref().get(), channel.baseUri, listOfObjects));
        }
        return lobby;
    }

    @Override
    public ObixObject get(String uri) {
        ObixObject object = channel.get(uri, APPLICATION_XML);
        object.setUri(uri);
        object.setObj(ObixXmlChannelDecorator.decode(object.getObjectAsString()));
        return object;
    }

    public static Obj decode(String objectAsXml) {
        Obj obj;
        try {
            obj = ObixDecoder.fromString(objectAsXml);
        } catch (XException ex) {
            return new Err("Invalid payload");
        } catch (Exception ex) {
            return new Err("Error parsing xml " + ex.getMessage());
        }
        return obj;
    }

    private List<ObixObject> getNeededObixLobbyObjectsrecusively(String uri, String baseUri, List<ObixObject> list) {
        String u = ObixChannel.normalizeUri(uri, baseUri);
        ObixObject object = this.get(u);
        Obj tempOb = object.getObj();
        if((tempOb.isInt() || tempOb.isReal() || tempOb.isVal()
                || tempOb.isBool()) && !list.contains(object)) {

            list.add(object);
        }
        for (Obj o : tempOb.list()) {
            if (o.getHref() != null) {
                return getNeededObixLobbyObjectsrecusively(o.getHref().get(), uri, list);
            }
        }
        return list;
    }
}
