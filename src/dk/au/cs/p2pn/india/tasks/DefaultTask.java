package dk.au.cs.p2pn.india.tasks;


import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;
import org.apache.xmlrpc.XmlRpcClient;

abstract public class DefaultTask {
	String ip;
	int port;
	Peer peer;
	PeerApp app;

	XmlRpcClient client;

	public DefaultTask(PeerApp app){
		this.app = app;
		this.peer = app.getPeer();
	}

	public DefaultTask(Peer peer, PeerApp app){
		this.peer = peer;
		this.app = app;
	}

	public DefaultTask(String ip, int port, PeerApp app) {
		this.ip = ip;
		this.port = port;

		this.peer = app.getPeer();
		this.app = app;
	}

}
