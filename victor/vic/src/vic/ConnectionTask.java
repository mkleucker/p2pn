package vic;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;


public class ConnectionTask extends Task{
    public ConnectionTask(String ip, int port, Peer peer, PeerApp app){
        super(ip, port, peer, app);
    }

    public void run(){
        try {

            // Create the client, identifying the server
            this.client = new XmlRpcClient("http://" + ip + ':' + port + '/');
            logger.debug("{} Connection establish to {}:{}", this.peer.getId(), this.ip, this.port);

            // Issue a request
            Vector result = (Vector)client.execute("communication.pong",createVectorForPeer(this.peer, maxdepth));
            if(result == null){
                logger.debug("No result from Discovery");
                return;
            }
            /**
             * then add the peers in this vector to the peer list of the current peer;
             */
            for (Map.Entry<String, Vector> entry: ((Hashtable<String, Vector>) result).entrySet()) {
                peerList.put(Integer.parseInt(entry.getKey()), createPeerFromVector(entry.getValue()));
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
