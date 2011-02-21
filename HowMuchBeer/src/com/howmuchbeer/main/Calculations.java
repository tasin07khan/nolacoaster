package com.howmuchbeer.main;

import java.util.ArrayList;
import java.util.List;

/**
 * A place for beer calculations.
 * 
 * @author nbeckman
 *
 */
public final class Calculations {
	private Calculations() {}
	
	/**
	 * Given an empty list, result will be 0.
	 */
	public static long mean(List<BeerEventRecord> events) {
		if(events.isEmpty())
			return 0L;
		
		long sum_oz = 0L;
		long sum_attendees = 0L;

		for (BeerEventRecord rec : events) {
			sum_oz += rec.getOuncesConsumed();
			sum_attendees += rec.getAttendees();
		}
		
		return sum_oz / sum_attendees;
	}
	
	private static long square(long l1) {
		return l1 * l1;
	}
	
	/**
	 * Given an empty list, result will be 0.
	 */
	public static long stdDev(List<BeerEventRecord> events) {
		if(events.isEmpty())
			return 0L;
		
		long mean = mean(events);
		
		List<Long> squared_diffs = new ArrayList<Long>(events.size());
		// first compute the difference of each data point from the mean, and square the result of each
		for(BeerEventRecord rec : events) {
			long oz_per_person = rec.getOuncesConsumed() / rec.getAttendees();
			squared_diffs.add( square(oz_per_person - mean) );
		}
		// Compute the average and take the sqare root
		long sum = 0;
		for(Long l : squared_diffs) {
			sum += l;
		}
		double std_dev = Math.sqrt(sum / squared_diffs.size());
		return (long)std_dev;
	}
}
