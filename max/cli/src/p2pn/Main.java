package p2pn;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Main class to later run the peer that also is responsible for
 * taking input from the commandline and issuing the correct commands
 * to the peer.
 *
 */
public class Main {

    //private Object peer;

    /**
     * Default Java init method
     * @param args String array providing peer ID and port.
     */
    public static void main(String[] args){
        System.out.println("Initiating peer...");

        if(args.length < 2){
            System.err.println("Too few arguments. Make sure to provide the Peer ID and the desired port to run on.");
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        parseInput(br);



    }

    /**
     * Initiating the peer.
     */
    private void initPeer(String id, int port){
        //this.peer = new Peer(id, port);
    }

    private void shutdownPeer(){
        //this.peer.shutdown();
    }

    private static void parseInput(BufferedReader br){
        String input;
        try {
            System.out.print(">");
            input = br.readLine();
            System.out.println(input);
            if(input.equals("exit")){
                System.exit(0);
            }
            parseInput(br);
        } catch (IOException ioe) {
            System.out.println("IO error!");
            System.exit(1);
        }

    }

    private void issueCommandToPeer(){

    }
}
