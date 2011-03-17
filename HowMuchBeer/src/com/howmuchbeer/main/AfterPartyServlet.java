package com.howmuchbeer.main;

import java.io.IOException;
import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.howmuchbeer.containers.Container;
import com.howmuchbeer.containers.ContainerFactory;

/**
 * The servlet that is run after a user submits a
 * beer event record. Responsible for entering the
 * record into the database.
 * 
 * @author nbeckman
 *
 */
public class AfterPartyServlet extends HttpServlet {

	//private static final Logger log = Logger.getLogger(AfterPartyServlet.class.getName());
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			BeerEventRecord event = extractDefaultRecord(req);
			
			// Detect spam.
			if(!DetectSpam(event)) {
				String ip = req.getRemoteAddr();
				event.setIpAddress(ip);

				// Was user logged in?
				UserService userService = UserServiceFactory.getUserService();
				User user = userService.getCurrentUser();
				event.setAuthor(user);

				// Persist it
				PersistenceManager pm = PMF.get().getPersistenceManager();
				try {
					pm.makePersistent(event);
				} finally {
					pm.close();
				}
			}
			
	        // Back to front page
	        resp.sendRedirect("/?result=thanks");
		} catch(NumberFormatException nfe) {
		//	log.warning("Invalid format number got to server. " + nfe);
		} catch(IllegalArgumentException iae) {
		//	log.warning("Craziness value was not valid. " + iae);
		}
	}

	private static final Container TWELVE_OZ_BOTTLE = ContainerFactory.containerFromName(ContainerFactory.BOTTLE_12); 
	
	/**
	 * Returns true if spam was detected.
	 */
	private boolean DetectSpam(BeerEventRecord event) {
		// For now we are doing something pretty dumb, which is just to see if
		// this record is greater than some threshold.
		double max_per_person = 20.0 * TWELVE_OZ_BOTTLE.sizeInOunces();
		if(event.getOuncesConsumed() / event.getAttendees() > max_per_person) {
			return true;
		}
		return false;
	}

	static BeerEventRecord extractDefaultRecord(HttpServletRequest req) {
		// Get input
		Long attendees = Long.parseLong(req.getParameter("attendees"));
		Double quantity = Double.parseDouble(req.getParameter("quantity"));
		String craziness = req.getParameter("craziness");
		String container = req.getParameter("container");
		
		if(attendees <= 0) {
			throw new IllegalArgumentException("attendees was invalid " + attendees);
		}
		
		// Conversions
		long ounces = ContainerFactory.containerFromName(container).convertToOunces(quantity);
		PartyCraziness craziness_ = PartyCraziness.valueOf(craziness);
		
		// Create beer event record
		BeerEventRecord event = 
			new BeerEventRecord(new Date(), ounces, attendees, craziness_);
		return event;
	}

}
