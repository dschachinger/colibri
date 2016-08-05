
import obix.Err;
import obix.Obj;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class XmlChannelDecoratorTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void decodeNonXmlFormatString() {
        Obj obj = ObixXmlChannelDecorator.decode("<test.....");
        assertEquals(obj.getClass(), Err.class);
        Assert.assertThat(obj.getName(), CoreMatchers.containsString("Error parsing xml"));
    }

    @Test
    public void decodeAcceptableObixLobbyFromXml() {
        Obj obj = ObixXmlChannelDecorator.decode("<obj href=\"obix/\">\n" +
                "<ref name=\"about\" href=\"obix/about\"/>\n" +
                "<ref name=\"enums\" href=\"enums\"/>\n" +
                "<ref name=\"units\" href=\"units\"/>\n" +
                "<ref name=\"parameters\" href=\"parameters\"/>\n" +
                "<ref name=\"encodings\" href=\"encodings\"/>\n" +
                "<ref href=\"watchService\" is=\"obix:WatchService\"/>\n" +
                "<ref href=\"alarms\" is=\"obix:AlarmSubject\"/>\n" +
                "</obj>");
        Assert.assertThat(obj.getHref().get(), CoreMatchers.containsString("obix"));
    }
}
