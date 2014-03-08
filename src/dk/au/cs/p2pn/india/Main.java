package dk.au.cs.p2pn.india;

import dk.au.cs.p2pn.india.reporting.Reporter;
import dk.au.cs.p2pn.india.testing.Tester;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Main class to later run the peer that also is responsible for
 * taking input from the commandline and issuing the correct commands
 * to the peer.
 */
public class Main {

	private PeerApp peer;

	private BufferedReader reader;

	private static final Logger logger = LogManager.getLogger(Main.class.getSimpleName());

	public Main(String[] args) {
		// Initialize Reporter for this program instance.

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

			this.parseInput();

		} catch (Exception e) {
			e.printStackTrace();
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
			
			if (input.substring(0, 3).equals("get") && input.length() > 4) {
				String nameFile = input.substring(4);
				logger.info("Wrote get command with the name file argument: {}", nameFile);
				this.peer.getP2pFile(nameFile, this.peer.knownDataList.get(nameFile).getIP(), this.peer.knownDataList.get(nameFile).getPort());
			
			}

			if (input.equals("report")) {
				System.out.println("Recorded Data:\n " + Reporter.getData());
			}

			parseInput();
		} catch (IOException e) {
			logger.error(e);
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
		Reporter.init();

		// If started with the parameter "testing"
		// then load the testing instead of regular functionality
		if (args.length == 1 && args[0].equals("testing")){
			new Tester(args);
			return;
		}

		new Main(args);
	}
}
