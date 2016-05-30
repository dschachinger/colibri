import channel.CoapChannel;
import channel.ObixChannel;
import exception.CoapCommunicationException;
import model.ObixLobby;
import model.ObixObject;
import org.eclipse.californium.core.CoapClient;
import org.hamcrest.CoreMatchers;
import org.junit.*;

public class CoapChannelTest {

    private ObixChannel undecoratedCoapChannel;

    @Before
    public void setUp() {
        undecoratedCoapChannel = new CoapChannel();
    }

    @After
    public void tearDown() {
    }

    @Test(expected = CoapCommunicationException.class)
    public void getLobbyWithWrongURI() {
        undecoratedCoapChannel.getLobby("wrong.wrong.wrong/obix");
    }

    @Test(expected = CoapCommunicationException.class)
    public void getWithWrongURI() {
        undecoratedCoapChannel.get("wrong.wrong.wrong");
    }

    /**
     * A local oBIX server has to be running in order to pass this test.
     *
     * The IoTSYS System can be used for this: https://github.com/mjung85/iotsys/
     */
    @Test
    public void getLobbyShouldReturnEmptyLobbyOnlyWithResponseString() {
        String uri = "localhost/obix";
        CoapClient coapClient = new CoapClient(uri);
        //False, if the coap client is offline
        org.junit.Assume.assumeTrue(coapClient.ping());
        ObixLobby lobby = undecoratedCoapChannel.getLobby(uri);
        Assert.assertThat(lobby.getLobbyAsString(), CoreMatchers.containsString("href=\"obix/\""));
    }

    /**
     * A local oBIX server has to be running in order to pass this test.
     *
     * The IoTSYS System can be used for this: https://github.com/mjung85/iotsys/
     */
    @Test
    public void getShouldReturnEmptyObjectOnlyWithResponseString() {
        String uri = "localhost/VirtualDevices/virtualIndoorBrightnessSensor";
        CoapClient coapClient = new CoapClient(uri);
        //False, if the coap client is offline
        org.junit.Assume.assumeTrue(coapClient.ping());
        ObixObject object = undecoratedCoapChannel.get(uri);
        Assert.assertThat(object.getObjectAsString(), CoreMatchers.containsString("virtualIndoorBrightnessSensor"));
    }
}
