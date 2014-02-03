package vic;


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

    private PeerApp peer;
    private PeerApp peer2;

    private BufferedReader reader;

    public Main(String[] args){
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.peer = new PeerApp(12, "127.0.0.1", 18523, 9, 9);
        this.peer2 = new PeerApp(13, "127.0.0.1", 18524, 9, 9);
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
            if(input.length() >= 5 && input.substring(0,5).equals("hello")){

                String addressraw = input.substring(6);
                String[] address = addressraw.split(":");
                if(address.length > 2){
                    this.peer.hello(address[0], Integer.parseInt(address[1]));
                }else{
                    this.peer.helloAll();
                }
            }
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
