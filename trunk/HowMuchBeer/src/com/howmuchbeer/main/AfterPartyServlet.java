package com.howmuchbeer.main;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
			// Get input
			Long attendees = Long.parseLong(req.getParameter("attendees"));
			Double quantity = Double.parseDouble(req.getParameter("quantity"));
			String craziness = req.getParameter("craziness");
			String container = req.getParameter("container");
			String ip = req.getRemoteAddr();
			
			if(attendees <= 0) {
				throw new IllegalArgumentException("attendees was invalid " + attendees);
			}
			
			// Conversions
			long ounces = ContainerFactory.containerFromName(container).convertToOunces(quantity);
			PartyCraziness craziness_ = PartyCraziness.valueOf(craziness);
			
			// Create beer event record
			BeerEventRecord event = 
				new BeerEventRecord(new Date(), ounces, attendees, craziness_);
			event.setIpAddress(ip);
			
			// Persist it
			PersistenceManager pm = PMF.get().getPersistenceManager();
	        try {
	            pm.makePersistent(event);
	        } finally {
	            pm.close();
	        }
			
	        // Back to front page
	        resp.sendRedirect("/?result=thanks");
		} catch(NumberFormatException nfe) {
		//	log.warning("Invalid format number got to server. " + nfe);
		} catch(IllegalArgumentException iae) {
		//	log.warning("Craziness value was not valid. " + iae);
		}
	}

}
