package vic;

import java.net.Socket;
import java.net.InetAddress;
import java.net.ServerSocket;

import java.util.*;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.xmlrpc.*;

public class PeerApp {

	Peer peer;
	//ArrayList<Peer> peerList; 		Use HashMap instead
	HashMap<Integer, Peer> peerList;

	PrintWriter output;
	Scanner input;
	int MAXDEPTH;

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
		this.MAXDEPTH = max;

		Thread listening = new Thread(new listeningTask());   //start listening to the port
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
			Thread connection = new Thread(new connectionTask(ipAux, portAux));
			connection.start();
		}		
	}

	public void hello(String ip, int port) {		// send message to the peer indicated by the ip and port
		Thread connection = new Thread(new connectionTask(ip, port));
		connection.start();
		/**
		 * A problem here is that we only know the ip and the port of the peer, 
		 * but we need the whole peer to be added into the peerList, this needs to be
		 * solved, maybe by adding another argument?
		 */
	}

	class connectionTask implements Runnable {		//this is used when the local peer wants to establish 
		String ip;					// a connection to another peer
		int port;
		public connectionTask(String ip, int port) {
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
				params.addElement(peer);
				params.addElement(new Integer(MAXDEPTH));

				// Issue a request
				@SuppressWarnings("unchecked")
				HashMap<Integer, Peer> result = (HashMap<Integer, Peer>)client.execute("discovery.hello", params);

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
			} catch (XmlRpcException e) {
				System.out.println("Exception within XML-RPC: " + e.getMessage());
			}
		}
	}

	public class helloHandler {
		public HashMap<Integer, Peer> hello(Peer inPeer, Integer depthInt) {
			int depth = depthInt.intValue();
			peerList.put(new Integer(inPeer.getId()), inPeer);
			HashMap<Integer, Peer> res = new HashMap<Integer, Peer>();
			res.put(new Integer(peer.getId()), peer);

			if(depth <= 0) {
				res = peerList;
				return res;
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
						params.addElement(peer);
						params.addElement(new Integer(depth - 1));

						// Issue a request
						@SuppressWarnings("unchecked")
						HashMap<Integer, Peer> result = (HashMap<Integer, Peer>)client.execute("discovery.hello", params);

						/**
						 * then add the peers in this vector to the peer list of the current peer;
						 */
						Iterator<Map.Entry<Integer, Peer>> iterator = result.entrySet().iterator();
						while(iterator.hasNext()) {
							Map.Entry<Integer, Peer> entry = iterator.next();
							res.put(entry.getKey(), entry.getValue());
							peerList.put(entry.getKey(), entry.getValue()); // also update the current peer list
						}

					} catch (IOException e) {
						System.out.println("IO Exception: " + e.getMessage());
					} catch (XmlRpcException e) {
						System.out.println("Exception within XML-RPC: " + e.getMessage());
					}
				}
			}

			return res;
		}
	}

	class listeningTask implements Runnable {		//when the peer is created, it will use this thread to listen
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
				server.addHandler("discovery", new helloHandler());
				System.out.println("Now accepting requests. (Halt program to stop.)");

			} catch (Exception e) {
				System.out.println("Could not start server: " + e.getMessage(  ));
			}
		}
	}

	/*
	class connectionTask implements Runnable {		//this is used when the local peer wants to establish 
		String ip;					// a connection to another peer
		int port;
		public connectionTask(String ip, int port) {
			this.ip = ip;
			this.port = port;
		}
		@Override
		public void run() {
			try {
				Socket socket = new Socket(ip, port);
				input = new Scanner(socket.getInputStream());
				output = new PrintWriter(socket.getOutputStream());

				System.out.println("Connection established to " + ip + ':' + port);

				output.println("Hello this is peer P" + peer.id);
				output.flush();

			} catch (IOException ex) {
				System.err.println(ex);
			}
		}
	}

	class listeningTask implements Runnable {		//when the peer is created, it will use this thread to listen
		int port;					// to its port
		public listeningTask(int port) {
			this.port = port;
		}
		@Override
		public void run() { 
			try {
				ServerSocket serverSocket = new ServerSocket(port);
				while(true){
					Socket socket = serverSocket.accept();
					Thread thread = new Thread(new answerTask(socket));
					thread.start();
				}
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}
	}

	protected class answerTask implements Runnable {
		private Socket socket;

		answerTask(Socket socket){
			this.socket = socket;
		}

		@Override
		public void run(){
			try{
				input = new Scanner(socket.getInputStream());
				output = new PrintWriter(socket.getOutputStream());

				while(true){
					lock.lock();

					String greeting = input.nextLine();
					System.out.println(greeting);

					lock.unlock();
				}
			} catch(Exception ex) {
				System.err.println(ex);
				try {
					socket.close();
				} catch(Exception e) {
					System.err.println(e);
				}
			}
		}
	}
	*/
}
