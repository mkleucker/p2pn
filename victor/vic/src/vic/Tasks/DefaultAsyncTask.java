package vic.Tasks;

import org.apache.xmlrpc.XmlRpcClient;
import vic.Entities.Peer;
import vic.PeerApp;


abstract class DefaultAsyncTask extends DefaultTask implements Runnable {

    public DefaultAsyncTask(Peer peer, PeerApp app){
        super(peer, app);
    }

    public DefaultAsyncTask(String ip, int port, Peer peer, PeerApp app) {
        super(ip, port, peer, app);
    }

    @Override
    abstract public void run();
}
