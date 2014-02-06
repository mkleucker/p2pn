package vic;

public class Peer {
	int id; // the id number for each Peer
	String ip; // the IP 
	int port; // the port number  
	int capacity; // the maximum number of neighbors
	
	/**
	 * Constructor of the class Peer
	 * 
	 * @param id
	 * @param ip
	 * @param port
	 * @param capacity
	 */
	Peer(int id, String ip, int port, int capacity){
		this.id = id; // the id number for each Peer
		this.ip = ip; // the IP 
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

	public String getIP() {
		return ip;
	}

	public void setIP(String ip) {
		this.ip = ip;
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
		if(p.getId() == getId())
			return true;
		return false;
	}
}
