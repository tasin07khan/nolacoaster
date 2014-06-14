package com.howmuchbeer.main;

import java.io.IOException;
import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

/**
 * This servlet is run after the user is presented the captcha.
 * If 
 * 
 * @author nbeckman
 *
 */
public class PostCaptchaServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		// Did the user pass the captcha?
		String remoteAddr = req.getRemoteAddr();
        ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
        reCaptcha.setPrivateKey("6LfSYskSAAAAAFcTmr4EOvNLB5r4CtgWJHQ80SbM");

        String challenge = req.getParameter("recaptcha_challenge_field");
        String uresponse = req.getParameter("recaptcha_response_field");
        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);

        if (!reCaptchaResponse.isValid()) {
    		resp.sendRedirect("/?result=captchafail");
        	return;
        } 
		// Persist it
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Comment comment = new Comment(new Date(),
				req.getParameter("author"),
				new com.google.appengine.api.datastore.Text(req.getParameter("comment")));
		try {
			pm.makePersistent(comment);
		} finally {
			pm.close();
		}
		// Back to front page
		resp.sendRedirect("/?result=thanks");
	}

}
