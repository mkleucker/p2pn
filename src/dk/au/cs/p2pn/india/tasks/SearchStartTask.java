package dk.au.cs.p2pn.india.tasks;

import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;
import dk.au.cs.p2pn.india.communication.ClientRequestFactory;
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
		} catch (XmlRpcException e) {
			logger.error(e);
		}

	}


	private void executeFloodSearch() throws IOException, XmlRpcException {
		for (Map.Entry<Integer, Peer> entry : this.app.getNeighborList().entrySet()) {
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
			int randomInt = rand.nextInt(possiblePeers.size());
			Peer peer = possiblePeers.get(randomInt);
			possiblePeers.remove(randomInt);
			this.executeSearch(peer);
		}

	}
	
	private void executeAKWalkerSearch() throws IOException, XmlRpcException {
		AdvancedWalkerSearch aSearch = (AdvancedWalkerSearch) this.search;
		int num = aSearch.getWalkerCount();
		
		ArrayList<Peer> possiblePeers = new ArrayList<Peer>(this.app.getNeighborList().values());
		if (num <= possiblePeers.size()) {
			num = possiblePeers.size();
		}
		
		if (!this.app.neighborWeight.containsKey(this.search.getFilename())) {	
			this.app.updateNeighborWeightAddFile(this.search.getFilename());
			
			Random rand = new Random();
			for (int i = 0; i < num; i++){
				int randomInt = rand.nextInt(possiblePeers.size());
				Peer peer = possiblePeers.get(randomInt);
				possiblePeers.remove(randomInt);
				this.executeSearch(peer);
			}
		} else {
			//TODO
			//randomly draw some neighbors according to the distribution
			this.app.normalizeWeight(this.search.getFilename());
			Set<Map.Entry<Peer, Double>> distr = this.app.neighborWeight.get(this.search.getFilename()).entrySet();
			double[] cumula = new double[distr.size()];
			Vector<Map.Entry<Peer, Double>> v = new Vector<Map.Entry<Peer, Double>>();
			for (Map.Entry<Peer, Double> entry: distr) {
				v.add(entry);
			}
			cumula[0] = v.get(0).getValue().doubleValue();
			for (int i = 1; i < v.size(); i++) {
				cumula[i] = cumula[i - 1] + v.get(i).getValue().doubleValue();
			}
			for (int i = 0; i < num; i++) {
				//TODO ask max how to modify the search list
			}
			double r = Math.random();
			int res = v.size() - 1;
			for (int i = 0; i < v.size(); i++) {
				if (r < cumula[i]) {
					res = i;
					break;
				}
			}
			
		}
	}
	
	private void executeSearch(Peer peer) throws IOException, XmlRpcException{
		this.app.addToSearchList(this.search.getId(), peer);
		this.client = ClientRequestFactory.getClient("http://" + peer.getIP() + ':' + peer.getPort() + '/');
		this.client.execute("communication.respondSearch", this.search.toVector());
	}
}
