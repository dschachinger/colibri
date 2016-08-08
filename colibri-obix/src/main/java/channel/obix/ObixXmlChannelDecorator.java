package channel.obix;

import model.obix.ObixLobby;
import model.obix.ObixObject;
import obix.Err;
import obix.Int;
import obix.Obj;
import obix.Real;
import obix.contracts.Unit;
import obix.io.ObixDecoder;
import obix.io.ObixEncoder;
import obix.xml.XException;

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
        List<ObixObject> obixObjects = new ArrayList<ObixObject>();
        for(Obj o : root.list()) {
            List<ObixObject> listOfObjects = new ArrayList<ObixObject>();
            obixObjects.addAll(getNeededObixLobbyObjectsRecursively(o.getHref().get(), channel.lobbyUri, listOfObjects));
        }
        ObixObject object = new ObixObject(uri, channel.getPort());
        object.setObj(root);
        setUnitOfObject(object);
        obixObjects.add(object);
        lobby.setObixObjects(obixObjects);
        return lobby;
    }

    @Override
    public ObixObject get(String uri) {
        ObixObject object = channel.get(uri, APPLICATION_XML);
        object.setObixUri(uri);
        object.setObj(ObixXmlChannelDecorator.decode(object.getObjectAsString()));
        setUnitOfObject(object);
        return object;
    }

    @Override
    public ObixObject put(ObixObject obj) {
        obj.setObjectAsString(encode(obj.getObj()));
        obj.setObj(ObixXmlChannelDecorator.decode(channel.put(obj, APPLICATION_XML).getObjectAsString()));
        return obj;
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

    public static String encode(Obj obj) {
        return ObixEncoder.toString(obj);
    }

    private List<ObixObject> getNeededObixLobbyObjectsRecursively(String uri, String baseUri, List<ObixObject> list) {
        String u = normalizeUri(uri, baseUri);
        ObixObject object = this.get(u);
        Obj tempOb = object.getObj();
        if(channel.getObservedTypes().contains(tempOb.getClass().getName()) && !list.contains(object)) {
            list.add(object);
        }
        for (Obj o : tempOb.list()) {
            if (o.getHref() != null) {
                getNeededObixLobbyObjectsRecursively(o.getHref().get(), u, list);
            }
        }
        return list;
    }

    private void setUnitOfObject(ObixObject object) {
        String unitUri = null;
        if(object.getObj().isReal()) {
            Real real = (Real) object.getObj();
            if(real.getUnit() != null) {
                unitUri = real.getUnit().toString();
            }
        } else if(object.getObj().isInt()) {
            Int i = (Int) object.getObj();
            if(i.getUnit() != null) {
                unitUri = i.getUnit().toString();
            }
        }
        object.setObixUnitUri("dimensionless");
        if(unitUri != null) {
            if(unitUri.contains(":")) {
                unitUri = unitUri.split(":")[1];
            }
            if(unitUri.startsWith("/")) {
                unitUri = unitUri.substring(1, unitUri.length());
            }
            Obj o = ObixXmlChannelDecorator.decode(channel.get(unitUri, APPLICATION_XML).getObjectAsString());
            if(!o.isErr()) {
                object.setUnit((Unit) o);
            }
            object.setObixUnitUri(unitUri);
        }
    }
}
