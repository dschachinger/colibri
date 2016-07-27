import exception.ConfigurationException;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import service.Configurator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ConfiguratorTest {

    private Configurator conf;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        conf = new Configurator();
        Properties prop = new Properties();
        OutputStream output = null;

        try {
            File file = tempFolder.newFile("test.properties");
            File empty = tempFolder.newFile("empty.properties");
            File wrong = tempFolder.newFile("wrong.properties");
            output = new FileOutputStream(file);
            prop.setProperty("oBIXLobby1", "http://localhost:8080/obix");
            prop.setProperty("oBIXLobby2", "http://test.test.test/obix");
            prop.store(output, null);

            prop = new Properties();
            output = new FileOutputStream(wrong);
            prop.setProperty("wrongProperty", "http://wrong.wrong.wrong/obix");
            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @After
    public void tearDown() {
        tempFolder.delete();
    }

    @Test(expected = ConfigurationException.class)
    public void readEmptyConfigFile() {
        conf.getObixCoapChannels(tempFolder.getRoot() + File.separator + "empty.properties");
    }

    @Test(expected = ConfigurationException.class)
    public void readConfigFileWithNoObixLobby() {
        conf.getObixCoapChannels(tempFolder.getRoot() + File.separator + "wrong.properties");
    }

    @Test
    public void readObixLobbiesSuccessfully() {
        assertEquals("http://localhost:8080/obix", conf.getObixCoapChannels(tempFolder.getRoot() +File.separator + "test.properties").get(0).getLobbyUri());
        assertEquals("http://test.test.test/obix", conf.getObixCoapChannels(tempFolder.getRoot() +File.separator + "test.properties").get(1).getLobbyUri());
    }
}
