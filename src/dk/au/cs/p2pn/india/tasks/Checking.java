package dk.au.cs.p2pn.india.tasks;


import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Checking implements Runnable {

	private PeerApp app;

	private static final Logger logger = LogManager.getLogger(Checking.class.getSimpleName());

	private int connectionsPerRun = 5;

	public Checking(PeerApp app) {
		this.app = app;
	}

	public void run (){
		try{
			this.checkConnection();
		}catch (InterruptedException e){
			logger.error("Checking was interrupted: {}", e.getMessage());
		}
	}

	/**
	 * Check the connection with all the Peers of the peerList
	 * and makes and update of the peerlist if there's any peer died.
	 *
	 */
	public void checkConnection() throws InterruptedException{
		if(randInt(1,5) == 3){
			for (Peer entry: this.getPeersForChecking()) {
				this.app.ping(entry.getIP(), entry.getPort());
			}
		}
		Thread.sleep(5000);
	}

	private ArrayList<Peer> getPeersForChecking(){
		ArrayList<Peer> allPeers = new ArrayList<Peer>(this.app.getPeerList().values());
		allPeers.addAll(this.app.getNeighborList().values());

		HashMap<Peer, Date> lastSeen = (HashMap<Peer, Date>) this.app.getLastSeenList();

		for (Map.Entry<Peer, Date> entry : lastSeen.entrySet()) {
			// Eliminate all peers that have been visited in the last 5 minutes.
			if (new Date().getTime() - entry.getValue().getTime() < 1000*60*5) {
				allPeers.remove(entry.getKey());
			}
		}

		if (allPeers.size() < this.connectionsPerRun) {
			return allPeers;
		}

		ArrayList<Peer> toContact = new ArrayList<Peer>();

		// Generate a couple of random ints to use than as indices
		// to filter toContact
		ArrayList<Integer> indices = new ArrayList<Integer>();
		Random rand = new Random();
		while (indices.size() < this.connectionsPerRun) {
			indices.add(rand.nextInt(allPeers.size()));
		}

		for (Integer i : indices){
			toContact.add(allPeers.get(i));
		}

		return toContact;
	}

	/**
	 * It returns a random number between min and max parameters.
	 *
	 * @param min Lower boundary for radnom number
	 * @param max Upper boundary for random number
	 * @return random number
	 */
	private static int randInt(int min, int max) {
		Random rand = new Random();
		return rand.nextInt((max - min) + 1) + min;
	}
}
