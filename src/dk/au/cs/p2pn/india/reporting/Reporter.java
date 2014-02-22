package dk.au.cs.p2pn.india.reporting;


import java.util.HashMap;

public class Reporter {

	private static HashMap<ReporterMeasurements, Integer> data;

	public Reporter() {
		// Register all counters


	}

	public static void init(){
		data = new HashMap<ReporterMeasurements, Integer>();
		for (ReporterMeasurements key : ReporterMeasurements.values()) {
			data.put(key, 0);
		}
	}

	/**
	 * Register an occurrence of $event
	 * @param event Type of the Event
	 */
	public static void addEvent(ReporterMeasurements event) {
		addEvent(event, 1);
	}

	/**
	 * Increase the counter for $event by $value
	 * @param event Type of the Event
	 * @param value Value to increase the counter by (positive only)
	 */
	public static synchronized void addEvent(ReporterMeasurements event, int value){
		if(value < 0) return;
		data.put(event, data.get(event)+value);
	}

}
