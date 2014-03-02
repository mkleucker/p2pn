package dk.au.cs.p2pn.india.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import dk.au.cs.p2pn.india.communication.ClientRequestFactory;
import dk.au.cs.p2pn.india.search.AdvancedWalkerSearch;
import dk.au.cs.p2pn.india.search.BasicSearch;
import dk.au.cs.p2pn.india.search.SearchTypes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;

import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;

/**
 * @author johnny
 *         used to pass the search to other peers when the local peer doesn't have the file
 */
public class SearchPassTask extends DefaultAsyncTask {

	private BasicSearch search;
	private static final Logger logger = LogManager.getLogger(SearchStartTask.class.getSimpleName());

	public SearchPassTask(PeerApp app, BasicSearch search) {
		super(app);
		this.search = search;
	}

	@Override
	public void run() {
		try {

			if (search.getType() == SearchTypes.FLOOD_SEARCH) {
				this.executeFloodSearch();
			} else if (this.search.getType() == SearchTypes.K_WALKER_SEARCH){
				this.executeWalkerSearch();
			} else if (this.search.getType() == SearchTypes.AK_WALKER_SEARCH){
				this.executeAdvancedWalkerSearch();
			}

		} catch (IOException e) {
			logger.error(e);
		} catch (XmlRpcException e) {
			logger.error(e);
		}

	}

	private void executeFloodSearch() throws IOException, XmlRpcException {
		for (Map.Entry<Integer, Peer> entry : this.app.getNeighborList().entrySet()) {
			Peer peer = entry.getValue();
			this.executeSearch(peer);
		}
	}

	private void executeWalkerSearch() throws IOException, XmlRpcException {

		// All neighbors are potential targets
		ArrayList<Peer> possibleTargets = new ArrayList<Peer>(this.app.getNeighborList().values());

		// ... except for the ones we already sent this search to
		if (this.app.getSearchList().containsKey(this.search.getId())) {
			possibleTargets.removeAll(this.app.getSearchList().get(this.search.getId()));
		}

		if (possibleTargets.size() > 0) {
			Random rand = new Random();

			this.executeSearch(possibleTargets.get(rand.nextInt(possibleTargets.size())));
		}
	}

	private void executeAdvancedWalkerSearch() throws IOException, XmlRpcException {
		// TODO

		AdvancedWalkerSearch aWalkerSearch = (AdvancedWalkerSearch)this.search;

		((AdvancedWalkerSearch)this.search).addToPath(this.peer);

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
			if (!this.app.getSearchList().get(this.search.getFilename()).contains(entry.getKey())) {
				v.add(entry);
			}
		}
		
		if (v.size() > 0) {
			Map.Entry<Peer, Double> peerSearch = this.app.randomDrawDelete(v);
			this.executeSearch(peerSearch.getKey());
			this.app.neighborWeight.get(this.search.getFilename()).put(peerSearch.getKey(), peerSearch.getValue() / AdvancedWalkerSearch.DEC);
			this.app.normalizeWeight(this.search.getFilename());
		}


	}

	private void executeSearch(Peer peer) throws IOException, XmlRpcException{
		this.app.addToSearchList(this.search.getId(), peer);
		this.client = ClientRequestFactory.getClient("http://" + peer.getIP() + ':' + peer.getPort() + '/');
		this.client.execute("communication.respondSearch", this.search.toVector());
	}

}
