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


public class ConnectionAsyncTask extends DefaultAsyncTask {

    private static final Logger logger = LogManager.getLogger(ConnectionAsyncTask.class.getSimpleName());

    public ConnectionAsyncTask(String targetIp, int targetPort, PeerApp app){
        super(targetIp, targetPort, app);
    }

    public void run(){
        try {

            // Create the client, identifying the server
            this.client = new XmlRpcClient("http://" + ip + ':' + port + '/');
            logger.debug("{} Connection establish to {}:{}", this.peer.getId(), this.ip, this.port);

            Vector<Object> params = CommunicationConverter.createVector(this.peer);
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
            this.app.removePeer(peer);

		} catch (XmlRpcException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
