package dk.au.cs.p2pn.india.tasks;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

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
		params = new Vector<Object>();
		params.add(origin);
		params.add(fileName);
		params.add(ttl);
		params.add(ident);
	}

	@Override
	public void run() {
		try {
			Set<Map.Entry<Integer, Peer>> peerSet = this.app.getPeerSet();
			for (Map.Entry<Integer, Peer> entry : peerSet) {
				Peer itPeer = entry.getValue();
				logger.info("Inside passSearchTask, iterating all peers");

				// Create the client, identifying the server
				this.client = new XmlRpcClient("http://" + itPeer.getIP() + ':' + itPeer.getPort() + '/');
				this.client.execute("communication.respondSearch", params);
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (XmlRpcException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return;
	}

}
