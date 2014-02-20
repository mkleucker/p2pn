package dk.au.cs.p2pn.india.tasks;

import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;
import dk.au.cs.p2pn.india.helper.CommunicationConverter;
import dk.au.cs.p2pn.india.helper.ReporterMeasurements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Vector;

public class SearchTask extends DefaultAsyncTask implements Runnable{

	public String fileName;
	private static final Logger logger = LogManager.getLogger(SearchTask.class.getSimpleName());

	public SearchTask(PeerApp app, String fileName) {
		super(app);
		this.fileName = fileName;
	}

	@Override
	public void run() {
		try {
			// Create the client, identifying the server
			this.client = new XmlRpcClient("http://" + ip + ':' + port + '/');

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
			params.add(Integer(ttl));
			params.add(id);

			this.client.execute("communication.search", params);


		} catch (IOException e) {
		} catch (XmlRpcException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

}
