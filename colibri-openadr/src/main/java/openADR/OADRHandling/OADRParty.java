package openADR.OADRHandling;

import Utils.OpenADRColibriBridge;
import openADR.OADRMsgInfo.MsgInfo_OADRCreateReport;
import openADR.Utils.FollowUpMsg;
import openADR.Utils.OADRConInfo;
import openADR.XMPP.XMPPChannel;

import javax.xml.bind.JAXBException;
import java.util.List;

/**
 * Created by georg on 09.06.16.
 * This class represents one party of the openADR standard.
 * It can be either a VTN (=server) or a VEN (=client).
 */
public abstract class OADRParty {
    // This manager transforms XML messages into java objects
    protected JAXBManager jaxbManager;
    // This object defines the used channel to communicate with the opposite party
    protected Channel channel;

    protected OpenADRColibriBridge bridge;

    public OADRParty(OpenADRColibriBridge bridge) {
        try {
            jaxbManager = new JAXBManager();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        try {
            channel = new XMPPChannel(jaxbManager, this);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Can not establish a proper connection to XMPP server.");
            System.exit(1);
        }

        this.bridge = bridge;
    }

    /**
     * This method is called if a follow-up message is needed.
     * It handles the VTN communication by its own.
     * @param followUpMsg contains information which message should be transmitted and how the message should look like.
     */
    public abstract void handleFollowUpMsg(FollowUpMsg followUpMsg);

    /**
     * This method terminates the party.
     * It is not guaranteed that this party still works afterwards.
     * It is only allowed to call this method if the party was successfully started beforehand.
     */
    public abstract void terminate();

    public Channel getChannel(){
        return channel;
    }

    public OpenADRColibriBridge getBridge() {
        return bridge;
    }

    public void generateAsyncSendUpdateReportMsgWorker(List<MsgInfo_OADRCreateReport.ReportRequest> reportRequests){
        // start new thread
        for(MsgInfo_OADRCreateReport.ReportRequest reportRequest : reportRequests){
            AsyncSendUpdateReportMsgWorker asyncSendUpdateReportMsgWorker = new AsyncSendUpdateReportMsgWorker(reportRequest, this);
            OADRConInfo.addUpdateReportMsgWorker(asyncSendUpdateReportMsgWorker);
            asyncSendUpdateReportMsgWorker.start();
        }
    }
}
