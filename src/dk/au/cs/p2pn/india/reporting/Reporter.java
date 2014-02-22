package dk.au.cs.p2pn.india.reporting;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Reporter {

	private static Map<ReporterMeasurements, Integer> data;

	public static void init(){
		data =  Collections.synchronizedMap(new HashMap<ReporterMeasurements, Integer>());
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

	/**
	 * Get the recorded metrics.
	 * @return Map with the metric as key and the value
	 */
	public static synchronized HashMap<String, Integer> getData(){
		HashMap<String, Integer> retVal = new HashMap<String, Integer>();
		for (ReporterMeasurements metric : ReporterMeasurements.values()){
			retVal.put(metric.toString(), data.get(metric));
		}
		return retVal;
	}
}
