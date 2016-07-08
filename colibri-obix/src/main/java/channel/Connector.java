package channel;

import channel.colibri.ColibriChannel;
import channel.message.colibriMessage.ColibriMessage;
import channel.obix.ObixChannel;

import java.util.HashMap;
import java.util.Map;

public class Connector {
    private ObixChannel obixChannel;
    private ColibriChannel colibriChannel;
    private String connectorAddress;


    public Connector(ObixChannel obixChannel, ColibriChannel colibriChannel, String connectorAddress) {
        this.obixChannel = obixChannel;
        this.colibriChannel = colibriChannel;
        this.connectorAddress = connectorAddress;
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


}
