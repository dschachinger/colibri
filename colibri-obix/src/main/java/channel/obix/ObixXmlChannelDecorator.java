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

/**
 * This class decorates an obix channel with a xml-channel. Data will be sent and received in xml-format.
 */
public class ObixXmlChannelDecorator extends ObixChannelDecorator {

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    public ObixXmlChannelDecorator(ObixChannel channel) {
        super(channel);
    }

    /******************************************************************
     *                            Methods                             *
     ******************************************************************/

    /**
     * This method sends a GET message with the given obix lobby-URI and returns the response as an {@link ObixLobby}.
     *
     * @param uri   The URI of the requested OBIX lobby.
     * @return      The requested {@link ObixLobby}.
     */
    @Override
    public ObixLobby getLobby(String uri) {
        ObixLobby lobby = channel.getLobby(uri, APPLICATION_XML);
        Obj root = ObixXmlChannelDecorator.decode(lobby.getLobbyAsString());
        lobby.setObj(root);
        List<ObixObject> obixObjects = new ArrayList<ObixObject>();
        for(Obj o : root.list()) {
            List<ObixObject> listOfObjects = new ArrayList<ObixObject>();
            obixObjects.addAll(getNeededObixLobbyObjectsRecursively(o.getHref().get(), channel.getLobbyUri(), listOfObjects));
        }
        ObixObject object = new ObixObject(uri, channel.getPort());
        object.setObj(root);
        setUnitOfObject(object);
        obixObjects.add(object);
        lobby.setObixObjects(obixObjects);
        return lobby;
    }

    /**
     * This method sends a GET message to the CoAP endpoint with the given URI and returns
     * the response as an {@link ObixObject}.
     *
     * @param uri   The uri which is used for the GET message.
     * @return      The response to the GET message.
     */
    @Override
    public ObixObject get(String uri) {
        ObixObject object = channel.get(uri, APPLICATION_XML);
        object.setObixUri(uri);
        object.setObj(ObixXmlChannelDecorator.decode(object.getObjectAsString()));
        setUnitOfObject(object);
        return object;
    }

    /**
     * This method sends a PUT message to the CoAP endpoint with the given URI and returns
     * the response as an {@link ObixObject}.
     *
     * @param obj   The {@link ObixObject} which is used for th PUT message.
     * @return      The response to the PUT message.
     */
    @Override
    public ObixObject put(ObixObject obj) {
        obj.setObjectAsString(encode(obj.getObj()));
        obj.setObj(ObixXmlChannelDecorator.decode(channel.put(obj, APPLICATION_XML).getObjectAsString()));
        return obj;
    }

    /**
     * This method decodes decodes xml-Strings to {@link obix.Obj}.
     *
     * @param objectAsXml   The xml-String which is decoded.
     * @return              The new {@link obix.Obj} parsed from the xml-String.
     */
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

    /**
     * This method encodes a {@link obix.Obj} to a xml-Strings.
     *
     * @param obj   The {@link obix.Obj} which is encoded.
     * @return      The xml-String which is parsed from the {@link obix.Obj}.
     */
    public static String encode(Obj obj) {
        return ObixEncoder.toString(obj);
    }

    /**
     * This method is used for recursively gathering all available {@link ObixObject} from a obix lobby.
     *
     * @param uri       The uri of an {@link ObixObject}.
     * @param baseUri   The {@link #getBaseUri()} of the {@link ObixChannel}.
     * @param list      A helper list used for the recursion.
     * @return          The list of all recursively gathered {@link ObixObject}.
     */
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

    /**
     * Sets the unit of an {@link ObixObject}. If none is available, the unit will be set to 'dimensionless'.
     *
     * @param object    The {@link ObixObject} of which the unit is set.
     */
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
