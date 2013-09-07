package com.nbeckman.megabudget;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

// The AccountManager is where we store all the code relating to accounts.
// The AccountManager is responsible for many things:
// - storing & returning the current account.
// - forcing the selection of a new account.
// - checking to see if the current account has spreadsheet
//   permissions. 
public class AccountManager extends Activity {

	// The name by which clients can retrieve the authorized users if they call this
	// activity with an intent.
	public static final String kResultDataName = "result";

	private static final String kForceAccountPickerName = "force_account_picker";
	
	// The preference name for the stored account, picked initially and used forevermore
	// after that.
	public static final String kAccountPreferencesName = "ACCOUNT_PREFERENCES_NAME";

	// The ID of the result coming back from Docs giving us permission to use spreadsheets.
	private static final int kDocsPermissionResult = 0;
	// The ID of the result coming back from the account picker, telling us what account was chosen.
	private static final int kAccountChoiceIntent = 1;
	
	// Returns true if there is currently a set account.
	public static boolean hasStoredAccount(Context context) {
		final SharedPreferences shared_pref = 
				PreferenceManager.getDefaultSharedPreferences(context);
		return shared_pref.contains(kAccountPreferencesName);	
	}
	
	// Gets the current account in use as is stored in the shared preferences 
	// manager. Because the account may not have been set yet, this method
	// can return an empty string.
	public static String getStoredAccount(Context context) {
		final SharedPreferences shared_pref = 
				PreferenceManager.getDefaultSharedPreferences(context);
		return shared_pref.getString(kAccountPreferencesName, "");		
	}

	// Stores the given account to the shared preferences store.
	public static void storeAccount(Context context, String account) {
		final SharedPreferences shared_pref = 
				PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = shared_pref.edit();
		editor.putString(kAccountPreferencesName, account);
		editor.commit();
	}
	
	// New intent; newReturnAuthorizedUserIntent
	// Creates an intent that can be launched for result.
	// This intent will do the minimum possible work necessary to return
	// an authorized user. If there is a stored account, it will make sure
	// it is authorized and then return it. If there is no stored account,
	// it will first launch the application picker, and store the chosen
	// account.
	public static Intent newReturnAuthorizedUserIntent(Context context) {
		Intent intent = new Intent(context, AccountManager.class);
		intent.putExtra(kForceAccountPickerName, false);
		return intent;
	}

	// This method basically works just like newReturnAuthorizedUserIntent()
	// except that it forces an account selection, even if we have previously
	// got an account working. The most obvious example of when this is useful
	// is when the user wants to change their account from the preferences menue.
	public static Intent newForceAccountPickerIntent(Context context) {
		Intent intent = new Intent(context, AccountManager.class);
		intent.putExtra(kForceAccountPickerName, true);
		return intent;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO(nbeckman): Is there a better callback to handle this?
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			final boolean force_account_picker = 
					extras.getBoolean(kForceAccountPickerName);
			if (force_account_picker) {
				handleForceAccountPicker();
			} else {
				handleReturnAuthorizedUser();
			}
		} else {
			System.err.println("ActivityManager being called without data.");
			// For the moment, it seems that I cannot structure the intent from
			// the preferences menu the way I want, so in this case we just force
			// the account picker, assuming the intent came from the preferences menu.
			handleForceAccountPicker();
		}
	}

	// A handler for the force account picker intent.
	private void handleForceAccountPicker() {
		// To force an account picker, we basically just create a new
		// account picker intent, and ignore whatever the current account
		// value is (it will be overwritten).
		final Intent intent = 
    			AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
    					true, null, null, null, null);
    	startActivityForResult(intent, kAccountChoiceIntent);
	}

	// Handler for the intent to return the current authorized user or ask which
	// account should be used (but not to force asking again).
	private void handleReturnAuthorizedUser() {
		if (hasStoredAccount(this)) {
			final String account = getStoredAccount(this);
			forceDocsPermissionCheck(account);
		} else {
			// We need to force asking for a new account.
			handleForceAccountPicker();
		}
	}

	// Forces a Docs permission check by attempting to load
	// a spreadsheet feed for the given user.
	private void forceDocsPermissionCheck(final String account) {
    	(new AsyncTask<String, String,String>(){
    		@Override
    		protected String doInBackground(String... arg0) {
    			try {
    				// This call forces us to have permission.
    				SpreadsheetUtils.setupSpreadsheetServiceInThisThread(
    						AccountManager.this, account);
        			// At this point we know we have permission, so we can just
        			// return and everything is fine.
        			AccountManager.this.finishWithAccount();
    			} catch (UserRecoverableAuthException e) {
    				// This is NECESSARY so the user can say, "yeah I want
    				// this app to have permission to read my spreadsheet."
    				Intent get_docs_permission_intent = e.getIntent();
    				AccountManager.this.startActivityForResult(
    						get_docs_permission_intent, kDocsPermissionResult);
    			} catch (GoogleAuthException e) {
    				// There are _other_ types of authorization exceptions that
    				// we may not be able to do anything about!
    				e.printStackTrace();
    			}
    			return "TODO Can't I use a different type?";
    		}}).execute();
	}

	// Finishes this intent, putting the authorized account name in the
	// result.
	private void finishWithAccount() {
		final String account = getStoredAccount(this);
		Intent returnIntent = new Intent();
    	returnIntent.putExtra(kResultDataName, account);
    	setResult(RESULT_OK, returnIntent);     
    	finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == kDocsPermissionResult && resultCode == RESULT_OK) {
			// Looks like finally, after all this work, everything is good.
			// Respond to our original caller with the authorized user.
			finishWithAccount();
		} else if (requestCode == kAccountChoiceIntent && resultCode == RESULT_OK) {
			// Now we have an account name! Great, let's save it. 
			final String account = data.getStringExtra(
					android.accounts.AccountManager.KEY_ACCOUNT_NAME);
			storeAccount(this, account);
			// And continue by checking to see if the account has permission to use docs.
			forceDocsPermissionCheck(account);
		} else if (resultCode != RESULT_OK) {
			// This is the sad end. Return a sad end to the person who called us.
			setResult(resultCode);
			finish();
		}
	}
}
