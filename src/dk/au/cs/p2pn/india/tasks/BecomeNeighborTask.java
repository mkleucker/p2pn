package dk.au.cs.p2pn.india.tasks;


import dk.au.cs.p2pn.india.PeerApp;
import dk.au.cs.p2pn.india.communication.ClientRequestFactory;
import dk.au.cs.p2pn.india.helper.CommunicationConverter;
import dk.au.cs.p2pn.india.helper.NeighborNegotiationState;
import dk.au.cs.p2pn.india.reporting.Reporter;
import dk.au.cs.p2pn.india.reporting.ReporterMeasurements;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Vector;

public class BecomeNeighborTask extends DefaultTask {


	private static final Logger logger = LogManager.getLogger(BecomeNeighborTask.class.getSimpleName());

	public BecomeNeighborTask(String targetIp, int targetPort, PeerApp app){
		super(targetIp, targetPort, app);
	}

	public boolean execute(){
		try {
			//logger.debug("before the connection of {} to {}:{}", this.peer.getId(), this.ip, this.port);

			// Create the client, identifying the server
			this.client = ClientRequestFactory.getClient("http://" + ip + ':' + port + '/');

			// Issue a request

			Vector<Object> params = CommunicationConverter.createVector(this.peer, true);

			app.setNeighborRequest(ip + ":" + port, NeighborNegotiationState.REQUEST_SENT);
			Reporter.addEvent(ReporterMeasurements.NEIGHBOR_REQUEST_SENT);

			Vector result = (Vector)this.client.execute("communication.pong", params);
			if(result == null){
				logger.debug("No result from Discovery");
				return false;
			}else{
				// Process the answer.
				boolean returnVal = (Boolean) result.get(4);
				this.app.receiveConnectionAnswer(result);
				return returnVal;
			}



		} catch (IOException e) {
			//logger.error(e.getMessage());
			//e.printStackTrace();
			app.removePeer(peer);

		} catch (XmlRpcException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return false;
	}
}
