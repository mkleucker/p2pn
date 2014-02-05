package vic.Tasks;

import org.apache.xmlrpc.XmlRpcClient;
import vic.Peer;
import vic.PeerApp;


abstract class DefaultTask implements Runnable {
    String ip;
    int port;
    Peer peer;
    PeerApp app;

    XmlRpcClient client;

    public DefaultTask(Peer peer, PeerApp app){
        this.peer = peer;
        this.app = app;
    }

    public DefaultTask(String ip, int port, Peer peer, PeerApp app) {
        this.ip = ip;
        this.port = port;

        this.peer = peer;
        this.app = app;
    }

    @Override
    abstract public void run();
}
