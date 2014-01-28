package vic;

public class Peer {
	int id; // the id number for each Peer
	String adress; // the IP adress 
	int port; // the port number  
	int capacity; // the maximum number of neighbours
	
	/*
	 * Constructor of the class Peer
	 */
	Peer(int idC, String adressC, int portC, int capacityC){
		id = idC; // the id number for each Peer
		adress = adressC; // the IP adress 
		port = portC; // the port number  
		capacity = capacityC; // the maximum number of neighbours
	}
	
	Peer(){
		
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAdress() {
		return adress;
	}

	public void setAdress(String adress) {
		this.adress = adress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

}
