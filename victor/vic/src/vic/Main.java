package vic;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
            this.peer = new PeerApp(0, "127.0.0.1", 18523, 9, 9);

            this.parseInput();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void test() {
        try {
            logger.info("Starting test...");

            ArrayList<PeerApp> peers = new ArrayList<PeerApp>();
            for (int i = 0; i < 5; i++) {
                peers.add(new PeerApp(i, "127.0.0.1", this.peer.getPort()+1+i, 9, 9));
            }

            Thread.sleep(1000);
            for (PeerApp peer : peers) {
                this.peer.hello(peer.getIP(), peer.getPort());
                Thread.sleep(1000);
            }

            Thread.sleep(3000);

            for (PeerApp peer: peers) {
                peer.destroyPeer();
            }

            logger.debug(this.peer.plist());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void parseInput() {
        String input;
        try {
            System.out.print(">");
            input = reader.readLine();
            System.out.println(input);
            if (input.equals("exit")) {
                // TODO: Close peer properly.
                System.exit(0);
            }

            if (input.equals("test")) {
                this.test();
            }
            if (input.length() >= 5 && input.substring(0, 5).equals("hello")) {

                String addressraw = input.substring(5);
                String[] address = addressraw.split(":");

                if (address.length == 2) {
                    this.peer.hello(address[0].substring(1), Integer.parseInt(address[1]));
                } else {
                    this.peer.helloAll();
                }
            }

            if (input.equals("plist")) {
                System.out.println(this.peer.plist());
            }

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
        System.out.println("Initiating peer...");

        if (args.length < 2) {
            System.err.println("Too few arguments. Make sure to provide the Peer ID and the desired port to run on.");
        }

        //
        Main director = new Main(args);
    }
}
