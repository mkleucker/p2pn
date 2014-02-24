package dk.au.cs.p2pn.india.tasks;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import dk.au.cs.p2pn.india.communication.ClientRequestFactory;
import dk.au.cs.p2pn.india.helper.CommunicationConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;

/**
 * @author johnny
 * used to pass the search to other peers when the local peer doesn't have the file
 */
public class PassSearchTask extends DefaultAsyncTask {

	public Vector<Object> params;
	public Vector<Object> origin;
	private static final Logger logger = LogManager.getLogger(SearchTask.class.getSimpleName());

	public PassSearchTask(PeerApp app, Vector<Object> origin, String fileName, Integer ttl, String ident) {
		super(app);
		this.origin = origin;
		
		/**
		 * Format of the message:
		 * Origin: 						Peer   (represented as a vector)
		 * File name: 					String
		 * Time to live: 				Integer
		 * Identifier of this search: 	String
		 */
		this.params = CommunicationConverter.createSearchVector(origin, fileName, ttl, ident);

	}

	@Override
	public void run() {
		try {
			for (Map.Entry<Integer, Peer> entry : this.app.getNeighborList().entrySet()) {
				Peer itPeer = entry.getValue();

				// Create the client, identifying the server
				this.client = ClientRequestFactory.getClient("http://" + itPeer.getIP() + ':' + itPeer.getPort() + '/');
				this.client.execute("communication.respondSearch", params);
			}
		} catch (IOException e) {
			logger.error(e);
		} catch (XmlRpcException e) {
			logger.error(e);
		}

	}

}
