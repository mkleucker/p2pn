package vic.Tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import vic.Peer;
import vic.PeerApp;

import java.io.IOException;
import java.util.Vector;


public class ConnectionTask extends DefaultTask {

    private int maxDepth;

    private static final Logger logger = LogManager.getLogger(ConnectionTask.class.getName());

    public ConnectionTask(String targetIp, int targetPort, Peer peer, PeerApp app, int maxDepth){
        this(targetIp, targetPort, peer, app, maxDepth, false);
    }

    public ConnectionTask(String targetIp, int targetPort, Peer peer, PeerApp app, int maxDepth, boolean neighborRequest){
        super(targetIp, targetPort, peer, app);
        this.maxDepth = maxDepth;
        this.neighborRequest = neighborRequest;
    }

    public void run(){
        try {

            // Create the client, identifying the server
            this.client = new XmlRpcClient("http://" + ip + ':' + port + '/');
            logger.debug("{} Connection establish to {}:{}", this.peer.getId(), this.ip, this.port);

            // Issue a request

            Vector params = PeerApp.createVectorForPeer(this.peer, this.maxDepth);

            if(neighborRequest){
                params.add(neighborRequest);
            }

            Vector result = (Vector)this.client.execute("communication.pong", params);
            if(result == null){
                logger.debug("No result from Discovery");
                return;
            }else{
                // Add the peer to my peerlist.
                this.app.addPeer(result);
            }


		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (XmlRpcException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
