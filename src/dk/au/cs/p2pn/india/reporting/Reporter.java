package dk.au.cs.p2pn.india.reporting;


import java.util.HashMap;

public class Reporter {

	private HashMap<ReporterMeasurements, Integer> data;

	public Reporter() {
		// Register all counters
		this.data = new HashMap<ReporterMeasurements, Integer>();
		for (ReporterMeasurements key : ReporterMeasurements.values()) {
			this.data.put(key, 0);
		}
	}

	/**
	 * Register an occurrence of $event
	 * @param event Type of the Event
	 */
	public void addEvent(ReporterMeasurements event) {
		this.addEvent(event, 1);
	}

	/**
	 * Increase the counter for $event by $value
	 * @param event Type of the Event
	 * @param value Value to increase the counter by (positive only)
	 */
	public synchronized void addEvent(ReporterMeasurements event, int value){
		if(value < 0) return;

		this.data.put(event, this.data.get(event)+value);
	}

}
