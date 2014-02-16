package vic;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vic.Entities.Peer;

import java.util.Map;
import java.util.Random;

public class Checking implements Runnable {

	PeerApp peerApp;

	private static final Logger logger = LogManager.getLogger(Checking.class.getName());


	public Checking(PeerApp app) {
		this.peerApp = app;
	}

	public void run (){
		try{
			this.checkConnection();
		}catch (InterruptedException e){
			logger.error("Checking was interrupted: {}", e.getMessage());
		}
	}

	/**
	 * Check the connection with all the Peers of the peerList
	 * and makes and update of the peerlist if there's any peer died.
	 *
	 */
	public void checkConnection() throws InterruptedException{
		if(randInt(1,5) == 3){
			for (Map.Entry<Integer, Peer> entry: this.peerApp.getPeerList().entrySet()) {
				this.peerApp.ping(entry.getValue().getIP(), entry.getValue().getPort());
			}
		}
		Thread.sleep(5000);
	}

	/**
	 * It returns a random number between min and max parameters.
	 *
	 * @param min Lower boundary for radnom number
	 * @param max Upper boundary for random number
	 * @return random number
	 */
	public static int randInt(int min, int max) {
		Random rand = new Random();
		return rand.nextInt((max - min) + 1) + min;
	}
}
