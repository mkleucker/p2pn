package vic;

import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.*;

public class PeerApp {

	Peer peer;
	Map<Integer, Peer> peerList;

    private static final Logger logger = LogManager.getLogger(PeerApp.class.getName());
	int maxdepth;

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
		this.maxdepth = max;

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
		String ipAux;
		int portAux;
		 //Iteration of all the peers in the list of peers

        for (Map.Entry<Integer, Peer> entry: new HashMap<Integer,Peer>(this.peerList).entrySet()) {
			Peer peerAux = entry.getValue();
			ipAux = peerAux.getIP();
            if (peerAux.getId() == this.peer.getId()) {
                continue;
            }
			portAux = peerAux.getPort();
			// creation of a connection for every peer in the list of peers
			Thread connection = new Thread(new ConnectionTask(ipAux, portAux, this.peer));
			connection.start();
		}		
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

    public synchronized Map<Integer, Peer> getPeerList(){
        return this.peerList;
    }

    /**
     * Connects to the specified adress.
     * @param ip IP-Address of the targeted peer.
     * @param port Port of the targeted peer.
     */
	public void ping(String ip, int port) {
		Thread connection = new Thread(new ConnectionTask(ip, port, this.peer));
		connection.start();
        // TODO: Add to peerlist
		/**
		 * A problem here is that we only know the ip and the port of the peer,
		 * but we need the whole peer to be added into the peerList, this needs to be
		 * solved, maybe by adding another argument?
		 */
	}

    /**
     * Subclass to establish a connection to a different peer.
     */
	class ConnectionTask implements Runnable {

        String ip;
		int port;
        Peer peer;

        XmlRpcClient client;

		public ConnectionTask(String ip, int port, Peer peer) {
			this.ip = ip;
			this.port = port;

            this.peer = peer;
		}

		@Override
		public void run() {
			try {

				// Create the client, identifying the server
				this.client = new XmlRpcClient("http://" + ip + ':' + port + '/');
                logger.debug("{} Connection establish to {}:{}", this.peer.getId(), this.ip, this.port);

				// Issue a request
                Vector result = (Vector)client.execute("communication.pong",createVectorForPeer(this.peer, maxdepth));
                if(result == null){
                    logger.debug("No result from Discovery");
                    return;
                }
				/**
				 * then add the peers in this vector to the peer list of the current peer;
				 */
                for (Map.Entry<String, Vector> entry: ((Hashtable<String, Vector>) result).entrySet()) {
					peerList.put(Integer.parseInt(entry.getKey()), createPeerFromVector(entry.getValue()));
				}

			} catch (IOException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            } catch (XmlRpcException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
		}

	}


    /**
     * Creates request parameters for the current peer.
     * @return Vector with all parameters
     */
    private static Vector<Object> createVectorForPeer(Peer peer, int depth){
        Vector<Object> params = new Vector<Object>();
        params.addElement(peer.getId());
        params.addElement(peer.getIP());
        params.addElement(peer.getPort());
        params.addElement(peer.getCapacity());
        params.addElement(depth);

        return params;
    }

    private static Hashtable<String,Vector> createExchangeData(Map<Integer,Peer> rawData){
        Hashtable<String, Vector> result = new Hashtable<String, Vector>();

        for (Map.Entry<Integer, Peer> entry: rawData.entrySet()) {
            // TODO: fix depth parameter
            result.put(Integer.toString(entry.getKey()), createVectorForPeer(entry.getValue(), 0));
        }

        return result;
    }

    private static Peer createPeerFromVector(Vector data){
        return new Peer((Integer)data.get(0),
                (String)data.get(1),
                (Integer) data.get(2),
                (Integer) data.get(3));
    }

    /**
     * Handler is able to do the handshake with another peer.
     */
	public class CommunicationHandler {

        private Peer peer;
        private PeerApp app;

        public CommunicationHandler(Peer peer, PeerApp app){
            this.peer = peer;
            this.app = app;
        }
        
		public Vector pong(int IdArg, String IPArg, int portArg, int capacityArg, int depthInt) {
            
            // Create Peer object
            this.app.addPeer(new Peer(IdArg, IPArg, portArg, capacityArg));

            return createVectorForPeer(this.peer, depthInt-1);

            // Only return local object
            /*if(depthInt <= 0) {
                return createExchangeData(peerList);
            }

			HashMap<Integer, Peer> res = new HashMap<Integer, Peer>();
			res.put(peer.getId(), peer); //TODO: Necessary?

			for (Map.Entry<Integer, Peer> temp: new HashMap<Integer,Peer>(peerList).entrySet()) {
				Peer itPeer = temp.getValue();

                if(itPeer.getId() == peer.getId())
                    continue;

				if(!itPeer.equals(inPeer)) {
					try {
						// Create the client, identifying the server
						XmlRpcClient client = new XmlRpcClient("http://" + itPeer.getIP() + ':' + itPeer.getPort() + '/');
						logger.debug("{} CommunicationHandler: Connection established to {}:{}",this.peer.getId(), itPeer.getIP(), itPeer.getPort());

						// Issue a request
						Object incomingData = (Object) client.execute("communication.ping",
                                createVectorForPeer(peer, depthInt-1));

                        Hashtable<String,Vector> result;
                        // Check answer
                        if(incomingData instanceof Hashtable)
                        {
                            result = (Hashtable<String,Vector>) incomingData;
                        }
                        else
                        {
                            logger.debug("No valid return value from {}:{}", itPeer.getIP(), itPeer.getPort());
                            return null;
                        }



                        for (Map.Entry<String, Vector> entry: result.entrySet()) {
                            Integer id = Integer.parseInt(entry.getKey());
                            Peer peer = createPeerFromVector(entry.getValue());
                            res.put(id, peer);
                            peerList.put(id, peer);
                        }


					} catch (IOException e) {
						logger.error(e.getMessage());
                        e.printStackTrace();
					} catch (XmlRpcException e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }
				}
			}

			return createExchangeData(res);*/
		}

        public Hashtable<String, Vector> getPeerList(){
            return createExchangeData(this.app.getPeerList());
        }

	}

    /**
     * Creates the XML-RPC listening part.
     */
	class ListeningTask implements Runnable {
        WebServer server;
        PeerApp app;
        Peer peer;
        
        public ListeningTask(Peer peer, PeerApp app){
            this.peer = peer;
            this.app = app;    
        }
        
		@Override
		public void run() { 
			try {
				// Start the server, using built-in version
				this.server = new WebServer(getPort());

                // we _do_ want other clients to connect to us
				server.setParanoid(false);
				server.acceptClient(getIP());

				// Register our handler class as discovery
				server.addHandler("communication", new CommunicationHandler(this.peer, this.app));
                server.start();
                logger.debug("Created ListeningTask successfully");
			} catch (Exception e) {
				logger.error("Could not start server: " + e.getMessage());
                logger.error(e.getStackTrace());
            }
		}

        public boolean shutdown(){
            this.server.shutdown();
            return true;
        }
	}

}
