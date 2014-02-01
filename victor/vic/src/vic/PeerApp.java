package vic;

import org.apache.xmlrpc.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PeerApp {

	Peer peer;
	//ArrayList<Peer> peerList; 		Use HashMap instead
	HashMap<Integer, Peer> peerList;

	PrintWriter output;
	Scanner input;
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
		Iterator<Map.Entry<Integer, Peer>> iterator = peerList.entrySet().iterator();
		while(iterator.hasNext()) {			
			Map.Entry<Integer, Peer> entry = iterator.next();
			Peer peerAux = entry.getValue();
			ipAux = peerAux.getIP();
			portAux = peerAux.getPort();
			// creation of a connection for every peer in the list of peers
			Thread connection = new Thread(new ConnectionTask(ipAux, portAux));
			connection.start();
		}		
	}

	public void hello(String ip, int port) {		// send message to the peer indicated by the ip and port
		Thread connection = new Thread(new ConnectionTask(ip, port));
		connection.start();
		/**
		 * A problem here is that we only know the ip and the port of the peer, 
		 * but we need the whole peer to be added into the peerList, this needs to be
		 * solved, maybe by adding another argument?
		 */
	}

	class ConnectionTask implements Runnable {		//this is used when the local peer wants to establish
		String ip;					// a connection to another peer
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

				// Create the request parameters using user input
				Vector<Object> params = new Vector<Object>();
				params.addElement(new Integer(peer.getId()));
				params.addElement(peer.getIP());
				params.addElement(new Integer(peer.getPort()));
				params.addElement(new Integer(peer.getCapacity()));
				params.addElement(new Integer(maxdepth));

				// Issue a request
				@SuppressWarnings("unchecked")
                Hashtable result = (Hashtable)client.execute("discovery.hello", params);
                System.out.println(result);
				/**
				 * then add the peers in this vector to the peer list of the current peer;
				 */
				Iterator<Map.Entry<Integer, Peer>> iterator = result.entrySet().iterator();
				while(iterator.hasNext()){
					Map.Entry<Integer, Peer> entry = iterator.next();
					peerList.put(entry.getKey(), entry.getValue());
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

	public class HelloHandler {
        
		public Hashtable<String, Peer> hello(int IdArg, String IPArg, int portArg, int capacityArg, int depthInt) {
			Peer inPeer = new Peer(IdArg, IPArg, portArg, capacityArg);
			int depth = depthInt;
			peerList.put(new Integer(inPeer.getId()), inPeer);
			HashMap<String, Peer> res = new HashMap<String, Peer>();
			res.put(new Integer(peer.getId()).toString(), peer);

			if(depth <= 0) {
//				res = peerList;
//				return new Hashtable<String, Peer>(res);
                return null;
			}

			Set<Map.Entry<Integer, Peer>> set = peerList.entrySet();
			for (Map.Entry<Integer, Peer> temp: set) {
				Peer itPeer = temp.getValue();
				if(!peer.equals(itPeer)) {
					try {
						// Create the client, identifying the server
						XmlRpcClient client = new XmlRpcClient("http://" + itPeer.getIP() + ':' + itPeer.getPort() + '/');
						System.out.println("Connection established to " + itPeer.getIP() + ':' + itPeer.getPort());

						// Create the request parameters using user input
						Vector<Object> params = new Vector<Object>();
						params.addElement(peer.getId());
						params.addElement(peer.getIP());
						params.addElement(peer.getPort());
						params.addElement(peer.getCapacity());
						params.addElement(depth - 1);

						// Issue a request
						@SuppressWarnings("unchecked")
						Object result = (Object) client.execute("discovery.hello", params);
                        if(result instanceof Hashtable)
                        {
                            System.out.println("HASHTABLE!");
                        }
                        else
                        {
                            System.out.println("OTHER");
                        }
                        System.out.println(result.toString());
                        Hashtable<String,Peer> result2 = (Hashtable<String,Peer>) result;
						/**
						 * then add the peers in this vector to the peer list of the current peer;
						 */
						Iterator<Map.Entry<String, Peer>> iterator = result2.entrySet().iterator();
						while(iterator.hasNext()) {
							Map.Entry<String, Peer> entry = iterator.next();
							res.put(entry.getKey().toString(), entry.getValue());
							peerList.put(Integer.parseInt(entry.getKey()), entry.getValue()); // also update the current peer list
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
            Hashtable<String,Peer> res2 = new Hashtable<String,Peer>(res);
            System.out.println(res2.toString());

			return res2;
		}
	}

	class ListeningTask implements Runnable {		//when the peer is created, it will use this thread to listen
		@Override
		public void run() { 
			try {
				// Start the server, using built-in version
				System.out.println("Attempting to start XML-RPC Server...");
				WebServer server = new WebServer(getPort());
				System.out.println("Started successfully.");

				server.setParanoid(true);
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
