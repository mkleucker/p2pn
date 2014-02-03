package vic;

import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PeerApp {

	Peer peer;
	HashMap<Integer, Peer> peerList;

	int maxdepth;

	/**
	 *used to ensure peers finished saying hello before some 
	 *other peers come and start to say hello
	 */
	Lock lock = new ReentrantLock();	

	/**
	 * Constructor of the class PeerApp
	 */
	public PeerApp(int id, String ip, int port, int capacity, int max) {
		peer = new Peer(id, ip, port, capacity);//creation of the Peer
		peerList = new HashMap<Integer, Peer>();	// initialize the peerList
		this.maxdepth = max;

		Thread listening = new Thread(new ListeningTask());   //start listening to the port
		listening.start();
	}

	public Set<Map.Entry<Integer, Peer>> getPeerSet() {		// return the set of peers in the peerList
		Set<Map.Entry<Integer, Peer>> peerSet = peerList.entrySet();
		return peerSet;
	}

	public void plist() {					//print the peerList on the console
		System.out.println("List of peers known to the local peer: ");
		Set<Map.Entry<Integer, Peer>> peerSet = getPeerSet();
		for (Map.Entry<Integer, Peer> entry: peerSet) {
			Peer peer = entry.getValue();
			System.out.print("Name: P" + getId() + "  ");
			System.out.print("IP: " + getIP() + "  ");
			System.out.print("Port: " + getPort() + "  ");
			System.out.println("Capacity: " + getCapacity());
		}
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

        for (Map.Entry<Integer, Peer> entry: peerList.entrySet()) {
			Peer peerAux = entry.getValue();
			ipAux = peerAux.getIP();
			portAux = peerAux.getPort();
			// creation of a connection for every peer in the list of peers
			Thread connection = new Thread(new ConnectionTask(ipAux, portAux));
			connection.start();
		}		
	}

    /**
     * Connects to the specified adress.
     * @param ip IP-Address of the targeted peer.
     * @param port Port of the targeted peer.
     */
	public void hello(String ip, int port) {
		Thread connection = new Thread(new ConnectionTask(ip, port));
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

		public ConnectionTask(String ip, int port) {
			this.ip = ip;
			this.port = port;
		}

		@Override
		public void run() {
			try {

				// Create the client, identifying the server
				XmlRpcClient client = new XmlRpcClient("http://" + ip + ':' + port + '/');
				System.out.println("Connection established to " + ip + ':' + port);

				// Issue a request
                Hashtable result = (Hashtable)client.execute("discovery.hello", createVectorForPeer(peer, maxdepth));
                System.out.println(result);
                if(result == null){
                    return;
                }
                System.out.println(result);

				/**
				 * then add the peers in this vector to the peer list of the current peer;
				 */
                for (Map.Entry<String, Vector> entry: ((Hashtable<String, Vector>) result).entrySet()) {
                	System.out.println("The list before is " + peerList);
                	System.out.println("The vector is " + entry.getValue());
                	Peer temp = createPeerFromVector(entry.getValue());
                	System.out.println("The peer created is " + temp);
                	System.out.println("The id is " + entry.getKey());
                	Integer idCreated = Integer.parseInt(entry.getKey());
                	System.out.println("The id integer is " + idCreated);
					peerList.put(idCreated, temp);
					System.out.println("The list after is " + peerList);
				}

			} catch (IOException e) {
				System.out.println("IO Exception: " + e.getMessage());
                e.printStackTrace();
            } catch (XmlRpcException e) {
				System.out.println("Exception within XML-RPC: " + e.getMessage());
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

    private static Hashtable<String,Vector> createExchangeData(HashMap<Integer,Peer> rawData){
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
	public class HelloHandler {
        
		public Hashtable<String, Vector> hello(int IdArg, String IPArg, int portArg, int capacityArg, int depthInt) {

            // Create Peer object
            Peer inPeer = new Peer(IdArg, IPArg, portArg, capacityArg);
			peerList.put(inPeer.getId(), inPeer); //TODO: Necessary?

            // Only return local object
            if(depthInt <= 0) {
                System.out.println("final");
                return createExchangeData(peerList);
            }

			HashMap<Integer, Peer> res = new HashMap<Integer, Peer>();
			res.put(peer.getId(), peer); //TODO: Necessary?

			for (Map.Entry<Integer, Peer> temp: peerList.entrySet()) {
				Peer itPeer = temp.getValue();

                System.out.println(itPeer.getId()+ "=="+peer.getId()+"=="+inPeer.getId());
				if(!itPeer.equals(inPeer)) {
					try {
						// Create the client, identifying the server
						XmlRpcClient client = new XmlRpcClient("http://" + itPeer.getIP() + ':' + itPeer.getPort() + '/');
						System.out.println("Connection established to " + itPeer.getIP() + ':' + itPeer.getPort());

						// Issue a request
						Object incomingData = (Object) client.execute("discovery.hello",
                                createVectorForPeer(peer, depthInt-1));

                        Hashtable<String,Vector> result;
                        // Check answer
                        if(incomingData instanceof Hashtable)
                        {
                            result = (Hashtable<String,Vector>) incomingData;
                        }
                        else
                        {
                            System.out.println("OTHER");
                            return null;
                        }

						/**
						 * then add the peers in this vector to the peer list of the current peer;
						 */

                        for (Map.Entry<String, Vector> entry: result.entrySet()) {
                            Integer id = Integer.parseInt(entry.getKey());
                            Peer peer = createPeerFromVector(entry.getValue());
                            res.put(id, peer);
                            peerList.put(id, peer);
                        }


					} catch (IOException e) {
						System.out.println("IO Exception: " + e.getMessage());
                        e.printStackTrace();
					} catch (XmlRpcException e) {
						System.out.println("Exception within XML-RPC: " + e.getMessage());
                        e.printStackTrace();

                    }
				}
			}
            System.out.println("called hello");
            System.out.println(peerList);
            System.out.println(res);
			return createExchangeData(res);
		}


	}

    /**
     * Creates the XML-RPC listening part.
     */
	class ListeningTask implements Runnable {
		@Override
		public void run() { 
			try {
				// Start the server, using built-in version
				System.out.println("Attempting to start XML-RPC Server...");
				WebServer server = new WebServer(getPort());
				System.out.println("Started successfully.");

                // we _do_ want other clients to connect to us
				server.setParanoid(false);
				server.acceptClient(getIP());

				// Register our handler class as discovery
				System.out.println("Registering helloHandler class to discovery...");
				server.addHandler("discovery", new HelloHandler());
				System.out.println("Now accepting requests. (Halt program to stop.)");
                server.start();
			} catch (Exception e) {
				System.out.println("Could not start server: " + e.getMessage());
                e.printStackTrace();
            }
		}
	}

}
