package dk.au.cs.p2pn.india.tasks;

import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;
import dk.au.cs.p2pn.india.communication.ClientRequestFactory;
import dk.au.cs.p2pn.india.helper.CommunicationConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcClient;

import java.util.*;


public class PeerExchangeTask extends DefaultAsyncTask {

    private static final Logger logger = LogManager.getLogger(PeerExchangeTask.class.getSimpleName());
    private boolean recursiveSearch;


    public PeerExchangeTask(Peer peer, PeerApp app, boolean recursive) {
        super(peer, app);
        this.recursiveSearch = recursive;
    }

    public PeerExchangeTask(Peer peer, PeerApp app) {
        this(peer, app, false);
    }


    @Override
    public void run() {
        try {

            //Iteration of all the peers in the list of peers
            ArrayList<Integer> visited = new ArrayList<Integer>();
            visited.add(this.peer.getId());

            HashMap<String, Peer> foundPeers = new HashMap<String, Peer>();

            HashMap<String, Peer> startingPeers = (HashMap<String, Peer>) this.app.getPeerList();

            for (Map.Entry<String, Peer> entry : startingPeers.entrySet()) {
                try {
                    Peer currentPeer = entry.getValue();

                    XmlRpcClient client = ClientRequestFactory.getClient("http://" + currentPeer.getIP() + ':' + currentPeer.getPort() + '/');

                    Hashtable<String, Vector> result = (Hashtable<String, Vector>) client.execute(
                            "communication.getPeerList",
                            new Vector(0));
                    if (result == null) {
                        logger.debug("No result from Discovery");
                        return;
                    } else {
                        // Add the peer to my peerlist.
                        for (Map.Entry<String, Vector> resultEntry : result.entrySet()){
                            foundPeers.put(resultEntry.getKey(), CommunicationConverter.createPeer(resultEntry.getValue()));
                        }
                    }

                    visited.add(peer.getId());

                } catch (Exception e) {
                    // Handling errors directly here, so one faulty peer
                    // won't corrupt everything else.
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            }

            for (Map.Entry<String, Peer> entry : foundPeers.entrySet()) {
                if (visited.contains(Integer.parseInt(entry.getKey()))) {
                    continue;
                }

                try {
                    Peer currentPeer = entry.getValue();

                    Thread connection = new Thread(new ConnectionAsyncTask(currentPeer.getIP(), currentPeer.getPort(), this.app));
                    connection.start();

                    visited.add(peer.getId());

                } catch (Exception e) {
                    // Handling errors directly here, so one faulty peer
                    // won't corrupt everything else.
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            }


        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
