package openADR.del;

/**
 * Created by georg on 01.06.16.
 */
/*
public class XMPPOADR2IQListener implements StanzaListener {
    private openADR.XMPP.XMPPChannel channel;


    public XMPPOADR2IQListener(openADR.XMPP.XMPPChannel channel){
        this.channel = channel;
    }

    @Override
    public void processPacket(Stanza packet) throws SmackException.NotConnectedException{
        OADR2IQ iq = (OADR2IQ)packet;
        channel.getController().processReceivedMessage(new OADRMsgObject(iq.getExtension().getElementNameWithoutNamespace(), iq.getStanzaId(), iq.getOADRPayload()));
        System.out.println("Oadr msg received: " + iq.getChildElementXML());
    }

    private void printInfo(String msgTyp, Object msg){
        System.out.println("message name: " + msgTyp);
        */
/*
        if(msg instanceof OadrCreatedPartyRegistration) System.out.println("vtn id: " + ((OadrCreatedPartyRegistration)msg).getVtnID());

        if(msg instanceof OadrDistributeEvent) {
            OadrDistributeEvent oadrDistributeEvent = ((com.enernoc.open.oadr2.model.v20b.OadrDistributeEvent) msg);
            SignalPayload signalPayload = (com.enernoc.open.oadr2.model.v20b.ei.SignalPayload) oadrDistributeEvent.getOadrEvents().get(0).getEiEvent().
                    getEiEventSignals().getEiEventSignals().get(0).getIntervals().getIntervals().get(0).getStreamPayloadBases().get(0).getValue();
            System.out.println("vtn id: " + ((OadrDistributeEvent) msg).getVtnID());
            System.out.println("value: " + ((PayloadFloatType) signalPayload.getPayloadBase().getValue()).getValue());

            channel.createSendMsg("oadrCreatedEvent");

        }
    }
}*/
