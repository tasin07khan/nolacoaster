package com.howmuchbeer.main;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import java.util.Date;

/**
 * A record of a beer event. Contains the amount of beer drunk at a particular event
 * along with the number of people, the wild-ness of the event, and then a bunch of
 * other interesting but optional statistics. This is the basic persistent piece of
 * data in our application.
 * 
 * 
 * @author nbeckman
 *
 */
@PersistenceCapable
public class BeerEventRecord {

	/*
	 * Requiresd datat
	 */
	
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	
	@Persistent
    private Date dateOfEntry;
	
	@Persistent
	private Long ouncesConsumed;
	
	@Persistent
	private Long attendees;
	
	@Persistent
	private String partyCraziness;
	
	/*
	 * Optional information, future information
	 */
	@Persistent
	private String ipAddress = "";  // empty string is unknown

	@Persistent
	private Double averageABV = 0.0; // 0.0 is unknown
	
	@Persistent
	private Boolean enoughBeer = true; // Was it enough beer? Default is yes

	public BeerEventRecord(Date dateOfEntry, Long ouncesConsumed,
			Long attendees, PartyCraziness partyCraziness) {
		super();
		this.dateOfEntry = dateOfEntry;
		this.ouncesConsumed = ouncesConsumed;
		this.attendees = attendees;
		this.partyCraziness = partyCraziness.toString();
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Date getDateOfEntry() {
		return dateOfEntry;
	}

	public void setDateOfEntry(Date dateOfEntry) {
		this.dateOfEntry = dateOfEntry;
	}

	public Long getOuncesConsumed() {
		return ouncesConsumed;
	}

	public void setOuncesConsumed(Long ouncesConsumed) {
		this.ouncesConsumed = ouncesConsumed;
	}

	public Long getAttendees() {
		return attendees;
	}

	public void setAttendees(Long attendees) {
		this.attendees = attendees;
	}

	public String getPartyCraziness() {
		return partyCraziness;
	}

	public void setPartyCraziness(String partyCraziness) {
		this.partyCraziness = partyCraziness;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Double getAverageABV() {
		return averageABV;
	}

	public void setAverageABV(Double averageABV) {
		this.averageABV = averageABV;
	}

	public Boolean getEnoughBeer() {
		return enoughBeer;
	}

	public void setEnoughBeer(Boolean enoughBeer) {
		this.enoughBeer = enoughBeer;
	}
}
