package dk.au.cs.p2pn.india.tasks;

import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;
import dk.au.cs.p2pn.india.helper.CommunicationConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

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

	@Override
	public void run() {
		try {
			
			/**
			 * Format of the message:
			 * Origin: 			Peer   (represented as a vector)
			 * File name: 			String
			 * Time to live: 		Integer
			 * Identifier of this search: 	String
			 */
			Vector<Object> params = new Vector<Object>();
			params.add(CommunicationConverter.createVector(this.peer));
			params.add(fileName);
			params.add(new Integer(ttl));
			params.add(ident);

			Set<Map.Entry<Integer, Peer>> peerSet = this.app.getPeerSet();
			for (Map.Entry<Integer, Peer> entry : peerSet) {
				Peer itPeer = entry.getValue();
			
				// Create the client, identifying the server
				this.client = new XmlRpcClient("http://" + itPeer.getIP() + ':' + itPeer.getId() + '/');
				this.client.execute("communication.respondSearch", params);
			}
		} catch (IOException e) {
		} catch (XmlRpcException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
}
