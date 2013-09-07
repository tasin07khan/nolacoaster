package com.nbeckman.megabudget;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

// A wrapper for the settings fragment that actually contains the settings.
// Not sure why fragments are good actually...
public class MegaBudgetSettingsActivity extends Activity {

	// The name of the spreadsheet, picked initially and used forevermore.
	// TODO(nbeckman): Allow this to be changed.
	// TODO(nbeckman): Wouldn't we rather store a doc ID or something? Something we are
	//                 not afraid of collisions?
	public static final String kBudgetSpreadsheetPreferencesName = "BUDGET_SPREADSHEET_PREFERENCES_NAME";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MegaBudgetSettingsFragment())
                .commit();
    }
	
	// This 'fragment' is a tiny view that can be displayed by a full
	// activity. This one lets the user choose preferences.
	public static class MegaBudgetSettingsFragment extends PreferenceFragment 
												   implements OnSharedPreferenceChangeListener {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.megabudget_preferences);
			
	        // Set summaries to their values in code.
			final String account = AccountManager.getStoredAccount(getActivity());
			// TODO This is returning null...
			final Preference account_pref = findPreference(AccountManager.kAccountPreferencesName);
			account_pref.setSummary(account);

			final SharedPreferences shared_pref = 
					PreferenceManager.getDefaultSharedPreferences(getActivity());
	        final String spreadsheet = 
	        		shared_pref.getString(kBudgetSpreadsheetPreferencesName, "");
			final Preference accountPref = 
					findPreference(kBudgetSpreadsheetPreferencesName);
	        accountPref.setSummary(spreadsheet);
	        
	        // This used to be in onResume(), but I found it was unregistering when I went to
	        // other activities to change the value.
	        shared_pref.registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			// When a preference changes, load its new value as the summary. 
			SharedPreferences shared_pref = 
					PreferenceManager.getDefaultSharedPreferences(getActivity());
			final String value = shared_pref.getString(key, "");
			final Preference pref = findPreference(key);
			pref.setSummary(value);
		}
		
		@Override
		public void onResume() {
		    super.onResume();
		}

		@Override
		public void onPause() {
		    super.onPause();
		    // TODO(nbeckman): All of the examples says to register/unregister as
		    // a preference change listener in onResume/onPause, but if I do that
		    // I never get the callback when the preference is changed in the
		    // AccountManager activity. So instead, I moved it to onCreate, and life
		    // is good.
		    // Try to put it back here if we have leaks and stuff.
		}
	}

}