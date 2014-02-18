package dk.au.cs.p2pn.india.tasks;


import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;
import dk.au.cs.p2pn.india.helper.NeighborNegotiationState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Vector;

public class BecomeNeighborTask extends DefaultTask {


	private static final Logger logger = LogManager.getLogger(BecomeNeighborTask.class.getSimpleName());

	public BecomeNeighborTask(String targetIp, int targetPort, Peer peer, PeerApp app){
		super(targetIp, targetPort, peer, app);
	}

	public void execute(){
		try {

			// Create the client, identifying the server
			this.client = new XmlRpcClient("http://" + ip + ':' + port + '/');
			logger.debug("{} Connection establish to {}:{}", this.peer.getId(), this.ip, this.port);

			// Issue a request

			Vector<Object> params = PeerApp.createVectorForPeer(this.peer);

			params.add(true); // Set flag for Neighbor Request
			app.setNeighborRequest(ip + ":" + port, NeighborNegotiationState.REQUEST_SENT);


			Vector result = (Vector)this.client.execute("communication.pong", params);
			if(result == null){
				logger.debug("No result from Discovery");
			}else{
				// Process the answer.
				this.app.receiveConnectionAnswer(result);
			}


		} catch (IOException e) {
			//logger.error(e.getMessage());
			//e.printStackTrace();
			//app.removePeer(peer);

		} catch (XmlRpcException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
}