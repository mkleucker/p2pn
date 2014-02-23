package dk.au.cs.p2pn.india.tasks;

import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;
import dk.au.cs.p2pn.india.communication.ClientRequestFactory;
import dk.au.cs.p2pn.india.helper.CommunicationConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * 
 * @author johnny
 *
 * SearchTask used to start a search process for a specific file using flooding across the whole network.
 * 
 * Format of the message:
 * Origin: 						Peer   (represented as a vector)
 * File name: 					String
 * Time to live: 				Integer
 * Identifier of this search: 	String
 * 
 */

public class SearchTask extends DefaultAsyncTask implements Runnable{

	public String fileName;
	public int ttl;
	public String ident;
	private static final Logger logger = LogManager.getLogger(SearchTask.class.getSimpleName());

	public SearchTask(PeerApp app, String fileName, int ttl, String ident) {
		super(app);
		this.fileName = fileName;
		this.ttl = ttl;
		this.ident = ident;
	}

	@SuppressWarnings({ "unused", "rawtypes" })
	@Override
	public void run() {
		try {
			
			Vector<Object> params = new Vector<Object>();
			params.add(CommunicationConverter.createVector(this.peer));
			params.add(fileName);
			params.add(ttl);
			params.add(ident);

			for (Map.Entry<Integer, Peer> entry : this.app.getNeighborList().entrySet()) {
				Peer itPeer = entry.getValue();
				
				logger.info("Inside searchTask, ready to ask peer {} with IP {} and port {}", itPeer.getId(), itPeer.getIP(), itPeer.getPort());
				// Create the client, identifying the server
				this.client = ClientRequestFactory.getClient("http://" + itPeer.getIP() + ':' + itPeer.getPort() + '/');
				Vector res = (Vector)this.client.execute("communication.respondSearch", params);
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (XmlRpcException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
}
