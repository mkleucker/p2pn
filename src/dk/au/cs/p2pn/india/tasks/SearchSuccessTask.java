package dk.au.cs.p2pn.india.tasks;

import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;
import dk.au.cs.p2pn.india.communication.ClientRequestFactory;
import dk.au.cs.p2pn.india.helper.CommunicationConverter;
import dk.au.cs.p2pn.india.search.AdvancedWalkerSearch;
import dk.au.cs.p2pn.india.search.BasicSearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;

/**
 * 
 * @author johnny
 * SearchSuccessTask: used when a peer is asked for some file it has. This task will send a message 
 * back to the origin and tell him this peer has the file.
 * 
 * Format of the message:
 * 
 * Origin:Peer(represented as a vector)
 * 
 * File name:String
 * 
 * Identifier of this search:String
 * 
 * Owner of the file:Peer
 */
@SuppressWarnings("unused")
public class SearchSuccessTask extends DefaultAsyncTask {

	private static final Logger logger = LogManager.getLogger(SearchSuccessTask.class.getSimpleName());

	private BasicSearch search;

	public SearchSuccessTask(BasicSearch search, PeerApp ownerApp) {
		super(ownerApp);

		this.search = search;
		this.search.setSuccess(this.peer);
	}

	@Override
	public void run() {
		logger.info("Inside SearchSuccessTask, establishing connection");

		try {
			executeDirectSuccess();
		} catch (IOException e) {
			logger.error(e);
		} catch (XmlRpcException e) {
			logger.error(e);
		}
	}

	private void executeDirectSuccess() throws IOException, XmlRpcException{
		this.client = ClientRequestFactory.getClient("http://" + search.getSource().getIP() + ':' + search.getSource().getPort() + '/');
		this.client.execute("communication.respondSuccess", search.toVector());
	}

}
