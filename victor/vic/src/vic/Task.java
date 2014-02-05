package vic;

import org.apache.xmlrpc.XmlRpcClient;


abstract class Task implements Runnable {
    String ip;
    int port;
    Peer peer;
    PeerApp app;

    XmlRpcClient client;

    public Task(String ip, int port, Peer peer, PeerApp app) {
        this.ip = ip;
        this.port = port;

        this.peer = peer;
        this.app = app;
    }

    @Override
    abstract public void run();
}
