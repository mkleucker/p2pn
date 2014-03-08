package dk.au.cs.p2pn.india.tasks;

import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Created by max on 18/02/14.
 */
public class NegotiationTask extends DefaultAsyncTask {
	private static final Logger logger = LogManager.getLogger(NegotiationTask.class.getSimpleName());

	public NegotiationTask(PeerApp app){
		super(app);
	}


	@Override
	public void run() {
		try {
			this.startNegotiate();
		}catch(Exception e){
			logger.error("error nima", e.getCause());
		}
	}

	private void startNegotiate(){
		Vector<Peer> peers = new Vector<Peer>();
		Set<Map.Entry<String, Peer>> set = this.app.getPeerList().entrySet();
		for (Map.Entry<String, Peer> entry: set) {
			if (!entry.getValue().equals(this.app.getPeer()))
				peers.add(entry.getValue());
		}

		if (peers.size() == 0){
			return;
		}

		// I only know one peer -> Add him / her
		if (peers.size() == 1){
			System.out.println("let's become neighbors!!!");
			this.becomeNeighbor(peers.get(0).getIP(), peers.get(0).getPort());
			return;
		}

		double[] c = new double[peers.size()];

		c[0] = peers.get(0).getCapacity();
		for (int i = 1; i < peers.size(); i++) {
			c[i] = c[i - 1] + peers.get(i).getCapacity();
		}
		for (int i = 0; i < peers.size(); i++) {
			c[i] = c[i] / c[peers.size() - 1];
		}

		logger.info("inside of the startNegotiate function");
		double r = Math.random();
		int contacted = 0;

		for (int i = 0; i < peers.size(); i++) {
			Peer itPeer = peers.get(i);
			if (r < c[i] && contacted < (this.peer.getCapacity() - this.app.getNeighborList().size())) {
				contacted++;
				try {
					this.becomeNeighbor(itPeer.getIP(), itPeer.getPort());
					logger.info("Start to negotiate from peer " + this.peer.getId() + " to peer " + itPeer.getId());
				} catch (Exception e) {
					logger.error("Function startNegotiate failed, probably because the peer no longer exists, error message {}", e.getMessage());
				}
			}
		}
	}

	private void becomeNeighbor(String ip, int port) {
		new BecomeNeighborTask(ip, port, this.app).execute();
	}
}
