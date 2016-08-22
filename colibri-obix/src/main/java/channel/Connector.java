package channel;

import channel.colibri.ColibriChannel;
import channel.obix.ObixChannel;
import service.RunAndStopAble;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class represents the connector and is used for linking the obix and colibri world together.
 * For each {@link ObixChannel} a new {@link Connector} is instantiated. Each {@link Connector} has the same
 * {@link ColibriChannel}.
 */
public class Connector {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    private ObixChannel obixChannel;
    private ColibriChannel colibriChannel;
    private String connectorAddress;
    private String ipAddress;

    /**
     * True, if the connector is running, otherwise false.
     */
    private boolean running;

    /**
     * A list of all {@link RunAndStopAble} of this connector which aren't stopped.
     */
    private List<RunAndStopAble> openRunAndStopAbles;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    public Connector(ObixChannel obixChannel, ColibriChannel colibriChannel, String connectorAddress, String ipAddress) {
        this.obixChannel = obixChannel;
        this.colibriChannel = colibriChannel;
        this.connectorAddress = connectorAddress;
        this.ipAddress = ipAddress;
        this.running = true;
        this.openRunAndStopAbles = Collections.synchronizedList(new ArrayList<>());;
    }

    /******************************************************************
     *                           Methods                              *
     ******************************************************************/

    public void stop() {
        openRunAndStopAbles.forEach(RunAndStopAble::stop);
        executor.shutdownNow();
    }

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public ObixChannel getObixChannel() {
        return obixChannel;
    }

    public void setObixChannel(ObixChannel obixChannel) {
        this.obixChannel = obixChannel;
    }

    public ColibriChannel getColibriChannel() {
        return colibriChannel;
    }

    public String getConnectorAddress() {
        return connectorAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void addRunAndStopAble(RunAndStopAble t) {
        openRunAndStopAbles.add(t);
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
