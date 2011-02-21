package com.howmuchbeer.containers;

import java.util.Iterator;

/**
 * An assortment of containers basically determines what sorts of containers you should be
 * buying. I.e., if you don't want kegs or can't buy power hours, you don't want them to
 * be returned to you as a thing to buy.
 * 
 * @author nbeckman
 *
 */
public interface ContainerAssortment {

	/**
	 * Given the number of ounces you need, returns a 
	 * textual description of the produces and quantities
	 * you should buy, one per line.
	 */
	Iterable<String> resultsForOunces(long ounces);	
}