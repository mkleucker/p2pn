package p2pn;

/**
 * Main class to later run the peer that also is responsible for
 * taking input from the commandline and issuing the correct commands
 * to the peer.
 *
 */
public class Main {

    private Object peer;

    /**
     * Default Java init method
     * @param args
     */
    public static void main(String[] args){
        System.out.println("Initiating peer...");

        if(args.length < 2){
            System.err.println("Too few arguments. Make sure to provide the Peer ID and the desired port to run on.");
        }

        System.out.print(">");
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

    private void parseInput(){

    }

    private void issueCommandToPeer(){

    }
}
