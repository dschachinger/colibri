package channel;

import channel.colibri.ColibriChannel;
import channel.obix.ObixChannel;

public class Connector {
    private ObixChannel obixChannel;
    private ColibriChannel colibriChannel;
    private String connectorAddress;
    private String ipAddress;

    public Connector(ObixChannel obixChannel, ColibriChannel colibriChannel, String connectorAddress, String ipAddress) {
        this.obixChannel = obixChannel;
        this.colibriChannel = colibriChannel;
        this.connectorAddress = connectorAddress;
        this.ipAddress = ipAddress;
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
}
