package com.nbeckman.megabudget;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

// A wrapper for the settings fragment that actually contains the settings.
// Not sure why fragments are good actually...
public class MegaBudgetSettingsActivity extends Activity {

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
	public static class MegaBudgetSettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.megabudget_preferences);
		}
	}

}