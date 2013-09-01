package com.nbeckman.megabudget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

// My second shot at a 'main' activity for the the megabudget app.
// The original one had all this cool junk for making the app
// fullscreen which made it hard for me to add new buttons and stuff.
public class MainBudgetActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_budget);
        
        
        
        // Display the preferences fragment. TODO TERRIRBLE THIS JUST GOES OVER ABOVE
//        getFragmentManager().beginTransaction()
//                .replace(android.R.id.content, new MegaBudgetSettingsFragment())
//                .commit();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// Just the code to make my list of menu items come up.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.megabudget_menu, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Someone clicked on _something_ in the menu, and since
		// we have only one item, we assume it was the settings
		// menu. Launch it... 
		// TODO(nbeckman): And let is set its own results?
		Intent intent = new Intent();
	    intent.setClass(this, MegaBudgetSettingsActivity.class);
	    startActivity(intent);
		return true;
	}
}
