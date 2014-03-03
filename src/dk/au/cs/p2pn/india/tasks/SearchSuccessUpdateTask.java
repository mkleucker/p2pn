package dk.au.cs.p2pn.india.tasks;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;

import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;
import dk.au.cs.p2pn.india.communication.ClientRequestFactory;
import dk.au.cs.p2pn.india.search.AdvancedWalkerSearch;

/**
 * The task to call the updating function of the next peer to update the weight along the path.
 * @author johnny
 *
 */
public class SearchSuccessUpdateTask extends DefaultAsyncTask {

	private AdvancedWalkerSearch search;
	private static final Logger logger = LogManager.getLogger(SearchStartTask.class.getSimpleName());
	
	/**
	 * 
	 * @param search: we will only use the path and the file name.
	 * @param ownerApp: we don't use it.
	 */
	public SearchSuccessUpdateTask(AdvancedWalkerSearch search, PeerApp ownerApp) {
		super(ownerApp);

		this.search = search;
		this.search.setSuccess(this.peer);
	}


	@Override
	public void run() {
		logger.info("Inside SearchSuccessTask, establishing connection");

		try {
			/** The destination peer is stored in the penultimate position of the path. */
			Peer dest = search.getPath().elementAt(search.getPath().size() - 2);
			this.client = ClientRequestFactory.getClient("http://" + dest.getIP() + ':' + dest.getPort() + '/');
			this.client.execute("communication.updateSuccess", search.toVector());
		} catch (IOException e) {
			logger.error(e);
		} catch (XmlRpcException e) {
			logger.error(e);
		}
	}
}
