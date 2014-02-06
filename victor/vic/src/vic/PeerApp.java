package vic;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.*;
import vic.Tasks.*;

public class PeerApp {

	Peer peer;
	Map<Integer, Peer> peerList;

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
	 */
	public PeerApp(int id, String ip, int port, int capacity, int max) {
        logger.info("Started Peer with ID {}", id);
		this.peer = new Peer(id, ip, port, capacity);//creation of the Peer
		this.peerList =  Collections.synchronizedMap(new HashMap<Integer, Peer>());	// initialize the peerList
		this.maxDepth = max;

        this.server = new ListeningTask(this.peer, this);
		Thread listening = new Thread(this.server);   //start listening to the port
		listening.start();
	}

	public Set<Map.Entry<Integer, Peer>> getPeerSet() {		// return the set of peers in the peerList
		Set<Map.Entry<Integer, Peer>> peerSet = peerList.entrySet();
		return peerSet;
	}

	public String plist() {					//print the peerList on the console
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

	public void helloAll() {

        Thread peerExchange = new Thread(new PeerExchangeTask(this.peer, this));
        peerExchange.start();


	}

    public void destroyPeer(){
        this.server.shutdown();
        logger.debug("shutdown server successfull?");
    }

    /**
     * Adds a peer to the Peerlist.
     * @param peer
     */
    public synchronized void addPeer(Peer peer){
        this.peerList.put(peer.getId(), peer);
    }

    public synchronized void addPeer(Vector data){
        Peer peer = createPeerFromVector(data);
        this.addPeer(peer);
    }

    public synchronized void addPeers(Map<String, Vector> data){
        for(Vector rawPeer : data.values()){
            this.addPeer(rawPeer);
        }
    }

    public synchronized Map<Integer, Peer> getPeerList(){
        return new HashMap<Integer, Peer>(this.peerList);
    }

    /**
     * Connects to the specified adress.
     * @param ip IP-Address of the targeted peer.
     * @param port Port of the targeted peer.
     */
	public void ping(String ip, int port) {
		Thread connection = new Thread(new ConnectionTask(ip, port, this.peer, this, this.maxDepth));
		connection.start();
	}




    /**
     * Creates request parameters for the current peer.
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

    public static Hashtable<String,Vector> createExchangeData(Map<Integer,Peer> rawData){
        Hashtable<String, Vector> result = new Hashtable<String, Vector>();

        for (Map.Entry<Integer, Peer> entry: rawData.entrySet()) {
            // TODO: fix depth parameter
            result.put(Integer.toString(entry.getKey()), createVectorForPeer(entry.getValue(), 0));
        }

        return result;
    }

    public static Peer createPeerFromVector(Vector data){
        return new Peer((Integer)data.get(0),
                (String)data.get(1),
                (Integer) data.get(2),
                (Integer) data.get(3));
    }


}
