import channel.CoapChannel;
import channel.ObixChannel;
import channel.ObixXmlChannelDecorator;
import model.ObixLobby;
import model.ObixObject;
import obix.Obj;
import org.eclipse.californium.core.CoapClient;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CoapXmlChannelTest {

    private ObixChannel channel;

    @Before
    public void setUp() {
        channel = new ObixXmlChannelDecorator(new CoapChannel("localhost", "localhost/obix"));
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
        for(ObixObject o : lobby.getPoints()) {
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
        assertEquals(object.getObj().getName(), "moveDownValue");
        assertEquals(object.getObj().getHref().get(), "moveDownValue/");
        Assert.assertThat(object.getUri(), CoreMatchers.containsString("moveDownValue"));
        Assert.assertTrue(object.getObj().isBool());
        Assert.assertTrue(!object.getObj().getBool());
        Assert.assertTrue(object.getObj().isWritable());
    }

}
