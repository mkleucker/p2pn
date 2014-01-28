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
	ArrayList<Peer> peerList;

	PrintWriter output;
	Scanner input;


	/*
	 * Constructor of the class PeerApp
	 */
	PeerApp(int idC, String addressC, int portC, int capacityC){
		peer = new Peer(idC,addressC,portC,capacityC);//creation of the Peer
		peerList = new ArrayList<Peer>();
	}

	public ArrayList<Peer> getPeer(){
		return peerList;
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
