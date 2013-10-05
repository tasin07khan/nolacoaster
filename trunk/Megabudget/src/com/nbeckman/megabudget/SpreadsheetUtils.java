package com.nbeckman.megabudget;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.util.ServiceException;

// This class contains helper methods that are used for getting spreadsheet
// feeds and individual spreadsheets, especially if they are used by
// multiple different activities. In general these methods operate in the
// current thread, and may be long-running, so they should probably be called
// from a background thread.
public class SpreadsheetUtils {
	// Given a context in which the service will be used and an account name
	// returns a SpreadsheetService authenticated for that user.
	// Will throw a UserRecoverableAuthException if the user has not yet
	// authorized spreadsheet access for this app, in which case the caller should
	// handle it and do something.
	public static SpreadsheetService setupSpreadsheetServiceInThisThread(
			Context context, String account_name) throws GoogleAuthException {
    	// Turn account name into a token, which must
    	// be done in a background task, as it contacts
    	// the network.
		String token = null;
		try {
			token = GoogleAuthUtil.getToken(
					context, 
					account_name, 
					"oauth2:https://spreadsheets.google.com/feeds https://docs.google.com/feeds");
		} catch (IOException e) {
			// TODO(nbeckman): This is a real problem. This is the exception that is
			// thrown when the user is not connected to the Internet. And this is
			// likely going to be the first place that we notice it, as we ensure that
			// we have a user to get the spreadsheet from. Consider rethrowing this.
			e.printStackTrace();
		}
		
		// Now that we have the token, can we actually list
		// the spreadsheets or anything...
		SpreadsheetService s =
				new SpreadsheetService("Megabudget");
		s.setAuthSubToken(token);
		return s;
	}

	// Given a context in which the service will be used and an account name
	// (used to authenticate the user) returns a SpreadsheetFeed for that user.
	// Will throw a UserRecoverableAuthException if the user has not yet
	// authorized spreadsheet access for this app, in which case the caller should
	// handle it and do something.
	public static SpreadsheetFeed currentSpreadsheetFeedInThisThread(Context context, String account_name) 
			throws GoogleAuthException {
		SpreadsheetService service = 
				setupSpreadsheetServiceInThisThread(context, account_name);
		try {
			// Define the URL to request. This should never change.
			// (Magic URL good for all users.)
			URL SPREADSHEET_FEED_URL = new URL(
					"https://spreadsheets.google.com/feeds/spreadsheets/private/full");

			// Make a request to the API and get all spreadsheets.
			SpreadsheetFeed feed = null;
			feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
			return feed;
		} catch (MalformedURLException e) {
			// TODO When does this happen? Hopefully never if the above URL is correct.
			e.printStackTrace();
		} catch (IOException e) {
			// TODO When does this happen?
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO When does this happen?
			e.printStackTrace();
		}
		return null;
	}
}
