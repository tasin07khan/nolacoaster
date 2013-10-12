package com.nbeckman.megabudget;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;
import com.nbeckman.megabudget.adapters.BudgetAdapter;
import com.nbeckman.megabudget.adapters.BudgetMonth;
import com.nbeckman.megabudget.adapters.DefaultBudgetAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

// My second shot at a 'main' activity for the the megabudget app.
// The original one had all this cool junk for making the app
// fullscreen which made it hard for me to add new buttons and stuff.
public class MainBudgetActivity extends Activity {
	
	private static final int kMonthSpinnerID = 1;
	
	private MonthLoaderManager month_loader_manager_ = null;
	
	private BudgetAdapter budget_adapter_ = null;
	private SpreadsheetService spreadsheet_service_ = null;
	private WorksheetFeed worksheet_feed_ = null;
	private List<BudgetMonth> months_ = new ArrayList<BudgetMonth>(0);
	private CellEntry month_total_cell_ = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_budget);

        // Make sure we are logged in, and have a spreadsheet chosen.
        // TODO: Just want to try months, this will be hacked up.
        // Like, do I really want this asyn task here?
        (new AsyncTask<String, String, Boolean>(){
        	@Override
        	protected Boolean doInBackground(String... arg0) {
        		final String account = AccountManager.getStoredAccount(MainBudgetActivity.this);
    			// Can't be called in UI thread.
    			try {
    				spreadsheet_service_ = 
    						SpreadsheetUtils.setupSpreadsheetServiceInThisThread(MainBudgetActivity.this, account);
        			final String spreadsheet_url = ChooseFileActivity.getStoredSpreadsheetURL(MainBudgetActivity.this);
        			// Must be done in background thread.
        			worksheet_feed_ = spreadsheet_service_.getFeed(
        					new URL(spreadsheet_url), WorksheetFeed.class);
            		List<WorksheetEntry> worksheets = worksheet_feed_.getEntries();
            		WorksheetEntry worksheet = worksheets.get(0);
            		budget_adapter_ = new DefaultBudgetAdapter(worksheet, spreadsheet_service_);
            		
            		months_ = budget_adapter_.getMonths();
            		if (months_.size() > 0) {
            			// Initially, the total of the first month is displayed.
            			// If another month is chosen, we can display that total.
                		month_total_cell_ = budget_adapter_.getMonthTotalCell(months_.get(0));
            		}
        			return true;
				} catch (GoogleAuthException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			return false;
        	}
        	
        	@Override
        	protected void onPostExecute(Boolean success) {
        		if (!success) {
        			return;
        		}
        		
        		// END PART I KNOW IS HACKED UP

        		// Set up the loader manager to load months into the months spinner.
        		final Spinner month_spinner = (Spinner)findViewById(R.id.month_spinner);
        		ArrayAdapter<BudgetMonth> month_spinner_adapter = 
        				new ArrayAdapter<BudgetMonth>(MainBudgetActivity.this, android.R.layout.simple_list_item_1);
        		// This must be done in the foreground thread.
        		month_spinner.setAdapter(month_spinner_adapter);
        		month_loader_manager_  = 
        				new MonthLoaderManager(MainBudgetActivity.this, budget_adapter_, month_spinner_adapter);
        		getLoaderManager().initLoader(kMonthSpinnerID, null, month_loader_manager_);
        		
        		// Write total to total text box.
        		// TODO(nbeckman): I need a better way of storing, what is the current month..
        		if (month_total_cell_ != null) {
        			// Initially, the total of the first month is displayed.
        			// If another month is chosen, we can display that total.
            		final String month_total = month_total_cell_.getCell().getValue();
            		final TextView total_textbox = (TextView)findViewById(R.id.monthTotalDisplay);
            		total_textbox.setText(month_total);
        		}
        	}}).execute();
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
		Intent intent = new Intent();
	    intent.setClass(this, MegaBudgetSettingsActivity.class);
	    startActivity(intent);
		return true;
	}
}
