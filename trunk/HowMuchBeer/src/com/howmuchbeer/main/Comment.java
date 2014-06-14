package com.howmuchbeer.main;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

/**
 * A comment left by a visitor.
 * 
 * @author nbeckman
 *
 */
@PersistenceCapable
public class Comment {
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	
	@Persistent
    private Date dateOfEntry;
	
	@Persistent
	private Boolean moderated;
	
	// Comment and author come from the user, must be scrubbed.
	
	@Persistent
	private String author;
	
	@Persistent
	private com.google.appengine.api.datastore.Text comment;

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

	public Boolean getModerated() {
		return moderated;
	}

	public void setModerated(Boolean moderated) {
		this.moderated = moderated;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public com.google.appengine.api.datastore.Text getComment() {
		return comment;
	}

	public void setComment(com.google.appengine.api.datastore.Text comment) {
		this.comment = comment;
	}

	public Comment(Date dateOfEntry, String author, Text comment) {
		super();
		this.moderated = false;
		this.dateOfEntry = dateOfEntry;
		this.author = author;
		this.comment = comment;
	}
	
	
}
