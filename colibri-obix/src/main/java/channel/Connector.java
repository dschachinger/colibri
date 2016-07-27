package channel;

import channel.colibri.ColibriChannel;
import channel.obix.ObixChannel;
import service.RunAndStopAble;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Connector {
    private ObixChannel obixChannel;
    private ColibriChannel colibriChannel;
    private String connectorAddress;
    private String ipAddress;
    private boolean running;
    private List<RunAndStopAble> openRunAndStopAbles;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public Connector(ObixChannel obixChannel, ColibriChannel colibriChannel, String connectorAddress, String ipAddress) {
        this.obixChannel = obixChannel;
        this.colibriChannel = colibriChannel;
        this.connectorAddress = connectorAddress;
        this.ipAddress = ipAddress;
        this.running = true;
        this.openRunAndStopAbles = Collections.synchronizedList(new ArrayList<>());;
    }

    public ObixChannel getObixChannel() {
        return obixChannel;
    }

    public void setObixChannel(ObixChannel obixChannel) {
        this.obixChannel = obixChannel;
    }

    public ColibriChannel getColibriChannel() {
        return colibriChannel;
    }

    public void setColibriChannel(ColibriChannel colibriChannel) {
        this.colibriChannel = colibriChannel;
    }

    public String getConnectorAddress() {
        return connectorAddress;
    }

    public void setConnectorAddress(String connectorAddress) {
        this.connectorAddress = connectorAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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

    public void stop() {
        openRunAndStopAbles.forEach(RunAndStopAble::stop);
        executor.shutdownNow();
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
