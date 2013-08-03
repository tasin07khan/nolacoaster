package com.nbeckman.megabudget;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import com.nbeckman.megabudget.util.SystemUiHider;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ServiceException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.View;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

	private AccountManager mAccountManager;

	// The account name that the user wants to use.
	// Loaded from the preferences store. If not present,
	// we use the AccountManager to let the user choose an account.	
	private String mAccountName;
	
	private static final String kAccountPreferencesName = "ACCOUNT_PREFERENCES_NAME";
	
	private static final int kAccountChoiceIntent = 9;
	private static final int kModifySpreadSheetIntent = 10;
	private static final int kFileChoiceIntent = 11;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        
        findViewById(R.id.dummy_button).setOnClickListener(mButtonPressListener);
        
        // See if we have a preferred account stored, and if so, load it,
        // otherwise ask the user.
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        if (settings.contains(kAccountPreferencesName)) {
        	this.mAccountName = settings.getString(kAccountPreferencesName, "nobody@google.com");
        	this.launchChooseFileActivity();
        } else {
			Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
	       	         false, null, null, null, null);
	       	startActivityForResult(intent, kAccountChoiceIntent);
        }
    }

    // Launches the choose file activity, which will look at the user's account and
    // determine which spreadsheets are available and let them choose which one they
    // want to be their budget. We'll probably only do this once, but it needs to be
    // done after we know the account. It also needs to be done with a connection
    // to the spreadsheets...
    private void launchChooseFileActivity() {
    	// TODO(nbeckman): Put loading thingy onscreen because this takes a
    	// while and you have no idea what is going on.
    	(new AsyncTask<String, String,String>(){
    		@Override
    		protected String doInBackground(String... arg0) {
    			SpreadsheetFeed feed = null;
    			try {
    				feed = currentSpreadsheetFeedInThisThread();
    			} catch (UserRecoverableAuthException e) {
    				// This is NECESSARY so the user can say, "yeah I want
    				// this app to have permission to read my spreadsheet."
    				// TODO Make sure we get back to this call if we go down this path.
    				Intent recoveryIntent = e.getIntent();
    				startActivityForResult(recoveryIntent, 2);
    			}
    			if (feed != null) {
    				// From the feed, get an array of file names to put in the request to
    				// the file chooser activity.
    				List<SpreadsheetEntry> spreadsheets = feed.getEntries();
    				String[] spreadsheet_file_names = new String[spreadsheets.size()];

    				int index = 0;
    				for (SpreadsheetEntry spreadsheet : spreadsheets) {
    					final String name = spreadsheet.getTitle().getPlainText();
    					spreadsheet_file_names[index] = name;
    					index++;
    				}
    				Intent choose_file_intent = new Intent(FullscreenActivity.this, ChooseFileActivity.class);
    				choose_file_intent.putExtra("spreadsheet_files", spreadsheet_file_names);
    				startActivityForResult(choose_file_intent, kFileChoiceIntent);
    			}
    			return "WHAT";
    		}}).execute();
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    protected void onActivityResult(final int requestCode, final int resultCode,
            final Intent data) {
        if (requestCode == kAccountChoiceIntent && resultCode == RESULT_OK) {
            final String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            this.mAccountName = accountName;
            // Account chosen for the first time. Store it to the preferences.
            SharedPreferences settings = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(kAccountPreferencesName, accountName);
            editor.commit();
            
            // Now move on to choosing a budget file.
            this.launchChooseFileActivity();
        } else if (requestCode == kModifySpreadSheetIntent && resultCode == RESULT_OK) {
      } else if (requestCode == 2 && resultCode == RESULT_OK) {
    	  // After the user YAYs or NAYs our permission request, we are
    	  // taken here, so if we wanted to grab the token now we could.
      }
      
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    View.OnClickListener mButtonPressListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			modifySpreadsheet();
		}
	};
    
	// Gets a token for the current user, mAccountName, which should not be null.
	// Will throw a UserRecoverableAuthException if the user has not yet
	// authorize spreadsheet access for this app, in which case the caller should
	// handle it and do something.
	private SpreadsheetService setupSpreadsheetServiceInThisThread() throws UserRecoverableAuthException {
    	// Turn account name into a token, which must
    	// be done in a background task, as it contacts
    	// the network.
		String token = null;
		try {
			token = GoogleAuthUtil.getToken(
					FullscreenActivity.this, 
					mAccountName, 
					"oauth2:https://spreadsheets.google.com/feeds https://docs.google.com/feeds");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GoogleAuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Now that we have the token, can we actually list
		// the spreadsheets or anything...
		SpreadsheetService s =
				new SpreadsheetService("Megabudget");
		s.setAuthSubToken(token);
		return s;
	}
	
	private SpreadsheetFeed currentSpreadsheetFeedInThisThread() throws UserRecoverableAuthException {
		SpreadsheetService service = setupSpreadsheetServiceInThisThread();
		try {
			// Define the URL to request.  This should never change.
			// (Magic URL good for all users.)
			URL SPREADSHEET_FEED_URL = new URL(
					"https://spreadsheets.google.com/feeds/spreadsheets/private/full");

			// Make a request to the API and get all spreadsheets.
			SpreadsheetFeed feed = null;
			feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
			return feed;
		} catch (MalformedURLException e) {
			// TODO When does this happen? Probably never
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
	
	// Right now this function modifies a spreadsheet called 'foo'
	private void modifySpreadsheet() {
        (new AsyncTask<String, String,String>(){
			@Override
			protected String doInBackground(String... arg0) {
	            try {
	            	// Turn account name into a token, which must
	            	// be done in a background task, as it contacts
	            	// the network.
					String token = 
							GoogleAuthUtil.getToken(
									FullscreenActivity.this, 
									mAccountName, 
									"oauth2:https://spreadsheets.google.com/feeds https://docs.google.com/feeds");
					System.err.println("Token: " + token);
					
					// Now that we have the token, can we actually list
					// the spreadsheets or anything...
					SpreadsheetService s =
							new SpreadsheetService("Megabudget");
					s.setAuthSubToken(token);
					
				    // Define the URL to request.  This should never change.
					// (Magic URL good for all users.)
				    URL SPREADSHEET_FEED_URL = new URL(
				        "https://spreadsheets.google.com/feeds/spreadsheets/private/full");

				    // Make a request to the API and get all spreadsheets.
				    SpreadsheetFeed feed;
					try {
						feed = s.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
					    List<SpreadsheetEntry> spreadsheets = feed.getEntries();

					    // Iterate through all of the spreadsheets returned
					    for (SpreadsheetEntry spreadsheet : spreadsheets) {
					      // Print the title of this spreadsheet to the screen
					      final String name = spreadsheet.getTitle().getPlainText();
					      System.err.println(name);
					      
					      if ("foo".equals(name)) {
					    	  // Get the first work sheet, 
					    	  WorksheetFeed worksheetFeed = s.getFeed(
					    		        spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
					    	  List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
					    	  WorksheetEntry worksheet = worksheets.get(0);
					    	  
					    	  //read cell A1
					    	  URL cellFeedUrl = new URI(worksheet.getCellFeedUrl().toString()
					    			  + "?min-row=1&min-col=1&max-col=1&max-row=1").toURL();
					    	  CellFeed cell = s.getFeed(cellFeedUrl, CellFeed.class);
					    	  System.err.println("Cell " + cell.getTitle().getPlainText()
					    			  + " value is " + cell.getEntries().get(0).getCell().getInputValue());
					    	  
					    	  // write the time to cell A2.
					    	  // (if this cell doesn't already have some data in it,
					    	  // we get an indexoutofboundsexception).
					    	  URL a2CellFeedUrl = new URI(worksheet.getCellFeedUrl().toString()
					    			  + "?min-row=2&min-col=1&max-col=1&max-row=2").toURL();
					    	  CellFeed a2Cell = s.getFeed(a2CellFeedUrl, CellFeed.class);
					    	  Time now = new Time();
					    	  now.setToNow();
					    	  final String a2_value = now.format2445();
					    	  System.err.println("Setting cell " + a2Cell.getTitle().getPlainText()
					    			  + " value to " + a2_value);
					    	  a2Cell.getEntries().get(0).changeInputValueLocal(a2_value);
					    	  a2Cell.getEntries().get(0).update();
					      }
					    }
					} catch (ServiceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (UserRecoverableAuthException e) {
					// This is NECESSARY so the user can say, "yeah I want
					// this app to have permission to read my spreadsheet."
					Intent recoveryIntent = e.getIntent();
					startActivityForResult(recoveryIntent, 2);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (GoogleAuthException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}}).execute();

	}
	
    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
