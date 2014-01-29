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
	HashMap<int, Peer> peerList;

	PrintWriter output;
	Scanner input;


	/*
	 * Constructor of the class PeerApp
	 */
	public PeerApp(int id, String address, int port, int capacity) {
		peer = new Peer(id, address, port, capacity);//creation of the Peer
		peerList = new HashMap<int, Peer>();
	}

	public Set<Map.Entry<int, Peer>> getPeerSet() {
		Set<Map.Entry<int, Peer>> peerSet = peerList.entrySet();
		return peerSet;
	}

	public void plist() {
		System.out.println("List of peers known to the local peer: ");
		Set<Map.Entry<int, Peer>> peerSet = getPeerSet();
		for (Map.Entry<int, Peer> entry: peerSet) {
			Peer peer = entry.getValue();
			System.out.print("Name: P" + peer.id + "  ");
			System.out.print("IP: " + peer.ip + "  ");
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

	public String getAdress() {
		return peer.getAdress();
	}

	public void setAdress(String address) {
		peer.setAdress(address);
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

	public void hello(String ip, int port) {
		Thread connection = new Thread(new connectionTask(ip, port));
		connection.start();
	}

	class connectionTask implements Runnable {
		String ip;
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

				output.flush();
			} catch (IOException ex) {
				System.err.println(ex);
			}
		}
	}

	class listeningTask implements Runnable {
		int port;
		public listeningTask(int port) {
			this.port = port;
		}
		@Override
		public void run() { 
			try {
				ServerSocket serverSocket = new ServerSocket(port);
				while(true){
					Socket socket = serverSocket.accept();

				}
			} catch (IOException ex) {
				System.err.println(ex);
			}
		}
	}

}
