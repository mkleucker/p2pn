package dk.au.cs.p2pn.india.tasks;

import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;
import dk.au.cs.p2pn.india.communication.ClientRequestFactory;
import dk.au.cs.p2pn.india.helper.CommunicationConverter;

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
import java.util.Map;
import java.util.Random;
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

public class SearchStartTask extends DefaultAsyncTask implements Runnable {

	private static final Logger logger = LogManager.getLogger(SearchStartTask.class.getSimpleName());

	private BasicSearch search;

	public SearchStartTask(PeerApp app, BasicSearch search) {
		super(app);
		this.search = search;
	}

	@SuppressWarnings({"unused", "rawtypes"})
	@Override
	public void run() {

		Vector<Object> params = this.search.toVector();

		try {

			if (this.search.getType() == SearchTypes.FLOOD_SEARCH) {
				this.executeFloodSearch(params);
			} else if (this.search.getType() == SearchTypes.K_WALKER_SEARCH) {
				this.executeWalkerSearch(params);
			} else if (this.search.getType() == SearchTypes.AK_WALKER_SEARCH) {
				this.executeAKWalkerSearch(params);
			}

		} catch (IOException e) {
			logger.error(e);
		} catch (XmlRpcException e) {
			logger.error(e);
		}

	}


	@SuppressWarnings("rawtypes")
	private void executeFloodSearch(Vector params) throws IOException, XmlRpcException {
		for (Map.Entry<Integer, Peer> entry : this.app.getNeighborList().entrySet()) {
			Peer itPeer = entry.getValue();
			this.executeSearch(itPeer, params);
		}
	}

	@SuppressWarnings("rawtypes")
	private void executeWalkerSearch(Vector params) throws IOException, XmlRpcException {
		WalkerSearch wSearch = (WalkerSearch) this.search;
		int num = wSearch.getWalkerCount();

		ArrayList<Peer> possiblePeers = new ArrayList<Peer>(this.app.getNeighborList().values());

		if (num <= possiblePeers.size()) {
			num = possiblePeers.size();
		}

		Random rand = new Random();
		for (int i = 0; i < num; i++){
			int randomInt = rand.nextInt(possiblePeers.size());
			Peer peer = possiblePeers.get(randomInt);
			possiblePeers.remove(randomInt);
			this.executeSearch(peer, params);
		}

	}
	
	@SuppressWarnings("rawtypes")
	private void executeAKWalkerSearch(Vector params) throws IOException, XmlRpcException {
		AdvancedWalkerSearch aSearch = (AdvancedWalkerSearch) this.search;
		int num = aSearch.getWalkerCount();
		
		ArrayList<Peer> possiblePeers = new ArrayList<Peer>(this.app.getNeighborList().values());
		if (num <= possiblePeers.size()) {
			num = possiblePeers.size();
		}
		
		if (!this.app.neighborWeight.containsKey(this.search.getFilename())) {
			Vector<Double> distr = new Vector<Double>();
			for (int i = 0; i < this.app.getNeighborList().size(); i++) {
				distr.add(new Double(1.0));
			}
			this.app.neighborWeight.put(this.search.getFilename(), distr);
			this.app.normalizeWeight(this.search.getFilename());
			
			Random rand = new Random();
			for (int i = 0; i < num; i++){
				int randomInt = rand.nextInt(possiblePeers.size());
				Peer peer = possiblePeers.get(randomInt);
				possiblePeers.remove(randomInt);
				this.executeSearch(peer, params);
			}
		} else {
			//TODO
		}
		

	}
	
	@SuppressWarnings("rawtypes")
	private void executeSearch(Peer peer, Vector params) throws IOException, XmlRpcException{
		this.client = ClientRequestFactory.getClient("http://" + peer.getIP() + ':' + peer.getPort() + '/');
		this.client.execute("communication.respondSearch", params);
	}
}
