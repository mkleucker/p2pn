package vic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Hashtable;
import java.util.Vector;


public class CommunicationHandler {
    private Peer peer;
    private PeerApp app;

    private static final Logger logger = LogManager.getLogger(CommunicationHandler.class.getName());


    public CommunicationHandler(Peer peer, PeerApp app){
        this.peer = peer;
        this.app = app;
    }

    public Vector pong(int IdArg, String IPArg, int portArg, int capacityArg, int depthInt) {

        // Create Peer object
        this.app.addPeer(new Peer(IdArg, IPArg, portArg, capacityArg));

        return PeerApp.createVectorForPeer(this.peer, depthInt-1);
    }

    public Hashtable<String, Vector> getPeerList(){
        return PeerApp.createExchangeData(this.app.getPeerList());
    }
}
