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

    private Object peer;

    private BufferedReader reader;
,
    public Main(String[] args){
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.peer = new Object();
        this.parseInput();
    }

    private void parseInput(){
        String input;
        try {
            System.out.print(">");
            input = reader.readLine();
            System.out.println(input);
            if(input.equals("exit")){
                System.exit(0);
            }
            this.peer.toString();
            parseInput();
        } catch (IOException ioe) {
            System.out.println("IO error!");
            System.exit(1);
        }

    }

    private void issueCommandToPeer(){

    }


    /**
     * Default Java init method
     * @param args String array providing peer ID and port.
     */
    public static void main(String[] args){
        System.out.println("Initiating peer...");

        if(args.length < 2){
            System.err.println("Too few arguments. Make sure to provide the Peer ID and the desired port to run on.");
        }

        Main director = new Main(args);
    }
}
