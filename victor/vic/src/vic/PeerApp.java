package vic;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.*;
import vic.Tasks.*;
import vic.Helper.*;

public class PeerApp {

	Peer peer;
	Map<Integer, Peer> peerList;
    Map<Integer, Peer> neighborList;
    Map<Integer, Date> lastSeenList;

    Map<Integer, NeighborNegotiationState> openNeighborRequests;

	private static final Logger logger = LogManager.getLogger(PeerApp.class.getName());
	int maxDepth;

	/**
	 *used to ensure peers finished saying ping before some 
	 *other peers come and start to say ping
	 */
	Lock lock = new ReentrantLock();


	ListeningTask server;

	/**
	 * Constructor of the class PeerApp
	 * 
	 * @param id
	 * @param ip
	 * @param port
	 * @param capacity
	 * @param max
	 */
	public PeerApp(int id, String ip, int port, int capacity, int max) {
		logger.info("Started Peer with ID {}", id);
		this.peer = new Peer(id, ip, port, capacity);//creation of the Peer
		this.peerList =  Collections.synchronizedMap(new HashMap<Integer, Peer>());
		this.neighborList =  Collections.synchronizedMap(new HashMap<Integer, Peer>());
        this.lastSeenList = Collections.synchronizedMap(new HashMap<Integer, Date>());
        this.openNeighborRequests = Collections.synchronizedMap(new HashMap<Integer, NeighborNegotiationState>());
		this.maxDepth = max;

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
		Set<Map.Entry<Integer, Peer>> peerSet = peerList.entrySet();
		return peerSet;
	}

	/**
	 * String for print the peerList on the console
	 * 
	 * @return A string with the right format to be printed as a list in the console. 
	 */
	public String plist() {					
		String str = "List of known peers to Peer "+this.getId()+" ("+this.getIP()+":"+this.getPort()+"):\n";
		Set<Map.Entry<Integer, Peer>> peerSet = getPeerSet();
		for (Map.Entry<Integer, Peer> entry: peerSet) {
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

	public void setIP(String ip) {
		peer.setIP(ip);
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
	public void destroyPeer(){
		this.server.shutdown();
		logger.debug("shutdown server successfull?");
	}

	/**
	 * Adds a peer to the Peerlist.
	 * 
	 * @param peer
	 */
    public synchronized void addPeer(Peer peer){
        this.addPeer(peer, false);
    }

    public synchronized void addPeer(Peer peer, boolean neighbornegotiation){
		this.peerList.put(peer.getId(), peer);
        this.lastSeenList.put(peer.getId(), new Date());

        if (this.openNeighborRequests.containsKey(peer.getId()) &&
                this.openNeighborRequests.get(peer.getId()) == NeighborNegotiationState.REQUEST_SENT ){

//            this.openNeighborRequests.remove(peer.getId());
//
//            if(neighbornegotiation){
//                this.addNeighbor(peer);
//            }
        }
	}

    /**
     * Adds a peer to list of neighbors.
     * @param peer
     */
    public synchronized void addNeighbor(Peer peer){
        this.neighborList.put(peer.getId(), peer);
        this.updateLastSeen(peer);
    }

    /**
     * Returns a copy of the list of lastSeenList.
     * @return
     */
    public synchronized Map<Integer, Date> getLastSeenList(){
        return new HashMap<Integer, Date>(this.lastSeenList);
    }

    /**
     * Converts the Map neighborlist to an HashMap
     */
    public synchronized Map<Integer, Peer> getNeighborList(){
        return new HashMap<Integer, Peer>(this.neighborList);
    }


	/**
	 * Creates a Peer from a vector and the calls
	 * addPeer method for add the peer to the Peerlist.
	 *
	 * @param data
	 */
	public synchronized void addPeer(Vector data){
		Peer peer = createPeerFromVector(data);
        // TODO: Check if peer is already in the system.
		this.addPeer(peer);
	}

	/**
	 * Add a set of peers to the Peerlist.
	 *
	 * @param data
	 */
	public synchronized void addPeers(Map<String, Vector> data){
		for(Vector rawPeer : data.values()){
			this.addPeer(rawPeer);
		}
	}

	/**
	 * Converts the Map peerlist to an HashMap
	 */
	public synchronized Map<Integer, Peer> getPeerList(){
		return new HashMap<Integer, Peer>(this.peerList);
	}

    /**
     * Remove a peer from all my peer-lists.
     * @param peer
     */
    public synchronized void removePeer(Peer peer){
        Integer peerId = peer.getId();
        this.peerList.remove(peerId);
        this.neighborList.remove(peerId);
        this.lastSeenList.remove(peerId);
    }
    /**
     * Updates the timestamp for th last time a peer has been
     * communicated with.
     * @param peer
     */
    private void updateLastSeen(Peer peer){
        this.lastSeenList.put(peer.getId(), new Date());
    }

	/**
	 * Connects to the specified adress.
	 *
	 * @param ip IP-Address of the targeted peer.
	 * @param port Port of the targeted peer.
	 */
	public void ping(String ip, int port) {
		Thread connection = new Thread(new ConnectionTask(ip, port, this.peer, this, this.maxDepth));
		connection.start();
	}

	/**
	 * Changes the format of the data for the sending process.
	 * From a HashMap<Integer,Peer> to a Hashtable<String, Vector>
	 *
	 * @param rawData
	 * @return Hashtable with the format: Hashtable<String, Vector>
	 */
	private static Hashtable<String,Vector> createExchangeData(HashMap<Integer,Peer> rawData){
		Hashtable<String, Vector> result = new Hashtable<String, Vector>();

		for (Map.Entry<Integer, Peer> entry: rawData.entrySet()) {
			// TODO: fix depth parameter
			result.put(Integer.toString(entry.getKey()), createVectorForPeer(entry.getValue(), 0));
		}

		return result;
	}

	/**
	 * Creates request parameters for the current peer.
	 *
	 * @return Vector with all parameters
	 */
	public static Vector<Object> createVectorForPeer(Peer peer, int depth){
		Vector<Object> params = new Vector<Object>();
		params.addElement(peer.getId());
		params.addElement(peer.getIP());
		params.addElement(peer.getPort());
		params.addElement(peer.getCapacity());
		params.addElement(depth);

		return params;
	}

	/**
	 * Changes the format of the data for the sending process.
	 * From a Map<Integer,Peer> to a Hashtable<String, Vector>
	 * 
	 * @param rawData
	 * @return Hashtable with the format: Hashtable<String, Vector>
	 */
	public static Hashtable<String,Vector> createExchangeData(Map<Integer,Peer> rawData){
		Hashtable<String, Vector> result = new Hashtable<String, Vector>();

		for (Map.Entry<Integer, Peer> entry: rawData.entrySet()) {
			// TODO: fix depth parameter
			result.put(Integer.toString(entry.getKey()), createVectorForPeer(entry.getValue(), 0));
		}

		return result;
	}

	/**
	 * Creates a Peer from a Vector. 
	 * 
	 * @param data
	 * @return a new Peer.
	 */
	public static Peer createPeerFromVector(Vector data){
		return new Peer((Integer)data.get(0),
				(String)data.get(1),
				(Integer) data.get(2),
				(Integer) data.get(3));
	}

    public void startNegotiate () {
        Set<Map.Entry<Integer, Peer>> peerSet = peerList.entrySet();
        if (peerSet.size() < 1){
            return;
        }
        Vector<Peer> peers = new Vector<Peer>();
        double[] c = new double[peerSet.size()];
        for (Map.Entry<Integer, Peer> entry: peerSet) {
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
            if(r < c[i]) {
                try {
                    Peer itPeer = peers.get(i);
                    ConnectionTask connect = new ConnectionTask(itPeer.getIP(), itPeer.getPort(), itPeer, this, 1, true);
                    connect.run();
                    break;
                } catch (Exception e) {
                    logger.error("Function startNegotiate failed, probably because the peer no longer exists, error message {}", e.getMessage());
                }
            }
        }
    }
    
    /**
	 * Check the connection with all the Peers of the peerList
	 * and makes and update of the peerlist if there's any peer died. 
	 * 
	 */
	public void checkConnection(){
		if(randInt(1,5) == 3){
			for (Map.Entry<Integer, Peer> entry: peerList.entrySet()) {
				ping(entry.getValue().getIP(),entry.getValue().getPort());					
			}
		}
	}
	
	
	/**
	 * It returns a random number between min and max parameters. 
	 * 
	 * @param min
	 * @param max
	 * @return random number
	 */
	public static int randInt(int min, int max) {
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
}
