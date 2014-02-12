package vic;


public class Checking implements Runnable {
	
	PeerApp peerApp;
	
	public Checking(PeerApp peer) {
		peerApp = peer; 
	}

	public void run (){
		peerApp.checkConnection();
	}

}
