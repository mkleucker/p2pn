package dk.au.cs.p2pn.india;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.WebServer;

public class ListeningTask extends DefaultAsyncTask {
    WebServer server;

    private static final Logger logger = LogManager.getLogger(ListeningTask.class.getName());


    public ListeningTask(Peer peer, PeerApp app){
        super(peer, app);
    }

    @Override
    public void run() {
        try {
            // Start the server, using built-in version
            this.server = new WebServer(this.peer.getPort());

            // we _do_ want other clients to connect to us
            server.setParanoid(false);
            server.acceptClient(this.peer.getIP());

            // Register our handler class as discovery
            server.addHandler("communication", new CommunicationHandler(this.peer, this.app));
            server.start();
            logger.debug("Created ListeningTask successfully");
        } catch (Exception e) {
            logger.error("Could not start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean shutdown(){
        this.server.shutdown();
        return true;
    }
}
