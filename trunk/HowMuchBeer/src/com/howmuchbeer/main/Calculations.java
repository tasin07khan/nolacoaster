package com.howmuchbeer.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A place for beer calculations.
 * 
 * @author nbeckman
 *
 */
public final class Calculations {
	
	private static final int HAS_AUTHOR_WEIGHT = 4;
	
	private static final int IS_CITATION_WEIGHT = 25;
	
	private Calculations() {}
	
	/**
	 * Given an empty list, result will be 0. Performs a WEIGHTED average.
	 */
	public static long mean(List<BeerEventRecord> events) {
		if(events.isEmpty())
			return 0L;
		
		long sum_oz = 0L;
		long sum_attendees = 0L;

		for (BeerEventRecord rec : events) {
			long ounces_consumed = rec.getOuncesConsumed();
			long attendees = rec.getAttendees();
			
			// Apply weighting...
			if( rec.getIsCitation() ) {
				ounces_consumed *= IS_CITATION_WEIGHT;
				attendees *= IS_CITATION_WEIGHT;
			} else if( rec.getAuthor() != null ) {
				ounces_consumed *= HAS_AUTHOR_WEIGHT;
				attendees *= HAS_AUTHOR_WEIGHT;
			}
			
			sum_oz += ounces_consumed;
			sum_attendees += attendees;
		}
		
		return sum_oz / sum_attendees;
	}
	
	private static long square(long l1) {
		return l1 * l1;
	}
	
	/**
	 * Given an empty list, result will be 0. Standard deviation is of
	 * the weighted average.
	 */
	public static long stdDev(List<BeerEventRecord> events) {
		if(events.isEmpty())
			return 0L;
		
		long mean = mean(events);
		
		List<Long> squared_diffs = new ArrayList<Long>(events.size());
		// first compute the difference of each data point from the mean, and square the result of each
		for(BeerEventRecord rec : events) {
			long oz_per_person = rec.getOuncesConsumed() / rec.getAttendees();
			
			// Weight appropriately...
			long squared_diff = square(oz_per_person - mean);
			if( rec.getIsCitation() ) {
				squared_diffs.addAll(Collections.<Long>nCopies(IS_CITATION_WEIGHT, squared_diff));
			} else if( rec.getAuthor() != null ) {
				squared_diffs.addAll(Collections.<Long>nCopies(HAS_AUTHOR_WEIGHT, squared_diff));
			} else {
				squared_diffs.add( squared_diff );
			}
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
