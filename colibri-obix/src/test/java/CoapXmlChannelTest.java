import channel.obix.CoapChannel;
import channel.obix.ObixChannel;
import channel.obix.ObixXmlChannelDecorator;
import model.obix.ObixLobby;
import model.obix.ObixObject;
import obix.Int;
import org.eclipse.californium.core.CoapClient;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class CoapXmlChannelTest {

    private ObixChannel channel;

    @Before
    public void setUp() {
        List<String> observedTypes = new ArrayList<String>();
        observedTypes.add("obix.Bool");
        observedTypes.add("obix.Int");
        observedTypes.add("obix.Real");
        observedTypes.add("obix.Val");
        channel = new ObixXmlChannelDecorator(new CoapChannel("localhost", "localhost/obix", observedTypes));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void getLobbyShouldReturnLobby() {
        CoapClient coapClient = new CoapClient(channel.getBaseUri());
        //False, if the coap client is offline
        org.junit.Assume.assumeTrue(coapClient.ping());
        ObixLobby lobby = channel.getLobby();
        String ret = "";
        for(ObixObject o : lobby.getObixObjects()) {
            System.out.println(o.getObj().getName() + ": " + o.getUri());
            ret += o.getUri();
        }
        Assert.assertThat(ret, CoreMatchers.containsString("roomIllumination"));
        Assert.assertThat(ret, CoreMatchers.containsString("tempOutside"));
        Assert.assertThat(ret, CoreMatchers.containsString("virtualPushButton/value"));
    }

    @Test
    public void getWellKnownObjectShouldReturnObject() {
        CoapClient coapClient = new CoapClient(channel.getBaseUri());
        //False, if the coap client is offline
        org.junit.Assume.assumeTrue(coapClient.ping());
        ObixObject object = channel.get("VirtualDevices/sunblindMiddleA/moveDownValue");
        Assert.assertThat(object.getUri(), CoreMatchers.containsString("moveDownValue"));
        Assert.assertTrue(object.getObj().isBool());
        Assert.assertTrue(object.getObj().isWritable());
    }

    @Test
    public void getAndPutObjectShouldReturnObject() {
        CoapClient coapClient = new CoapClient(channel.getBaseUri());
        //False, if the coap client is offline
        org.junit.Assume.assumeTrue(coapClient.ping());
        ObixObject object = channel.get("VirtualDevices/virtualBrightnessActuator/value");
        Assert.assertTrue(object.getObj().isInt());
        Int i = (Int) object.getObj();
        i.set(77);
        object.setObj(i);
        ObixObject newO = channel.put(object);
        Assert.assertThat(newO.getUri(), CoreMatchers.containsString("virtualBrightnessActuator"));
        Assert.assertTrue(newO.getObj().isInt());
        Assert.assertEquals(newO.getObj().getInt(), 77);
    }

}
