package vic;

import java.util.ArrayList;

public class PeerApp {
	
	Peer peer;
	ArrayList<Peer> peerList;

	
	/*
	 * Constructor of the class PeerApp
	 */
	PeerApp(int idC, String adressC, int portC, int capacityC){
		peer = new Peer(idC,adressC,portC,capacityC);//creation of the Peer
		peerList = new ArrayList<Peer>();
	}
	
	public ArrayList<Peer> getPeer(){
		return peerList;
	}
	
	public int getId() {
		return peer.getId();
	}

	public void setId(int id) {
		peer.setId(id);
	}

	public String getAdress() {
		return peer.getAdress();
	}

	public void setAdress(String adress) {
		peer.setAdress(adress);
	}

	public int getPort() {
		return peer.getPort();
	}

	public void setPort(int port) {
		peer.setPort(port);
	}

	public int getCapacity() {
		return peer.getCapacity();
	}

	public void setCapacity(int capacity) {
		peer.setCapacity(capacity);
	}
	
	

	
	
}
