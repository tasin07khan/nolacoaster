package nbeckman.jumpcar;

/**
 * Miles per hour. Provides functions for
 * displaying speeds in miles per hour and
 * converting to units used in other parts of
 * the program. 
 *  
 * @author Nels E. Beckman
 *
 */
public final class MPH implements Comparable<MPH> {

	private final int mph;
	
	private MPH(int mph) {
		this.mph = mph;
	}
	
	/** Given a speed in miles per hour, returns a
	 *  representative instance. */
	public static MPH fromMilesPerHour(int mph) {
		return new MPH(mph);
	}
	
	@Override
	public int compareTo(MPH o) {
		return mph - o.mph;
	}

	
	
}
