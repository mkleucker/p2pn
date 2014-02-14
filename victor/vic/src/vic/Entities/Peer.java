package vic.Entities;

public class Peer {
	int id; // the id number for each Peer
	String ip; // the IP 
	int port; // the port number  
	int capacity; // the maximum number of neighbors
	
	/**
	 * Constructor of the class Peer
	 * 
	 * @param id ID of the peer
	 * @param ip IP of the peer
	 * @param port Port of the peer
	 * @param capacity Capacity of the peer
	 */
	public Peer(int id, String ip, int port, int capacity){
		this.id = id; // the id number for each Peer
		this.ip = ip; // the IP 
		this.port = port; // the port number  
		this.capacity = capacity; // the maximum number of neighbors
	}
		
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIP() {
		return ip;
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
	
	public boolean equals(Peer p) {
		return p.getId() == this.getId();
	}
}
