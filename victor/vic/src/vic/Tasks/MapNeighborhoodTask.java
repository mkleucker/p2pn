package vic.Tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vic.Entities.Peer;
import vic.PeerApp;

import java.util.ArrayList;
import java.util.HashMap;


public class MapNeighborhoodTask extends DefaultAsyncTask {

    private static final Logger logger = LogManager.getLogger(MapNeighborhoodTask.class.getName());

    private HashMap<Peer, ArrayList<Peer>> data;


    public MapNeighborhoodTask(Peer peer, PeerApp app) {
        super(peer, app);
        this.data = new HashMap<Peer, ArrayList<Peer>>();
        ArrayList<Peer> initialValues = new ArrayList<Peer>(this.app.getPeerList().values());
        execute(initialValues);
    }

    private void execute(ArrayList<Peer> peers) {

        for (Peer peer : peers){

            if (!data.containsKey(peer)){
                // Peer wasn't visited yet

                ArrayList<Peer> connections = new ArrayList<Peer>();

                // TODO: Add request to peer.getNeighbors
                // TODO: Process return values
                // TODO: Add connections to data

            }

        }

    }
}
