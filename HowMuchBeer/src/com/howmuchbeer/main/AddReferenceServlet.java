package com.howmuchbeer.main;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * The add_reference servlet allows an administrator to add a special entry
 * with a citation.
 * 
 * @author nbeckman
 *
 */
public final class AddReferenceServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			BeerEventRecord event = AfterPartyServlet.extractDefaultRecord(req);
			
			String citation = req.getParameter("citation");
			event.setCitation(citation);
			event.setIsCitation(true);
			
			String ip = req.getRemoteAddr();
			event.setIpAddress(ip);
			
			// Was user logged in?
			UserService userService = UserServiceFactory.getUserService();
	        User user = userService.getCurrentUser();
			event.setAuthor(user);
			
			// Now, don't persist it unless this is the admin!
			if(userService.isUserLoggedIn() && userService.isUserAdmin()) {
				PersistenceManager pm = PMF.get().getPersistenceManager();
				try {
					pm.makePersistent(event);
				} finally {
					pm.close();
				}
		        // Back to front page
		        resp.sendRedirect("/reference.jsp?result=thanks");
			} else {
				resp.sendRedirect("/reference.jsp?result=nothanks");
			}
			
		} catch(NumberFormatException nfe) {
		//	log.warning("Invalid format number got to server. " + nfe);
		} catch(IllegalArgumentException iae) {
		//	log.warning("Craziness value was not valid. " + iae);
		}
	}

}
