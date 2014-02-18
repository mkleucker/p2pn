package dk.au.cs.p2pn.india.tasks;

import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;
import dk.au.cs.p2pn.india.helper.CommunicationConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcClient;

import java.util.*;


public class MapNeighborhoodTask extends DefaultTask {

	private static final Logger logger = LogManager.getLogger(MapNeighborhoodTask.class.getSimpleName());

	private HashMap<Peer, ArrayList<Peer>> data;
	private HashSet<Peer> toDo;


	public MapNeighborhoodTask(Peer peer, PeerApp app) {
		super(peer, app);
		this.data = new HashMap<Peer, ArrayList<Peer>>();
		this.data.put(peer, new ArrayList<Peer>(this.app.getNeighborList().values()));
		this.toDo = new HashSet<Peer>(this.app.getPeerList().values());
	}

	public HashMap<Peer, ArrayList<Peer>> getTopology() {
		execute();
		return this.data;
	}

	private synchronized void execute() {
		if (this.toDo.size() == 0){
			return;
		}

		for (Peer peer : new HashSet<Peer>(this.toDo)) {
			if (!data.containsKey(peer)) { // Peer wasn't visited yet

				ArrayList<Peer> connections = new ArrayList<Peer>();
				try {
					XmlRpcClient client = new XmlRpcClient("http://" + peer.getIP() + ':' + peer.getPort() + '/');

					Vector<Vector> result = (Vector<Vector>) client.execute("communication.getNeighborList", new Vector(0));
					if (result != null) {
						for (Vector v : result) {
							Peer newPeer = CommunicationConverter.createPeer(v);
							connections.add(newPeer);
							if (!data.containsKey(newPeer)){
								toDo.add(newPeer);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				data.put(peer, connections);

			}
			toDo.remove(peer);
		}
		execute();
	}
}
