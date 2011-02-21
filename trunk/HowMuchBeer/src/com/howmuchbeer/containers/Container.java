package com.howmuchbeer.containers;

/**
 * Interface for all containers (bottles, kegs, etc). Containers must
 * be able to convert a quantity into ounces.
 *  
 * @author nbeckman
 *
 */
public interface Container {
	/**
	 * For the given quantity of this container, how many ounces is it? Will round
	 * up to the next ounce.
	 */
	long convertToOunces(double quantity);
	
	String pluralizedName();
	
	double sizeInOunces();
}
