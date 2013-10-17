package com.nbeckman.megabudget;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;
import com.nbeckman.megabudget.adapters.BudgetAdapter;
import com.nbeckman.megabudget.adapters.BudgetCategory;
import com.nbeckman.megabudget.adapters.BudgetMonth;
import com.nbeckman.megabudget.adapters.defaultadapter.DefaultBudgetAdapter;

// My second shot at a 'main' activity for the the megabudget app.
// The original one had all this cool junk for making the app
// fullscreen which made it hard for me to add new buttons and stuff.
public class MainBudgetActivity extends Activity {
	
	private static final int kMonthSpinnerID = 1;
	private static final int kCategorySpinnerID = 2;
	
	// TODO(nbeckman): Not sure we even need to save these.
	private MonthLoaderManager month_loader_manager_ = null;
	private CategoryLoaderManager category_loader_manager_ = null;
	
	private BudgetAdapter budget_adapter_ = null;
	private SpreadsheetService spreadsheet_service_ = null;
	private WorksheetFeed worksheet_feed_ = null;
	private List<BudgetMonth> months_ = new ArrayList<BudgetMonth>(0);
	
	private CellEntry month_total_cell_ = null;
	
	// Currently selected cells.
	private BudgetMonth selected_month_cell_ = null;
	private BudgetCategory selected_category_cell_ = null;
	
	// The listener for the 'add expense' button.
	// TODO(nbeckman): Probably make this its own class.
	private OnClickListener add_expense_button_listener_ = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// If there's no selected category/month, we do nothing.
			if (selected_category_cell_ == null || selected_month_cell_ == null ||
					budget_adapter_ == null) {
				return;
			}
			final TextView expense_textbox = 
					(TextView)findViewById(R.id.expense_amount_textbox);
			double expense_amount = 0.0;
			try {
				expense_amount = 
						Double.parseDouble(expense_textbox.getText().toString());
			} catch(NumberFormatException e) {
				return;
			}
			if (0.0 == expense_amount) {
				return;
			}
			// Text box cannot be edited while we are changing the value.
			expense_textbox.setEnabled(false);
			
			// Another progress dialog...
	        final ProgressDialog progress_dialog = new ProgressDialog(MainBudgetActivity.this);
	        // TODO(nbeckman): Pull out these strings.
	        progress_dialog.setTitle("Updating " + selected_category_cell_.getName());
	        progress_dialog.setMessage("Wait while updating spreadsheet...");
	        progress_dialog.show();
			
			final double final_expense_amount = expense_amount;
			(new AsyncTask<String, String, String>(){
				@Override
				protected String doInBackground(String... params) {
					budget_adapter_.AddValue(
							selected_month_cell_, selected_category_cell_, final_expense_amount);
					return "";
				}
				@Override
				protected void onPostExecute(String result) {
					// Renable text box again.
					progress_dialog.dismiss();
					expense_textbox.setEnabled(true);
					expense_textbox.setText("");
					expense_textbox.setHint(R.string.expense_amount_hint);
				}
			}).execute();
		}
	};
	
	private OnItemSelectedListener category_selected_listener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			final Object item = arg0.getItemAtPosition(arg2);
			if (item instanceof BudgetCategory) {
				selected_category_cell_ = (BudgetCategory)item;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			selected_category_cell_ = null;
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_budget);

        final Button add_expense_button = (Button)findViewById(R.id.add_expense_button);
        add_expense_button.setOnClickListener(add_expense_button_listener_);
        final Spinner category_spinner = (Spinner)findViewById(R.id.category_spinner);
        category_spinner.setOnItemSelectedListener(category_selected_listener);
        
        // Show a process dialog since loading the categories and
        // stuff can be slow.
        // TODO(nbeckman): This thing is like completely modal, so do something
        // better...
        // TODO(nbeckman): Pull out these strings.
        final ProgressDialog progress_dialog = new ProgressDialog(this);
        progress_dialog.setTitle("Loading Spreadsheet");
        progress_dialog.setMessage("Wait while loading spreadsheet...");
        progress_dialog.show();
        
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
            			selected_month_cell_ = months_.get(0);
            			// Initially, the total of the first month is displayed.
            			// If another month is chosen, we can display that total.
                		month_total_cell_ = budget_adapter_.getMonthTotalCell(months_.get(0));
            		}
            		List<BudgetCategory> categories = budget_adapter_.getCategories();
            		if (categories.size() > 0) {
            			selected_category_cell_ = categories.get(0);
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
                // Dismiss progress dialog.
                progress_dialog.dismiss();
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
        		
        		// Set up the loader manager to load categories into the category spinner.
        		final Spinner category_spinner = (Spinner)findViewById(R.id.category_spinner);
        		ArrayAdapter<BudgetCategory> category_spinner_adapter = 
        				new ArrayAdapter<BudgetCategory>(MainBudgetActivity.this, android.R.layout.simple_list_item_1);
        		// This must be done in the foreground thread.
        		category_spinner.setAdapter(category_spinner_adapter);
        		category_loader_manager_  = 
        				new CategoryLoaderManager(MainBudgetActivity.this, budget_adapter_, category_spinner_adapter);
        		getLoaderManager().initLoader(kCategorySpinnerID, null, category_loader_manager_);
        		
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
