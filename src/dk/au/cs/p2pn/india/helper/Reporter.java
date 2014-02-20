package dk.au.cs.p2pn.india.helper;


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

	public void addEvent(ReporterMeasurements event) {
		this.addEvent(event, 1);
	}

	public void addEvent(ReporterMeasurements event, int value){
		this.data.put(event, this.data.get(event)+value);
	}

}
