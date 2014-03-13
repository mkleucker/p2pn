package dk.au.cs.p2pn.india.tasks;

import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;
import dk.au.cs.p2pn.india.communication.ClientRequestFactory;
import dk.au.cs.p2pn.india.reporting.Reporter;
import dk.au.cs.p2pn.india.reporting.ReporterMeasurements;
import dk.au.cs.p2pn.india.search.BasicSearch;
import dk.au.cs.p2pn.india.search.SearchTypes;
import dk.au.cs.p2pn.india.search.WalkerSearch;
import dk.au.cs.p2pn.india.search.AdvancedWalkerSearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

/**
 * @author johnny
 *         <p/>
 *         SearchStartTask used to start a search process for a specific file using flooding across the whole network.
 *         <p/>
 *         Format of the message:
 *         Origin: 						Peer   (represented as a vector)
 *         File name: 					String
 *         Time to live: 				Integer
 *         Identifier of this search: 	String
 */

@SuppressWarnings({ "unused" })
public class SearchStartTask extends DefaultAsyncTask implements Runnable {

	private static final Logger logger = LogManager.getLogger(SearchStartTask.class.getSimpleName());

	private BasicSearch search;

	public SearchStartTask(PeerApp app, BasicSearch search) {
		super(app);
		this.search = search;
	}

	@Override
	public void run() {
		Reporter.addEvent(ReporterMeasurements.SEARCH_STARTED);
		try {

			if (this.search.getType() == SearchTypes.FLOOD_SEARCH) {
				this.executeFloodSearch();
			} else if (this.search.getType() == SearchTypes.K_WALKER_SEARCH) {
				this.executeWalkerSearch();
			} else if (this.search.getType() == SearchTypes.AK_WALKER_SEARCH) {
				this.executeAKWalkerSearch();
			}

		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (XmlRpcException e) {
			logger.error(e);
			e.printStackTrace();
		}

	}


	private void executeFloodSearch() throws IOException, XmlRpcException {
		for (Map.Entry<String, Peer> entry : this.app.getNeighborList().entrySet()) {
			Peer itPeer = entry.getValue();
			this.executeSearch(itPeer);
		}
	}

	private void executeWalkerSearch() throws IOException, XmlRpcException {
		int num = ((WalkerSearch)this.search).getWalkerCount();

		ArrayList<Peer> possiblePeers = new ArrayList<Peer>(this.app.getNeighborList().values());

		if (num <= possiblePeers.size()) {
			num = possiblePeers.size();
		}

		Random rand = new Random();
		for (int i = 0; i < num; i++){
			if (possiblePeers.size() > 0) {
				int randomInt = rand.nextInt(possiblePeers.size());
				Peer peer = possiblePeers.get(randomInt);
				possiblePeers.remove(randomInt);
				this.executeSearch(peer);
			}
		}

	}
	
	/**
	 * @throws IOException
	 * @throws XmlRpcException
	 * 
	 * Randomly pick num neighbors to pass the search to, every time a neighbor is
	 *  chosen, it is deleted from the 
	 * vector v, and a random draw over the rest elements is performed.
	 */
	private void executeAKWalkerSearch() throws IOException, XmlRpcException {
		logger.error("Inside SearchStartTask, executeAKWalkerSearch, current peer is {}", this.app.getPeer().getId());

		((AdvancedWalkerSearch)this.search).addToPath(this.peer);

		AdvancedWalkerSearch aSearch = (AdvancedWalkerSearch) this.search;
		int num = aSearch.getWalkerCount();
		
		ArrayList<Peer> possiblePeers = new ArrayList<Peer>(this.app.getNeighborList().values());
		if (num <= possiblePeers.size()) {
			num = possiblePeers.size();
		}
		
		/** If this peer has never searched the file, he will create a new neightWeight entry and 
		 * draw a neighbor uniformly at random to send the searching message to. */
		if (!this.app.neighborWeight.containsKey(this.search.getFilename())) {	
			this.app.updateNeighborWeightAddFile(this.search.getFilename());
		}
		
		//randomly draw some neighbors according to the distribution
		this.app.normalizeWeight(this.search.getFilename());
		Set<Map.Entry<Peer, Double>> distr = this.app.neighborWeight.get(this.search.getFilename()).entrySet();
		Vector<Map.Entry<Peer, Double>> v = new Vector<Map.Entry<Peer, Double>>();
		for (Map.Entry<Peer, Double> entry: distr) {
			v.add(entry);
		}
		for (int i = 0; i < num; i++) {
			Map.Entry<Peer, Double> peerSearch = this.app.randomDrawDelete(v);
			logger.error("Current peer is {}, passing search to peer {}", this.app.getPeer().getId(), peerSearch.getKey().getId());
			this.executeSearch(peerSearch.getKey());
			/** When a message is passed to a peer, we decrease 
			 * its weight, if the search is successful, 
			 * we will then increase it by a larger amount. */
			this.app.neighborWeight.get(this.search.getFilename()).put(peerSearch.getKey(), peerSearch.getValue().doubleValue() / AdvancedWalkerSearch.DEC);
		}
		this.app.normalizeWeight(this.search.getFilename());
	}

	
	private void executeSearch(Peer peer) throws IOException, XmlRpcException{
		this.app.addToSearchList(this.search.getId(), peer);
		this.client = ClientRequestFactory.getClient("http://" + peer.getIP() + ':' + peer.getPort() + '/');
		this.client.execute("communication.respondSearch", this.search.toVector());
	}
}
