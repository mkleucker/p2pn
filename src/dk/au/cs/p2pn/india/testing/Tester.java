package dk.au.cs.p2pn.india.testing;


import dk.au.cs.p2pn.india.PeerApp;
import dk.au.cs.p2pn.india.reporting.Reporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class Tester {

	private BufferedReader reader;

	private static final Logger logger = LogManager.getLogger(Tester.class.getSimpleName());

	private int startPort = 18523;

	public Tester(String[] args){
		try{
			logger.info("Starting Test Environment");

			this.reader = new BufferedReader(new InputStreamReader(System.in));

			this.parseInput();

		}catch (Exception e){
			e.printStackTrace();
		}
	}

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

			if (input.equals("testw")) {
				this.testWed();
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

			if (input.contains("testhostfile")) {
				this.testProcessHostFile();
			}

			if (input.contains("testm")) {
				this.testMonday();
			}

		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private ArrayList<PeerApp> setupNetwork() {
		return setupNetwork(50);
	}

	private ArrayList<PeerApp> setupNetwork(int count) {
		Random rand = new Random();

		ArrayList<PeerApp> peers = new ArrayList<PeerApp>();
		try {


			for (int i = 1; i < count; i++) {
				peers.add(new PeerApp(i, "127.0.0.1", startPort + i, rand.nextInt(9) + 1));
			}
			Thread.sleep(2000);

			for (PeerApp peer : peers) {
				int numberOfConnections = rand.nextInt(count);
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

	private void testWed(){

		try {

			String ip = "84.238.27.224";
			PeerApp peer1 = new PeerApp(1, ip, 18527, 5);
			PeerApp peer2 = new PeerApp(2, ip, 18528, 5);
			PeerApp peer3 = new PeerApp(3, ip, 18529, 5);
			PeerApp peer4 = new PeerApp(4, ip, 18530, 5);
			PeerApp peer5 = new PeerApp(5, ip, 18531, 5);

			Thread.sleep(2000);

			peer1.uploadFile("duck.mp3");
			peer2.ping(peer1.getPeer().getIP(), peer1.getPeer().getPort());
			peer3.ping(peer4.getPeer().getIP(), peer4.getPeer().getPort());

			Thread.sleep(2000);

		/*
		System.out.println("Peer 1's peer list is " + peer1.plist());
		System.out.println("Peer 2's peer list is " + peer2.plist());
		System.out.println("Peer 3's peer list is " + peer3.plist());
		System.out.println("Peer 4's peer list is " + peer4.plist());
		System.out.println("Peer 5's peer list is " + peer5.plist());
		*/

			peer1.startNegotiate();
			peer3.startNegotiate();

			Thread.sleep(2000);

			peer2.ping(peer3.getPeer().getIP(), peer3.getPeer().getPort());
			peer5.ping(peer4.getPeer().getIP(), peer4.getPeer().getPort());

			Thread.sleep(2000);

		/*
		System.out.println("Peer 1's peer list is " + peer1.plist());
		System.out.println("Peer 2's peer list is " + peer2.plist());
		System.out.println("Peer 3's peer list is " + peer3.plist());
		System.out.println("Peer 4's peer list is " + peer4.plist());
		System.out.println("Peer 5's peer list is " + peer5.plist());
		*/

			peer2.startNegotiate();
			peer5.startNegotiate();

			Thread.sleep(2000);
		/*
		System.out.println("Peer 1's neighbor list is " + peer1.getNeighborList());
		System.out.println("Peer 2's neighbor list is " + peer2.getNeighborList());
		System.out.println("Peer 3's neighbor list is " + peer3.getNeighborList());
		System.out.println("Peer 4's neighbor list is " + peer4.getNeighborList());
		System.out.println("Peer 5's neighbor list is " + peer5.getNeighborList());
		*/
			Thread.sleep(3000);

			peer3.startAdvancedWalkerSearch("duck.mp3", 2, 2);

			Thread.sleep(4000);

			peer3.printWeight();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void test() {
		try {
			logger.info("Starting test...");

			PeerApp testPeer = new PeerApp(0, "127.0.0.1", startPort-1, 8);
			ArrayList<PeerApp> peers = new ArrayList<PeerApp>();
			for (int i = 1; i < 2; i++) {
				peers.add(new PeerApp(i, "127.0.0.1", this.startPort + i, 9));
			}

			Thread.sleep(1000);
			for (PeerApp peer : peers) {
				testPeer.ping(peer.getPeer().getIP(), peer.getPeer().getPort());
				Thread.sleep(1000);
			}


			Thread.sleep(1000);

			logger.info("Peerlist of P1: {}", testPeer.plist());


			PeerApp test0r = new PeerApp(99, "127.0.0.1", 19876, 9);
			Thread.sleep(1000);
			test0r.ping(testPeer.getPeer().getIP(), testPeer.getPeer().getPort());

			Thread.sleep(1000);
			logger.info("Peerlist of P99: {}", test0r.plist());

			logger.info("P99 performing generic hello");
			test0r.helloAll();

			Thread.sleep(1000);

			logger.info("Peerlist of P99: {}", test0r.plist());

			logger.info("Peerlist of P{}: {}", testPeer.getPeer().getId(), testPeer.plist());

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

			PeerApp testPeer = new PeerApp(0, "127.0.0.1", startPort-1, 8);

			ArrayList<PeerApp> peers = new ArrayList<PeerApp>();
			for (int i = 1; i < 2; i++) {
				peers.add(new PeerApp(i, "127.0.0.1", this.startPort + i, 9));
			}

			Thread.sleep(1000);
			for (PeerApp peer : peers) {
				testPeer.ping(peer.getPeer().getIP(), peer.getPeer().getPort());
				Thread.sleep(1000);
			}


			Thread.sleep(1000);

			logger.info("Peerlist of P1: {}", testPeer.plist());


			PeerApp test0r = new PeerApp(99, "127.0.0.1", 19876, 9);
			Thread.sleep(1000);
			test0r.ping(testPeer.getPeer().getIP(), this.startPort);

			Thread.sleep(1000);
			logger.info("Peerlist of P99: {}", test0r.plist());

			logger.info("P99 performing generic hello");
			test0r.helloAll();

			Thread.sleep(1000);

			logger.info("Peerlist of P99: {}", test0r.plist());

			logger.info("Peerlist of P{}: {}", testPeer.getPeer().getId(), testPeer.plist());

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
			PeerApp testPeer = new PeerApp(0, "127.0.0.1", startPort-1, 8);

			ArrayList<PeerApp> peers = new ArrayList<PeerApp>();
			for (int i = 0; i < 5; i++) {
				peers.add(new PeerApp(i, "127.0.0.1", this.startPort + 1 + i, 9));
			}

			Thread.sleep(1000);
			for (PeerApp peer : peers) {
				testPeer.ping(peer.getPeer().getIP(), peer.getPeer().getPort());
				Thread.sleep(1000);
			}

			for (int i = 0; i < 4; i++) {
				peers.get(i).ping(peers.get(i + 1).getPeer().getIP(), peers.get(i + 1).getPeer().getPort());
			}


			Thread.sleep(1000);

			peers.get(peers.size() - 1).helloAll();

			Thread.sleep(1000);


			logger.info("Peerlist of P{}: {}", testPeer.getPeer().getId(), testPeer.plist());

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
			PeerApp testPeer = new PeerApp(0, "127.0.0.1", startPort-1, 8);

			PeerApp p2 = new PeerApp(2, "127.0.0.1", this.startPort + 1, 9);
			Thread.sleep(1000);
			//			logger.info("Created P2");
			//			this.peer.becomeNeighbor(p2.getIP(), p2.getPort());
			//			logger.info("P1 tries to become neighbor of P2...");
			//			Thread.sleep(1000);

			logger.info("Peerlist of P0: {}", testPeer.plist());
			logger.info("Neighborlist of P0: {}", testPeer.nlist());

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
			peers.get(random.nextInt(peers.size())).uploadFile("file");
			peers.get(random.nextInt(peers.size())).uploadFile("file2");

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

	@SuppressWarnings("unused")
	private void testGet() throws InterruptedException {
		PeerApp peer1 = new PeerApp(0, "127.0.0.1", 18525, 9);
		PeerApp testPeer = new PeerApp(2, "127.0.0.1", startPort-1, 8);

		Thread.sleep(1000);
		testPeer.getP2pFile("duck.jpg", "127.0.0.1", 18525);
	}
}
