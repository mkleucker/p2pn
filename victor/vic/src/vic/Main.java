package vic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
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

	private static final Logger logger = LogManager.getLogger(PeerApp.class.getName());

	public Main(String[] args) {
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

	private void test() {
		try {
			logger.info("Starting test...");

			ArrayList<PeerApp> peers = new ArrayList<PeerApp>();
			for (int i = 1; i < 6; i++) {
				peers.add(new PeerApp(i, "127.0.0.1", this.peer.getPort() + i, 9));
			}

			Thread.sleep(1000);
			for (PeerApp peer : peers) {
				this.peer.ping(peer.getIP(), peer.getPort());
				Thread.sleep(1000);
			}


			Thread.sleep(3000);

			logger.info("Peerlist of P1: {}", this.peer.plist());


			PeerApp test0r = new PeerApp(99, "127.0.0.1", 19876, 9);
			Thread.sleep(1000);
			test0r.ping(this.peer.getIP(), this.peer.getPort());
			Thread.sleep(1000);
			logger.info("Peerlist of P99: {}", test0r.plist());

			logger.info("P99 performing generic hello");
			test0r.helloAll();

			Thread.sleep(10000);

			logger.info("Peerlist of P99: {}", test0r.plist());

			logger.info("Peerlist of P{}: {}", this.peer.getId(), this.peer.plist());

			for (PeerApp peer : peers) {
				logger.info("Peerlist of P{}: {}", peer.getId(), peer.plist());
			}

			test0r.destroy();

			for (PeerApp peer : peers) {
				peer.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void test2() {
		try {
			logger.info("Starting test...");

			ArrayList<PeerApp> peers = new ArrayList<PeerApp>();
			for (int i = 0; i < 5; i++) {
				peers.add(new PeerApp(i, "127.0.0.1", this.peer.getPort() + 1 + i, 9));
			}

			Thread.sleep(1000);
			for (PeerApp peer : peers) {
				this.peer.ping(peer.getIP(), peer.getPort());
				Thread.sleep(1000);
			}

			for (int i = 0; i < 4; i++) {
				peers.get(i).ping(peers.get(i + 1).getIP(), peers.get(i + 1).getPort());
			}


			Thread.sleep(1000);

			peers.get(peers.size() - 1).helloAll();

			Thread.sleep(1000);


			logger.info("Peerlist of P{}: {}", this.peer.getId(), this.peer.plist());

			for (PeerApp peer : peers) {
				logger.info("Peerlist of P{}: {}", peer.getId(), peer.plist());
				peer.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void test3() {
		try {
			PeerApp p2 = new PeerApp(2, "127.0.0.1", this.peer.getPort() + 1, 9);
			Thread.sleep(1000);
			logger.info("Created P2");
			this.peer.becomeNeighbor(p2.getIP(), p2.getPort());
			logger.info("P1 tries to become neighbor of P2...");
			Thread.sleep(1000);

			logger.info("Peerlist of P0: {}", this.peer.plist());
			logger.info("Neighborlist of P0: {}", this.peer.nlist());

			logger.info("Peerlist of P2: {}", p2.plist());
			logger.info("Neighborlist of P2: {}", p2.nlist());

			p2.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void testNeighborhood() {
		try {

			Random rand = new Random();

			ArrayList<PeerApp> peers = new ArrayList<PeerApp>();
			int numOfPeers = 5;
			int port = this.peer.getPort();
			for (int i = 1; i < numOfPeers; i++) {
				peers.add(new PeerApp(i, "127.0.0.1", port + i, rand.nextInt(9)+1));
			}
			Thread.sleep(5000);

			for (PeerApp peer : peers) {
				int numberOfConnections = rand.nextInt(numOfPeers);
				System.out.println(numberOfConnections);
				for (int i = 0; i < numberOfConnections; i++) {
					peer.ping("127.0.0.1", peers.get(rand.nextInt(peers.size())).getPort());
				}
			}

			Thread.sleep(5000);

			for (PeerApp peer : peers) {
				peer.startNegotiate();
			}

			Thread.sleep(5000);

			for (PeerApp peer : peers) {
				System.out.println(peer.plist());
				System.out.println(peer.nlist());
			}

			Thread.sleep(5000);

			for (PeerApp peer : peers) {
				peer.destroy();
			}
		} catch (Exception e) {

		}
	}

	/**
	 * Method for parsing the inputs of the user in the console.
	 */
	@SuppressWarnings("InfiniteRecursion")
	private void parseInput() {
		String input;
		try {
			System.out.print(">");
			input = reader.readLine();
			if (input == null) {
				System.exit(0);
			}
			if (input.equals("exit")) {
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

			if (input.length() >= 5 && input.substring(0, 5).equals("hello")) {

				String addressraw = input.substring(5);
				String[] address = addressraw.split(":");

				if (address.length == 2) {
					this.peer.ping(address[0].substring(1), Integer.parseInt(address[1]));
				} else {
					this.peer.helloAll();
				}
			}


			if (input.equals("plist")) {
				System.out.println(this.peer.plist());
			}
			checkConnection();
			parseInput();
		} catch (IOException ioe) {
			System.out.println("IO error!");
			System.exit(1);
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
