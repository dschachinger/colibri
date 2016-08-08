import channel.obix.CoapChannel;
import channel.obix.ObixChannel;
import channel.obix.ObixXmlChannelDecorator;
import model.obix.ObixLobby;
import obix.Obj;
import org.eclipse.californium.core.CoapClient;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CoapChannelTest {

    private ObixChannel undecoratedCoapChannel;

    @Before
    public void setUp() {
        undecoratedCoapChannel = new CoapChannel("localhost", "localhost/obix", null);
    }

    @After
    public void tearDown() {
    }

    @Test()
    public void getLobbyWithWrongURIShouldReturnErr() {
        ObixLobby lobby = undecoratedCoapChannel.getLobby("wrong");
        Obj lobbyObj = ObixXmlChannelDecorator.decode(lobby.getLobbyAsString());
        Assert.assertTrue(lobbyObj.isErr());
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
}
