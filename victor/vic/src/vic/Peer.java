package vic;

public class Peer {
	int id; // the id number for each Peer
	String address; // the IP address 
	int port; // the port number  
	int capacity; // the maximum number of neighbors
	
	/*
	 * Constructor of the class Peer
	 */
	Peer(int id, String address, int port, int capacity){
		this.id = id; // the id number for each Peer
		this.address = address; // the IP address 
		this.port = port; // the port number  
		this.capacity = capacity; // the maximum number of neighbors
	}
	
	Peer(){
		
	}
		
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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
