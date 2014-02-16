package vic;

import java.util.*;

import org.apache.logging.log4j.*;
import vic.Entities.Peer;
import vic.Tasks.*;
import vic.Helper.*;

public class PeerApp {

	Peer peer;
	Map<Integer, Peer> peerList;
	Map<Integer, Peer> neighborList;
	Map<Integer, Date> lastSeenList;
	Map<String, NeighborNegotiationState> openNeighborRequests;

	private static final Logger logger = LogManager.getLogger(PeerApp.class.getName());

	ListeningTask server;

	/**
	 * Constructor of the class PeerApp
	 *
     * @param id       ID of the Peer
     * @param ip       IP Address of the Peer
     * @param port     Port on which this Peer listens
     * @param capacity Capacity of this Peer
     */
	public PeerApp(int id, String ip, int port, int capacity) {
		logger.info("Started Peer with ID {}", id);
		this.peer = new Peer(id, ip, port, capacity);//creation of the Peer
		this.peerList = Collections.synchronizedMap(new HashMap<Integer, Peer>());
		this.neighborList = Collections.synchronizedMap(new HashMap<Integer, Peer>());
		this.lastSeenList = Collections.synchronizedMap(new HashMap<Integer, Date>());
		this.openNeighborRequests = Collections.synchronizedMap(new HashMap<String, NeighborNegotiationState>());

		this.server = new ListeningTask(this.peer, this);
		Thread listening = new Thread(this.server);   //start listening to the port
		listening.start();
	}

	/**
	 * Return the set of peers in the peerList
	 *
	 * @return Set of Peers.
	 */
	public Set<Map.Entry<Integer, Peer>> getPeerSet() {
		return peerList.entrySet();
	}

	/**
	 * String for print the peerList on the console
	 *
	 * @return A string with the right format to be printed as a list in the console.
	 */
	public String plist() {
		String str = "List of known peers to Peer " + this.getId() + " (" + this.getIP() + ":" + this.getPort() + "):\n";
		Set<Map.Entry<Integer, Peer>> peerSet = getPeerSet();
		for (Map.Entry<Integer, Peer> entry : peerSet) {
			Peer peer = entry.getValue();
			str += "Name: P" + peer.getId() + "  ";
			str += "IP: " + peer.getIP() + "  ";
			str += "Port: " + peer.getPort() + "  ";
			str += "Capacity: " + peer.getCapacity();
			str += "\n";
		}

		return str;
	}

	/**
	 * String for print the peerList on the console
	 *
	 * @return A string with the right format to be printed as a list in the console.
	 */
	public String nlist() {
		String str = "List of neighbors to Peer " + this.getId() + " (" + this.getIP() + ":" + this.getPort() + "):\n";
		Set<Map.Entry<Integer, Peer>> peerSet = this.getNeighborList().entrySet();
		for (Map.Entry<Integer, Peer> entry : peerSet) {
			Peer peer = entry.getValue();
			str += "Name: P" + peer.getId() + "  ";
			str += "IP: " + peer.getIP() + "  ";
			str += "Port: " + peer.getPort() + "  ";
			str += "Capacity: " + peer.getCapacity();
			str += "\n";
		}

		return str;
	}

	public int getId() {
		return peer.getId();
	}

	public void setId(int id) {
		peer.setId(id);
	}

	public String getIP() {
		return peer.getIP();
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

	/**
	 * Says hello and creates a connection with all the Peers of the peerlist of our Peer.
	 */
	public void helloAll() {
		Thread peerExchange = new Thread(new PeerExchangeTask(this.peer, this));
		peerExchange.start();
	}

	/**
	 * Destroy the peer
	 */
	public void destroyPeer() {
		this.server.shutdown();
		logger.debug("shutdown server successfull?");
	}

	/**
	 * Adds a peer to the Peerlist.
	 *
	 * @param peer Peer object to add to registry.
	 */
	public synchronized void addPeer(Peer peer) {
		this.peerList.put(peer.getId(), peer);
		this.lastSeenList.put(peer.getId(), new Date());


	}

	/**
	 * Adds a peer to list of neighbors.
	 *
	 * @param peer Peer object to add to the neighbor list
	 */
	public synchronized void addNeighbor(Peer peer, boolean success) {
		if (this.openNeighborRequests.containsKey(peer.getIP() + ':' + peer.getPort()) && this.openNeighborRequests.get(peer.getIP() + ':' + peer.getPort()) == NeighborNegotiationState.REQUEST_SENT) {
			this.openNeighborRequests.remove(peer.getIP() + ":" + peer.getPort());
		}
		if(success){
			this.neighborList.put(peer.getId(), peer);
			this.updateLastSeen(peer);
		}
	}

	/**
	 * Returns a copy of the list of lastSeenList.
	 *
	 * @return HashMap containing all LastSeen information
	 */
	public synchronized Map<Integer, Date> getLastSeenList() {
		return new HashMap<Integer, Date>(this.lastSeenList);
	}

	/**
	 * Converts the Map neighborlist to an HashMap
	 */
	public synchronized Map<Integer, Peer> getNeighborList() {
		return new HashMap<Integer, Peer>(this.neighborList);
	}

	public synchronized void setNeighborRequest(String address, NeighborNegotiationState state) {
		this.openNeighborRequests.put(address, state);
	}

	/**
	 * Creates a Peer from a vector and the calls
	 * addPeer method for add the peer to the Peerlist.
	 *
	 * @param data Single Vector object containing the data of a Peer.
	 */
	public synchronized void addPeer(Vector data) {
		Peer peer = createPeerFromVector(data);
		this.addPeer(peer);
		// TODO: Check if peer is already in the system.
	}


	public synchronized void receiveConnectionAnswer(Vector data){
		logger.debug("Received Connection answer with length {}", data.size());
		if(data.size() == 6){
			boolean neighborResponse = (Boolean) data.get(5);
			data.setSize(4);
			Peer peer = createPeerFromVector(data);
			this.addPeer(peer);
			this.addNeighbor(peer, neighborResponse);
		}else{
			this.addPeer(createPeerFromVector(data));
		}
	}

	/**
	 * Add a set of peers to the Peerlist.
	 *
	 * @param data Map of String - Vector pairs, where the Vector is a representation of a Peer.
	 */
	public synchronized void addPeers(Map<String, Vector> data) {
		for (Vector rawPeer : data.values()) {
			this.addPeer(rawPeer);
		}
	}

	/**
	 * Converts the Map peerlist to an HashMap
	 */
	public synchronized Map<Integer, Peer> getPeerList() {
		return new HashMap<Integer, Peer>(this.peerList);
	}

	/**
	 * Remove a peer from all my peer-lists.
	 *
	 * @param peer Peer object to remove form my registry
	 */
	public synchronized void removePeer(Peer peer) {
		Integer peerId = peer.getId();
		this.peerList.remove(peerId);
		this.neighborList.remove(peerId);
		this.lastSeenList.remove(peerId);
	}

	/**
	 * Updates the timestamp for th last time a peer has been
	 * communicated with.
	 *
	 * @param peer Peer object to update timestamp on
	 */
	private void updateLastSeen(Peer peer) {
		this.lastSeenList.put(peer.getId(), new Date());
	}

	/**
	 * Connects to the specified adress.
	 *
	 * @param ip   IP-Address of the targeted peer.
	 * @param port Port of the targeted peer.
	 */
	public void ping(String ip, int port) {
		Thread connection = new Thread(new ConnectionTask(ip, port, this.peer, this));
		connection.start();
	}

	/**
	 * Changes the format of the data for the sending process.
	 * From a HashMap<Integer,Peer> to a Hashtable<String, Vector>
	 *
	 * @param rawData HashMap of Integer - Peer pairs
	 * @return Hashtable with the format: Hashtable<String, Vector>
	 */
	private static Hashtable<String, Vector> createExchangeData(HashMap<Integer, Peer> rawData) {
		Hashtable<String, Vector> result = new Hashtable<String, Vector>();

		for (Map.Entry<Integer, Peer> entry : rawData.entrySet()) {
			// TODO: fix depth parameter
			result.put(Integer.toString(entry.getKey()), createVectorForPeer(entry.getValue()));
		}

		return result;
	}

	/**
	 * Creates request parameters for the current peer.
	 *
	 * @return Vector with all parameters
	 */
	public static Vector<Object> createVectorForPeer(Peer peer) {
		Vector<Object> params = new Vector<Object>();
		params.addElement(peer.getId());
		params.addElement(peer.getIP());
		params.addElement(peer.getPort());
		params.addElement(peer.getCapacity());

		return params;
	}

	/**
	 * Changes the format of the data for the sending process.
	 * From a Map<Integer,Peer> to a Hashtable<String, Vector>
	 *
	 * @param rawData Map of Peers in their Vector representation.
	 * @return Hashtable with the format: Hashtable<String, Vector>
	 */
	public static Hashtable<String, Vector> createExchangeData(Map<Integer, Peer> rawData) {
		Hashtable<String, Vector> result = new Hashtable<String, Vector>();

		for (Map.Entry<Integer, Peer> entry : rawData.entrySet()) {
			// TODO: fix depth parameter
			result.put(Integer.toString(entry.getKey()), createVectorForPeer(entry.getValue()));
		}

		return result;
	}

	/**
	 * Creates a Peer from a Vector.
	 *
	 * @param data Vector representation of a peer
	 * @return a new Peer.
	 */
	public static Peer createPeerFromVector(Vector data) {
		return new Peer((Integer) data.get(0),
				(String) data.get(1),
				(Integer) data.get(2),
				(Integer) data.get(3));
	}

	public void startNegotiate() {
		Set<Map.Entry<Integer, Peer>> peerSet = peerList.entrySet();
		if (peerSet.size() < 1) {
			return;
		}
		Vector<Peer> peers = new Vector<Peer>();
		double[] c = new double[peerSet.size()];
		for (Map.Entry<Integer, Peer> entry : peerSet) {
			peers.add(entry.getValue());
		}
		c[0] = peers.get(0).getCapacity();
		for (int i = 1; i < peers.size(); i++) {
			c[i] = c[i - 1] + peers.get(i).getCapacity();
		}
		for (int i = 0; i < peers.size(); i++) {
			c[i] = c[i] / c[peers.size() - 1];
		}
		double r = Math.random();
		for (int i = 0; i < peers.size() - 1; i++) {
			if (r < c[i]) {
				try {
					Peer itPeer = peers.get(i);
					ConnectionTask connect = new ConnectionTask(itPeer.getIP(), itPeer.getPort(), itPeer, this, true);
					connect.run();
					break;
				} catch (Exception e) {
					logger.error("Function startNegotiate failed, probably because the peer no longer exists, error message {}", e.getMessage());
				}
			}
		}
	}

	public void becomeNeighbor(String ip, int port) {
		Thread connection = new Thread(new ConnectionTask(ip, port, this.peer, this, true));
		connection.start();
	}

	public void nlistGraph(ArrayList<Peer> peers, String dir) {
		PrintWriter output;
		if (dir.isEmpty()) {
			output = new PrintWriter(System.out);
		} else {
			String fileName = dir.substring(3, dir.length());
			output = new PrintWriter(fileName);
		}
		Vector<Peer> pvTemp = new Vector<Peer>();
		Vector<Peer> pv = new Vector<Peer>();
		if (peers == null) {
			pv.add(this.peer);
		} else {
			for (int i = 0; i < peers.size(); i++) {
				pvTemp.add(peers.get(i));
			}
			int min;
			for (int i = 0; i < pvTemp.size(); i++) {
				min = 0;
				for (int j = 1; j < pvTemp.size(); j++) {
					if (pvTemp.get(j).smallerThan(pvTemp.get(min))) {
						min = j;
					}
				}
				pv.add(pvTemp.get(min));
			}
		}

		for (int i = 0; i < pv.size(); i++) {
		}
	}
}
