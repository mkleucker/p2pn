package vic;

import java.net.Socket;
import java.net.InetAddress;
import java.net.ServerSocket;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.io.IOException;
import java.io.PrintWriter;


public class PeerApp {

	Peer peer;
	//ArrayList<Peer> peerList; 		Use HashMap instead
	HashMap<Integer, Peer> peerList;

	PrintWriter output;
	Scanner input;

	/**
	 *used to ensure peers finished saying hello before some 
	 *other peers come and start to say hello
	 */
	Lock lock = new ReentrantLock();	

	/**
	 * Constructor of the class PeerApp
	 */
	public PeerApp(int id, String address, int port, int capacity) {
		peer = new Peer(id, address, port, capacity);//creation of the Peer
		peerList = new HashMap<Integer, Peer>();	// initialize the peerList

		Thread listening = new Thread(new listeningTask(port));   //start listening to the port
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
			System.out.print("Name: P" + peer.id + "  ");
			System.out.print("IP: " + peer.address + "  ");
			System.out.print("Port: " + peer.port + "  ");
			System.out.println("Capacity: " + peer.capacity);
		}
	}

	public int getId() {
		return peer.getId();
	}

	public void setId(int id) {
		peer.setId(id);
	}

	public String getAddress() {
		return peer.getAddress();
	}

	public void setAddress(String address) {
		peer.setAddress(address);
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
				Socket socket = new Socket(ip, port);
				input = new Scanner(socket.getInputStream());
				output = new PrintWriter(socket.getOutputStream());

				System.out.println("Connection established to " + ip + ':' + port);

				/**
				 *  do something here
				 */

				output.println("Hello this is peer P" + this.peer.id);
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

					/**
					 * new a thread to deal with this request, and send some messages.
					 */
					Thread thread = new Thread(new answerTask(socket));
					thread.start();
				}
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}
	}

	/**
	 * This thread is created when there is a new connection to the local peer,
	 * and this thread will answer the request of the peer.
	 */
	protected class answerTask implements Runnable {
		private Socket socket;

		SocketThread(Socket socket){
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
}
