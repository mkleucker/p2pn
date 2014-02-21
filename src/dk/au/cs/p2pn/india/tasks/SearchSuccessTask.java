package dk.au.cs.p2pn.india.tasks;

import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;
import dk.au.cs.p2pn.india.helper.CommunicationConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Vector;

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
public class SearchSuccessTask extends DefaultAsyncTask {

	public Vector<Object> params = new Vector<Object>();
	public Vector<Object> origin;
	private static final Logger logger = LogManager.getLogger(SearchTask.class.getSimpleName());


	public SearchSuccessTask(Vector<Object> origin, String fileName, String ident, PeerApp ownerApp) {
		super(ownerApp);

		this.origin = origin;

		params.add(origin);
		params.add(fileName);
		params.add(ident);
		params.add(CommunicationConverter.createVector(this.app.getPeer()));
	}

	@Override
	public void run() {
		try {
			Peer dest = CommunicationConverter.createPeer(origin);
			logger.info("Inside SearchSuccessTask, establishing connection");

			this.client = new XmlRpcClient("http://" + dest.getIP() + ':' + dest.getPort() + '/');
			this.client.execute("communication.respondSuccess", params);
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (XmlRpcException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

}
