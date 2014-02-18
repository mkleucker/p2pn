package dk.au.cs.p2pn.india.tasks;


import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;

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