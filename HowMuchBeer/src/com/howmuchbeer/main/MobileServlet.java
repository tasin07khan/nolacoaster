package com.howmuchbeer.main;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.howmuchbeer.containers.BasicAssortment;
import com.howmuchbeer.containers.ContainerAssortment;

/**
 * The mobile servlet, responds in plain text rather than in HTML.
 * 
 * @author nbeckman
 *
 */
public class MobileServlet extends HttpServlet {
	
	static class MissingParameter extends RuntimeException {
		final private String parameter;
		
		MissingParameter(String s) {
			this.parameter = s;
		}
		
		String parameter() {
			return parameter;
		}
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		
		try {
			// This is a simple request, so just get attendees, craziness and safe value
			String craziness = req.getParameter("craziness");
			if(craziness==null) throw new MissingParameter("craziness");
			craziness = craziness.toUpperCase();
			
			String attendees_ = req.getParameter("attendees");
			if(attendees_==null) throw new MissingParameter("attendees");
			Long attendees = Long.parseLong(attendees_);
			
			String safe_ = req.getParameter("safe");
			
			if(safe_==null) throw new MissingParameter("safe");
			
			try { 
				PartyCraziness.valueOf(craziness); 
			} catch(IllegalArgumentException iae) {
				// Normal if you entered something wrong
				craziness = PartyCraziness.NORMAL.toString();
			}
			
			PartySafety ps;
			try {
				ps = PartySafety.valueOf(safe_.toUpperCase());
			} catch(IllegalArgumentException iae) {
				ps = PartySafety.NORMAL;
			}
			
			// Do query
		    PersistenceManager pm = PMF.get().getPersistenceManager();
			String query = "select from " + BeerEventRecord.class.getName() + " where partyCraziness=='" + craziness + "'";
		    List<BeerEventRecord> events = (List<BeerEventRecord>) pm.newQuery(query).execute();
		    
		    if(!events.isEmpty()) {
				// Now, finally, get mean & std-dev, return Assortment	
		        long average_oz = Calculations.mean(events);
		    	long std_dev = Calculations.stdDev(events);
		        long mean_ounces = average_oz * attendees;
		        long above_ounces = mean_ounces + std_dev;
		        long below_ounces = mean_ounces - std_dev;
		        below_ounces = below_ounces < 0 ? 0 : below_ounces;
		        
		        ContainerAssortment assortment = new BasicAssortment();
		        Iterable<String> iterable;
		        switch(ps) {
		        case NORMAL:
		        	iterable = assortment.resultsForOunces(mean_ounces);
		        	break;
		        case ABOVE:
		        	iterable = assortment.resultsForOunces(above_ounces);
		        	break;
		        case BELOW:
		        	iterable = assortment.resultsForOunces(below_ounces);
		        	break;
		        default:
		        	throw new RuntimeException("Impossible");
		        }
		        
		        for(String s : iterable) {
		        	resp.getWriter().println(s);
		        }
		    } else {
		    	resp.getWriter().println("Not enough data on " + craziness + " parties yet. Try back later.");
		    }
			
		} catch(NumberFormatException nfe) {
			resp.getWriter().println("Attendees was not a number. Try again.");
		} catch(MissingParameter mp) {
			resp.getWriter().println("Missing parameter: " + mp.parameter());
		}
		
	    resp.flushBuffer();
	}
}
