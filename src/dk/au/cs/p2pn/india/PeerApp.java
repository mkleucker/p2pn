package dk.au.cs.p2pn.india;

import dk.au.cs.p2pn.india.download.Receive;
import dk.au.cs.p2pn.india.helper.CommunicationConverter;
import dk.au.cs.p2pn.india.helper.NeighborNegotiationState;
import dk.au.cs.p2pn.india.reporting.Reporter;
import dk.au.cs.p2pn.india.tasks.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class PeerApp {

	Peer peer;
	int searchCount;		// used to generate the id for each search

	Map<Integer, Peer> peerList;
	Map<Integer, Peer> neighborList;
	Map<Integer, Date> lastSeenList;
	Map<String, File> fileList;		//store the file of the local peer, key is file name, value is content
	Vector<String> searchList;			//store the identifier of search, to avoid repetitive search
	Map<String, Peer> knownDataList;	//store the information of data known to the local peer, key is the file name and peer is the owner
	Map<String, NeighborNegotiationState> openNeighborRequests;

	public static double[] POWERLAWCUMULATIVE = new double[10];
	public static double ALPHA;

	private static final Logger logger = LogManager.getLogger(PeerApp.class.getSimpleName());

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
		logger.info("Started Peer with ID {} (Capacity: {})", id, capacity);
		searchCount = 0;
		this.searchList = new Vector<String>();
		this.knownDataList = Collections.synchronizedMap(new HashMap<String, Peer>());
		this.fileList = Collections.synchronizedMap(new HashMap<String, File>());
		this.peer = new Peer(id, ip, port, capacity);//creation of the Peer
		this.peerList = Collections.synchronizedMap(new HashMap<Integer, Peer>());
		this.neighborList = Collections.synchronizedMap(new HashMap<Integer, Peer>());
		this.lastSeenList = Collections.synchronizedMap(new HashMap<Integer, Date>());
		this.openNeighborRequests = Collections.synchronizedMap(new HashMap<String, NeighborNegotiationState>());
		Reporter.init();

		this.server = new ListeningTask(this.peer, this);
		Thread listening = new Thread(this.server);   //start listening to the port
		listening.start();
	}


	/**
	 * Constructor of the class PeerApp with capacity randomly drawn
	 *
	 * @param id       ID of the Peer
	 * @param ip       IP Address of the Peer
	 * @param port     Port on which this Peer listens
	 */
	public PeerApp(int id, String ip, int port) {
		int capacity = 0;
		searchCount = 0;
		this.searchList = new Vector<String>();
		this.knownDataList = Collections.synchronizedMap(new HashMap<String, Peer>());
		this.fileList = Collections.synchronizedMap(new HashMap<String, File>());
		ALPHA = 0.6;
		POWERLAWCUMULATIVE[0] = Math.pow(ALPHA, 1);
		for (int i = 1; i < 10; i++) {
			POWERLAWCUMULATIVE[i] = POWERLAWCUMULATIVE[i - 1] + Math.pow(ALPHA, i + 1);
		}

		for (int i = 1; i < 10; i++) {
			POWERLAWCUMULATIVE[i] /= POWERLAWCUMULATIVE[9];
		}

		double r = Math.random();
		for (int i = 0; i < 10; i++) {
			if (r <= POWERLAWCUMULATIVE[i]) {
				capacity = i;
				break;
			}
		}

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
		String str = "List of known peers to Peer " + this.peer.getId() + " (" + this.peer.getIP() + ":" + this.peer.getPort() + "):\n";
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
		String str = "List of neighbors to Peer " + this.peer.getId() + " (" + this.peer.getIP() + ":" + this.peer.getPort() + " / Capacity: "+this.peer.getCapacity()+"):\n";
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

	public Peer getPeer(){
		return this.peer;
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
	public void destroy() {
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
		if (this.openNeighborRequests.containsKey(peer.getIP() + ':' + peer.getPort()) &&
				this.openNeighborRequests.get(peer.getIP() + ':' + peer.getPort()) == NeighborNegotiationState.REQUEST_SENT)
		{
			this.openNeighborRequests.remove(peer.getIP() + ":" + peer.getPort());
		}
		if (success) {
			if (this.peer.getId() == 1) {
				System.out.println("waaaa");
			}
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
	@SuppressWarnings("rawtypes")
	public synchronized void addPeer(Vector data) {
		Peer peer = CommunicationConverter.createPeer(data);
		this.addPeer(peer);
		// TODO: Check if peer is already in the system.
	}


	@SuppressWarnings("rawtypes")
	public synchronized void receiveConnectionAnswer(Vector data) {
		logger.debug("Received Connection answer with length {}", data.size());
		if (data.size() == 5) {
			boolean neighborResponse = (Boolean) data.get(4);
			data.setSize(4);
			Peer peer = CommunicationConverter.createPeer(data);
			this.addPeer(peer);
			this.addNeighbor(peer, neighborResponse);
		} else {
			this.addPeer(CommunicationConverter.createPeer(data));
		}
	}

	/**
	 * Add a set of peers to the Peerlist.
	 *
	 * @param data Map of String - Vector pairs, where the Vector is a representation of a Peer.
	 */
	@SuppressWarnings("rawtypes")
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
		Thread connection = new Thread(new ConnectionAsyncTask(ip, port, this));
		connection.start();
	}

	public void startNegotiate() {
		Thread negotiation = new Thread(new NegotiationTask(this));
		negotiation.start();
	}

	/**
	 * Search file in the network using flooding(only used by the peer that starts the search).
	 *
	 * @param fileName   the name of the file the peer is searching
	 */
	public void searchFile(String fileName, int ttl) {
		searchCount++;

		String searchIdentifier = this.getPeer().getId() + "." + this.searchCount;
		this.searchList.add(searchIdentifier);
		Thread search = new Thread(new SearchTask(this, fileName, ttl, searchIdentifier));
		search.start();
	}
	
	
	/**
	 * Get a file from another peer. 
	 * 
	 * @param nameFile File name of the File to be downloaded
	 * @param ip IP number of the peer that we will download the file. 
	 * @param port Port number of the peer 
	 */
	public void getP2pFile(String nameFile, String ip, int port) {
		Thread get = new Thread(new Receive(this,nameFile,ip,port));
		get.start();
	}
	

	/**
	 * Pass the search process to all peers in the peer list and add the identifier to the searchList.
	 * If it has already seen the request, then just ignore it.
	 */
	public void passSearch(Vector<Object> origin, String fileName, int ttl, String ident) {
		Thread pass = new Thread(new PassSearchTask(this, origin, fileName, ttl - 1, ident));
		pass.start();
	}

	public void nlistGraph(int[] peers, String dir, boolean all) throws IOException {

		PrintWriter output;
		if (dir == null) {
			output = new PrintWriter(System.out);
		} else {
			String fileName = dir.substring(0, dir.length());
			output = new PrintWriter(fileName);
		}

		output.println("graph network {");

		if (!all && peers == null) {
			output.println("      \"P" + this.peer.getId() + '(' + this.peer.getCapacity() + ")\";");
			Set<Map.Entry<Integer, Peer>> neighborSet = this.getNeighborList().entrySet();
			for (Map.Entry<Integer, Peer> entry : neighborSet) {
				Peer itPeer = entry.getValue();
				output.println("      \"P" + itPeer.getId() + '(' + itPeer.getCapacity() + ")\";");
			}
			for (Map.Entry<Integer, Peer> entry : neighborSet) {
				Peer itPeer = entry.getValue();
				output.println("      \"P" + this.peer.getId() + '(' + this.peer.getCapacity() + ")\" -- " + "\"P" + itPeer.getId() + '(' + itPeer.getCapacity() + ")\";");
			}
		} else {
			//peers from the arguments
			Vector<Peer> peerIndicated = new Vector<Peer>();	
			//peers from the arguments and there neighbors
			HashMap<Integer, Peer> peerInvolved = new HashMap<Integer, Peer>();		

			MapNeighborhoodTask nbTask = new MapNeighborhoodTask(this.peer, this);
			HashMap<Peer, ArrayList<Peer>> topo = nbTask.getTopology();
			Set<Peer> peerTopo = topo.keySet();

			System.out.print("The result is  ");
			System.out.println(topo);

			if (all) {
				if (peers != null) {
					logger.error("ERROR! When ALL is specified peers should be empty!");
					return;
				}
				for (Peer itPeer : peerTopo) {
					peerIndicated.add(itPeer);
					peerInvolved.put(itPeer.getId(), itPeer);
				}
			} else {
				Arrays.sort(peers);
				for (int i : peers) {
					for (Peer itPeer : peerTopo) {
						if (itPeer.getId() == i) {
							peerIndicated.add(itPeer);
							peerInvolved.put(itPeer.getId(), itPeer);
							for (Peer peerEntry: topo.get(itPeer))
								peerInvolved.put(peerEntry.getId(), peerEntry);
						}
					}
				}
			}


			System.out.print("peerIndicated is  ");
			System.out.println(peerIndicated);

			Set<Map.Entry<Integer, Peer>> setInv = peerInvolved.entrySet();

			for (Map.Entry<Integer, Peer> aPv : setInv) {
				output.println("      \"P" + aPv.getValue().getId() + '(' + aPv.getValue().getCapacity() + ")\";");
			}

			HashMap<Integer, Peer> peerIndict = new HashMap<Integer, Peer>();

			for (Peer itPeer: peerIndicated) {
				peerIndict.put(itPeer.getId(), itPeer);
			}

			for (int i = 0; i < peerIndicated.size(); i++) {
				ArrayList<Peer> nl = topo.get(peerIndicated.get(i));
				for (int j = 0; j < nl.size(); j++) {
					if (peerIndict.containsKey(nl.get(j).getId()) && !peerIndicated.get(i).smallerThan(nl.get(j)))
						continue;
					boolean contains = false;
					for (Map.Entry<Integer, Peer> aPv : setInv) {
						if (nl.get(j).equals(aPv.getValue())) {
							contains = true;
						}
					}
					if (contains) {
						output.println("      \"P" + peerIndicated.get(i).getId() + '(' + peerIndicated.get(i).getCapacity() + ")\" -- " + "\"P" + nl.get(j).getId() + '(' + nl.get(j).getCapacity() + ")\";");
					}
				}

			}
		}
		output.println("}");
		output.close();
	}


}
