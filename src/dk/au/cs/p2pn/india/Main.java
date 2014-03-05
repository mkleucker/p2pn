package dk.au.cs.p2pn.india;

import dk.au.cs.p2pn.india.reporting.Reporter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

/**
 * Main class to later run the peer that also is responsible for
 * taking input from the commandline and issuing the correct commands
 * to the peer.
 */
public class Main {

	private PeerApp peer;

	private BufferedReader reader;

	private static final Logger logger = LogManager.getLogger(PeerApp.class.getSimpleName());

	public Main(String[] args) {
		// Initialize Reporter for this program instance.
		Reporter.init();

		try {
			logger.info("Starting program");

			this.reader = new BufferedReader(new InputStreamReader(System.in));
			if (args.length != 3) {
				this.peer = new PeerApp(0, "127.0.0.1", 18523, 9);
			} else {
				int id = Integer.parseInt(args[0]);
				String ip = args[1];
				Integer port = Integer.parseInt(args[2]);
				this.peer = new PeerApp(id, ip, port, 9);
			}

			checkConnection();
			this.parseInput();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Starts a new thread for check the connections
	 */
	public void checkConnection() {
		Thread checkConn = new Thread(new Checking(peer));
		checkConn.start();
	}

	@SuppressWarnings("unused")
	private void testGet() throws InterruptedException {
		PeerApp peer1 = new PeerApp(0, "127.0.0.1", 18525, 9);
		Thread.sleep(1000);
		this.peer.getP2pFile("duck.jpg", "127.0.0.1", 18525);
	}

	private ArrayList<PeerApp> setupNetwork() {
		Random rand = new Random();

		ArrayList<PeerApp> peers = new ArrayList<PeerApp>();
		try {

			int numOfPeers = 100;
			int port = this.peer.getPeer().getPort();
			for (int i = 1; i < numOfPeers; i++) {
				peers.add(new PeerApp(i, "127.0.0.1", port + i, rand.nextInt(9) + 1));
			}
			Thread.sleep(2000);

			for (PeerApp peer : peers) {
				int numberOfConnections = rand.nextInt(numOfPeers);
				for (int i = 0; i < numberOfConnections; i++) {
					int p = rand.nextInt(peers.size());
					if (p != peer.getPeer().getId())
						peer.ping("127.0.0.1", peers.get(p).getPeer().getPort());
				}
			}

			Thread.sleep(3000);

			for (PeerApp peer : peers) {
				logger.info("{}", peer.plist());
			}

			Thread.sleep(3000);

			for (PeerApp peer : peers) {
				peer.startNegotiate();
			}

			Thread.sleep(5000);


		} catch (Exception e) {

		}
		return peers;

	}
	
	/**
	 * 
	 * Test method for the download part.
	 * 
	 * Must be used while it's being used the "testMonday() function" in another
	 * computer.  With this method you can establish a connection between
	 * 4 peers and offer a file to be downloaded from one peer to another
	 * peer (try.txt file).
	 *   
	 */
	private void testProcessHostFile(){
		
		//use your own IP
		PeerApp peer1 = new PeerApp(1, "10.192.75.179", 18525, 9);
		PeerApp peer2 = new PeerApp(2, "10.192.75.179", 18526, 9);
				
		try {
			Thread.sleep(2000);
			
			peer1.uploadFile("try.txt");
						
			Thread.sleep(1000);
			
			//ping with from peer 1 to peer 2
			peer1.ping("10.192.75.179", 18526);

			Thread.sleep(3000);
			
			peer1.startNegotiate();
			Thread.sleep(2000);
			
			//ping with from peer 2 to peer 3
			System.out.println("0.peer list: " + peer1.plist());

			//pc 2 IP
			peer2.ping("10.192.4.119", 18527);
			Thread.sleep(2000);
			
			peer2.startNegotiate();
			System.out.println("peer list: " + peer1.plist());
			System.out.println("peer list: " + peer2.plist());
			
			System.out.println("neighbour list: " + peer1.nlist());
			System.out.println("neighbour list: " + peer2.nlist());
						
		
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
}
	

	/**
	 * 
	 * Test method for the download part.
	 * 
	 * Must be used while it's being used the "testProcessHostFile() function" in another
	 * computer.  With this method you can establish a connection between
	 * 4 peers and download the file offered by the peer to be donwloaded (try.txt). 
	 *   
	 */
	private void testMonday(){

		try {
			
		PeerApp peer1 = new PeerApp(1, "10.192.4.119", 18527, 5);
		PeerApp peer2 = new PeerApp(2, "10.192.4.119", 18528, 5);
		
		Thread.sleep(2000);
		
		peer1.uploadFile("duck.mp4");
		peer2.ping(peer1.getPeer().getIP(), peer1.getPeer().getPort());
		
		System.out.println("Peer 1's IP is " + peer1.getPeer().getIP());
		System.out.println("Peer 1's port is " + peer1.getPeer().getPort());
		
		Thread.sleep(3000);
		
		System.out.println("Peer 2's peer list is " + peer2.plist());
		System.out.println("Peer 1's peer list is " + peer1.plist());
		
		peer1.startNegotiate();

		Thread.sleep(2000);
		
		System.out.println("Peer 2's neighbor list is " + peer2.getNeighborList());
		System.out.println("Peer 1's neighbor list is " + peer1.getNeighborList());
		
		Thread.sleep(3000);
		
		System.out.println("Peer 2's neighbor list is " + peer2.getNeighborList());
		
		Thread.sleep(2000);
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void testWednesday(){

		try {
			
		PeerApp peer1 = new PeerApp(1, "10.192.4.119", 18527, 5);
		PeerApp peer2 = new PeerApp(2, "10.192.4.119", 18528, 5);
		PeerApp peer3 = new PeerApp(3, "10.192.4.119", 18529, 5);
		
		Thread.sleep(2000);
		
		peer1.uploadFile("duck.mp4");
		peer2.ping(peer1.getPeer().getIP(), peer1.getPeer().getPort());
		
		System.out.println("Peer 1's IP is " + peer1.getPeer().getIP());
		System.out.println("Peer 1's port is " + peer1.getPeer().getPort());
		
		Thread.sleep(3000);
		
		System.out.println("Peer 2's peer list is " + peer2.plist());
		System.out.println("Peer 1's peer list is " + peer1.plist());
		
		peer1.startNegotiate();

		Thread.sleep(2000);
		
		System.out.println("Peer 2's neighbor list is " + peer2.getNeighborList());
		System.out.println("Peer 1's neighbor list is " + peer1.getNeighborList());
		
		Thread.sleep(3000);
		
		System.out.println("Peer 2's neighbor list is " + peer2.getNeighborList());
		
		Thread.sleep(2000);
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void test() {
		try {
			logger.info("Starting test...");

			ArrayList<PeerApp> peers = new ArrayList<PeerApp>();
			for (int i = 1; i < 2; i++) {
				peers.add(new PeerApp(i, "127.0.0.1", this.peer.getPeer().getPort() + i, 9));
			}

			Thread.sleep(1000);
			for (PeerApp peer : peers) {
				this.peer.ping(peer.getPeer().getIP(), peer.getPeer().getPort());
				Thread.sleep(1000);
			}


			Thread.sleep(1000);

			logger.info("Peerlist of P1: {}", this.peer.plist());


			PeerApp test0r = new PeerApp(99, "127.0.0.1", 19876, 9);
			Thread.sleep(1000);
			test0r.ping(this.peer.getPeer().getIP(), this.peer.getPeer().getPort());

			Thread.sleep(1000);
			logger.info("Peerlist of P99: {}", test0r.plist());

			logger.info("P99 performing generic hello");
			test0r.helloAll();

			Thread.sleep(1000);

			logger.info("Peerlist of P99: {}", test0r.plist());

			logger.info("Peerlist of P{}: {}", this.peer.getPeer().getId(), this.peer.plist());

			for (PeerApp peer : peers) {
				logger.info("Peerlist of P{}: {}", peer.getPeer().getId(), peer.plist());
			}

			test0r.destroy();

			for (PeerApp peer : peers) {
				peer.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// the same as test but without the destruction of the peers

	private void test0()  throws InterruptedException {
		try {
			logger.info("Starting test...");

			ArrayList<PeerApp> peers = new ArrayList<PeerApp>();
			for (int i = 1; i < 2; i++) {
				peers.add(new PeerApp(i, "127.0.0.1", this.peer.getPeer().getPort() + i, 9));
			}

			Thread.sleep(1000);
			for (PeerApp peer : peers) {
				this.peer.ping(peer.getPeer().getIP(), peer.getPeer().getPort());
				Thread.sleep(1000);
			}


			Thread.sleep(1000);

			logger.info("Peerlist of P1: {}", this.peer.plist());


			PeerApp test0r = new PeerApp(99, "127.0.0.1", 19876, 9);
			Thread.sleep(1000);
			test0r.ping(this.peer.getPeer().getIP(), this.peer.getPeer().getPort());

			Thread.sleep(1000);
			logger.info("Peerlist of P99: {}", test0r.plist());

			logger.info("P99 performing generic hello");
			test0r.helloAll();

			Thread.sleep(1000);

			logger.info("Peerlist of P99: {}", test0r.plist());

			logger.info("Peerlist of P{}: {}", this.peer.getPeer().getId(), this.peer.plist());

			for (PeerApp peer : peers) {
				logger.info("Peerlist of P{}: {}", peer.getPeer().getId(), peer.plist());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void test2()  throws InterruptedException {
		try {
			logger.info("Starting test...");

			ArrayList<PeerApp> peers = new ArrayList<PeerApp>();
			for (int i = 0; i < 5; i++) {
				peers.add(new PeerApp(i, "127.0.0.1", this.peer.getPeer().getPort() + 1 + i, 9));
			}

			Thread.sleep(1000);
			for (PeerApp peer : peers) {
				this.peer.ping(peer.getPeer().getIP(), peer.getPeer().getPort());
				Thread.sleep(1000);
			}

			for (int i = 0; i < 4; i++) {
				peers.get(i).ping(peers.get(i + 1).getPeer().getIP(), peers.get(i + 1).getPeer().getPort());
			}


			Thread.sleep(1000);

			peers.get(peers.size() - 1).helloAll();

			Thread.sleep(1000);


			logger.info("Peerlist of P{}: {}", this.peer.getPeer().getId(), this.peer.plist());

			for (PeerApp peer : peers) {
				logger.info("Peerlist of P{}: {}", peer.getPeer().getId(), peer.plist());
				peer.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void test3() throws InterruptedException {
		try {
			PeerApp p2 = new PeerApp(2, "127.0.0.1", this.peer.getPeer().getPort() + 1, 9);
			Thread.sleep(1000);
			//			logger.info("Created P2");
			//			this.peer.becomeNeighbor(p2.getIP(), p2.getPort());
			//			logger.info("P1 tries to become neighbor of P2...");
			//			Thread.sleep(1000);

			logger.info("Peerlist of P0: {}", this.peer.plist());
			logger.info("Neighborlist of P0: {}", this.peer.nlist());

			logger.info("Peerlist of P2: {}", p2.plist());
			logger.info("Neighborlist of P2: {}", p2.nlist());

			p2.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void testNeighborhood() throws InterruptedException {
		ArrayList<PeerApp> peers = setupNetwork();
		Thread.sleep(2000);

		for (PeerApp peer : peers) {
			System.out.println(peer.plist());
			System.out.println(peer.nlist());
		}
	}

	private void testSearching() throws InterruptedException{
		try {
			Reporter.resetReporter();
			ArrayList<PeerApp> peers = setupNetwork();
			String shoot = "Stats\nBuildingNetwork:" + Reporter.getData().toString()+"\n";
			Random random = new Random();
			peers.get(random.nextInt(peers.size())).fileList.put("file", new File("p2p3.dot"));
			peers.get(random.nextInt(peers.size())).fileList.put("file2", new File("output.dot"));

			Reporter.resetReporter();

			peers.get(random.nextInt(peers.size())).startFloodSearch("file", 6);

			Thread.sleep(10000);
			shoot += "\nFloodSearch: \n"+Reporter.getData().toString()+"\n";
			Reporter.resetReporter();
			peers.get(random.nextInt(peers.size())).startWalkerSearch("file", 20, 3);

			Thread.sleep(10000);
			shoot += "\nWalkerSearch: \n"+Reporter.getData().toString()+"\n";
			for (PeerApp peer : peers) {
				peer.destroy();
			}

			logger.info(shoot);

		} catch (Exception e) {

		}
	}




	/**
	 * Method for parsing the inputs of the user in the console.
	 */
	private void parseInput(){
		System.out.print(">");

		String input;
		try {

			input = reader.readLine();
			if (input == null) {
				System.exit(0);
			}
			if (input.equals("exit")) {
				System.out.println("Shutting down program...");
				this.peer.destroy();
				System.exit(0);
			}

			if (input.equals("test")) {
				this.test();
			}

			if (input.equals("test2")) {
				this.test2();
			}

			if (input.equals("test3")) {
				this.test3();
			}

			if (input.equals("testn")) {
				this.testNeighborhood();
			}

			if (input.equals("tests")) {
				this.testSearching();
			}

			if (input.equals("testm")) {
				this.testMonday();
			}
			
			if (input.equals("test0")) {
				this.test0();
			}

			if (input.equals("testge")) {
				try {
					this.testGet();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}


			if (input.length() >= 5 && input.substring(0, 5).equals("hello")) {

				String[] address = input.substring(5).split(":");

				if (address.length == 2) {
					this.peer.ping(address[0].substring(1), Integer.parseInt(address[1]));
					System.out.println("Contacted " + address[0] + ":" + address[1]);
				} else {
					this.peer.helloAll();
					System.out.println("Peer requested peer lists from all known peers. (depth: 1)");
				}
			}

			if (input.equals("plist")) {
				System.out.println(this.peer.plist());
			}

			if (input.length() >= 5 && input.substring(0, 5).equals("nlist")) {
				nlistParse(input);
			}

			//TODO

			
			if (input.substring(0, 4).equals("find") && input.length() > 5) {
				String[] address = input.substring(5).split(" ");
				String filename = address[0];
				Integer time;
				if (address.length > 1) {
					time = Integer.parseInt(address[1]);
				} else {
					time = 5;
				}
				peer.startFloodSearch(filename, time);
				logger.info("Wrote file command with the name file argument ant the time: Name file: {} Time: {}", filename, time);

			}

			// Proper syntax for kfind:
			//   kfind _filename_ _ttl_ _numOfWalkers_
			
			if (input.substring(0, 5).equals("kfind") && input.length() > 6){
				String[] args = input.substring(5).split(" ");
				int ttl = 6;
				if (args.length > 0){
					String fileName = args[0].trim();
					if (args.length > 1){
						ttl = Integer.parseInt(args[1].trim());
					}
					if (args.length > 2){
						this.peer.startWalkerSearch(fileName, ttl, Integer.parseInt(args[2].trim()));
					} else {
						this.peer.startWalkerSearch(fileName, ttl);
					}
				}
			}

			/** Parsing the advanced walker search command */
			//   akfind _filename_ _ttl_ _numOfWalkers_
			
			if (input.substring(0, 5).equals("afind") && input.length() > 6){
				String[] args = input.substring(6).split(" ");
				int ttl = 6;
				if (args.length > 0){
					String fileName = args[0].trim();
					if (args.length > 1){
						ttl = Integer.parseInt(args[1].trim());
					}
					if (args.length > 2){
						this.peer.startAdvancedWalkerSearch(fileName, ttl, Integer.parseInt(args[2].trim()));
					} else {
						this.peer.startAdvancedWalkerSearch(fileName, ttl);
					}
				}
			}
			
			if (input.substring(0, 3).equals("	") && input.length() > 4) {
				String nameFile = input.substring(4);
				logger.info("Wrote get command with the name file argument: {}", nameFile);
				this.peer.getP2pFile(nameFile, this.peer.knownDataList.get(nameFile).getIP(), this.peer.knownDataList.get(nameFile).getPort());
			
			}

			if (input.equals("report")) {
				System.out.println("Recorded Data:\n " + Reporter.getData());
			}
			

			if (input.contains("testhostfile")) {
				this.testProcessHostFile();
			}

			if (input.contains("testm")) {
				this.testMonday();
			}
			
			checkConnection();
			parseInput();
		} catch (InterruptedException e){
			logger.error(e);
		} catch (IOException ioe) {
			System.out.println("IO error!");
			System.exit(1);
		}
	}


	/**
	 * Method for treat the input of "nlist xxx"
	 *
	 * @param input
	 * @throws IOException
	 */
	private void nlistParse(String input) throws IOException {

		int[] listPeers = null;
		String nameFile = null;

		// without any arguments
		if (input.length() == 5 && input.equals("nlist")) {
			listPeers = null;
			nameFile = null;
			logger.info("Writed command without any arguments");
			peer.nlistGraph(listPeers, nameFile, false);
		}

		// with the "all" argument
		if (input.contains("-o") && input.contains("all")) {
			String addrRaw = input.substring(5);
			String[] addr;
			addr = addrRaw.split("-o");
			nameFile = addr[1].substring(1);

			peer.nlistGraph(listPeers, nameFile, true);
			logger.info("Writed command with the all argument.");
		}

		// with only the nameFile argument
		if (input.contains("-o") && !(input.contains("p")) && !(input.contains("P") && !(input.contains("all")))) {
			String addrRaw = input.substring(5);
			String[] addr;
			addr = addrRaw.split("-o");
			nameFile = addr[1].substring(1);
			listPeers = null;
			logger.info("Writed command with only the name file argument (File name {})", nameFile);
			peer.nlistGraph(listPeers, nameFile, false);

		}

		// with only the peer list argument
		if (!input.contains("-o") && (input.contains("p") || input.contains("P"))) {
			String addrRaw = input.substring(6);
			String[] peersParsed = addrRaw.split(" ");
			listPeers = new int[peersParsed.length];
			int j = 0;
			for (int i = 0; i < peersParsed.length; i++) {
				String a1 = peersParsed[i];
				a1 = a1.replace("P", "");
				a1 = a1.replace("p", "");
				int a = Integer.parseInt(a1);
				listPeers[j] = a;
				j++;
			}
			logger.info("Writed command with only the peer list argument. (Peer list: {})", addrRaw);
			peer.nlistGraph(listPeers, nameFile, false);

		}

		// with the list of peers and the name of the file
		if (input.contains("-o") && ((input.contains("p")) || (input.contains("P")))) {
			String addrRaw = input.substring(6);
			String[] addr = addrRaw.split("-o");
			String[] peersParsed = addr[0].split(" ");
			listPeers = new int[peersParsed.length];
			nameFile = addr[1].substring(1);
			int j = 0;
			for (int i = 0; i < peersParsed.length; i++) {
				String a1 = peersParsed[i];
				a1 = a1.replace("P", "");
				a1 = a1.replace("p", "");
				int a = Integer.parseInt(a1);
				listPeers[j] = a;
				j++;
			}
			logger.info("Writed command with the peer list and the file name arguments. (Peer list: {}) (Name file: {})", addr[0], nameFile);
			peer.nlistGraph(listPeers, nameFile, false);
		}
	}

	/**
	 * Default Java init method
	 *
	 * @param args String array providing peer ID and port.
	 */
	public static void main(String[] args) {

		if (args.length != 3) {
			logger.error("Wrong number of arguments given, will be running in auto mode.");
		}

		new Main(args);
	}
}
