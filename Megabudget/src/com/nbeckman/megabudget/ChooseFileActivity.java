package com.nbeckman.megabudget;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;

// This class shows users a list of available files, and then
// forces them to choose the default file that will be edited
// from that point forward.
// TODO(nbeckman): Change name to FileManager?
public class ChooseFileActivity extends Activity
		implements LoaderManager.LoaderCallbacks<List<SpreadsheetEntry>> {
	
	private final static int kSpreadsheetFileLoaderId = 1;
	
	// The preference name for the stored spreadsheet, picked initially and used forevermore
	// after that.
	public static final String kBudgetShreadsheetPreferencesName = "BUDGET_SPREADSHEET_PREFERENCES_NAME";
	
	// The preference name for the spreadsheet URL. Basically  pair with the above preference, except
	// that one is for display, and this one is for actually changing/reading the spreadsheet.
	public static final String kBudgetShreadsheetURLPreferencesName = "BUDGET_SPREADSHEET_URL_PREFERENCES_NAME";
	
	// Returns true if there is currently a set spreadsheet.
	public static boolean hasStoredSpreadsheet(Context context) {
		final SharedPreferences shared_pref = 
				PreferenceManager.getDefaultSharedPreferences(context);
		return shared_pref.contains(kBudgetShreadsheetPreferencesName);	
	}
	
	// Gets the current spreadsheet in use as is stored in the shared preferences 
	// manager. Because the spreadsheet may not have been set yet, this method
	// can return an empty string.
	public static String getStoredSpreadsheet(Context context) {
		final SharedPreferences shared_pref = 
				PreferenceManager.getDefaultSharedPreferences(context);
		return shared_pref.getString(kBudgetShreadsheetPreferencesName, "");		
	}

	// Gets the current spreadsheet URL in use as is stored in the shared preferences 
	// manager. Because the spreadsheet URL may not have been set yet, this method
	// can return an empty string.
	public static String getStoredSpreadsheetURL(Context context) {
		final SharedPreferences shared_pref = 
				PreferenceManager.getDefaultSharedPreferences(context);
		return shared_pref.getString(kBudgetShreadsheetURLPreferencesName, "");		
	}
	
	// Stores the given spreadsheet to the shared preferences store.
	public static void storeSpreadsheet(Context context, String spreadsheet_name) {
		final SharedPreferences shared_pref = 
				PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = shared_pref.edit();
		editor.putString(kBudgetShreadsheetPreferencesName, spreadsheet_name);
		editor.commit();
	}
	
	// Stores the given spreadsheet URL to the shared preferences store.
	public static void storeSpreadsheetURL(Context context, String spreadsheet_url) {
		final SharedPreferences shared_pref = 
				PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = shared_pref.edit();
		editor.putString(kBudgetShreadsheetURLPreferencesName, spreadsheet_url);
		editor.commit();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Show res/layout/file_chooser.xml
		this.setContentView(R.layout.file_chooser);
		
		// Set empty adapter for list view to start with.
		ListView list_view = 
				(ListView)this.findViewById(R.id.file_chooser_list_view);
		list_view.setAdapter(
				new SpreadsheetFilesAdapter(this, new ArrayList<SpreadsheetEntry>()));
		list_view.setOnItemClickListener(mListClickedHandler);
		
		// Set up the loader. The loader is responsible for asynchronously finding out
		// the names of all the spreadsheets and then populating the list with the
		// new adapter.
		getLoaderManager().initLoader(kSpreadsheetFileLoaderId, null, this);
	}
	
	// Handler for when the user clicks on a file name.
	private OnItemClickListener mListClickedHandler = new OnItemClickListener() {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	    	Intent returnIntent = new Intent();
	    	final Object item = parent.getItemAtPosition(position);
	    	if (item instanceof SpreadsheetEntry) {
	    		final SpreadsheetEntry spreadsheet = (SpreadsheetEntry)item;
		    	// Return the file name to the calling activity.
		    	// Store filename & worksheet feed URL
	    		storeSpreadsheet(
	    				ChooseFileActivity.this, spreadsheet.getTitle().getPlainText());
	    		storeSpreadsheetURL(
	    				ChooseFileActivity.this, spreadsheet.getWorksheetFeedUrl().toString());
	    		System.err.println(spreadsheet.getWorksheetFeedUrl().toString());
		    	returnIntent.putExtra("result", spreadsheet.getWorksheetFeedUrl().toString());
		    	setResult(RESULT_OK, returnIntent);   
	    	} else {
	    		setResult(RESULT_CANCELED, returnIntent);   
	    	}
	    	finish();
	    }
	};

	@Override
	public Loader<List<SpreadsheetEntry>> onCreateLoader(int arg0, Bundle arg1) {
		return new SpreadsheetFilesLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<List<SpreadsheetEntry>> loader,
			List<SpreadsheetEntry> spreadsheets) {
		ListView list_view = 
				(ListView)this.findViewById(R.id.file_chooser_list_view);
		list_view.setAdapter(
				new SpreadsheetFilesAdapter(this, spreadsheets));
	}

	@Override
	public void onLoaderReset(Loader<List<SpreadsheetEntry>> arg0) {
		ListView list_view = 
				(ListView)this.findViewById(R.id.file_chooser_list_view);
		list_view.setAdapter(
				new SpreadsheetFilesAdapter(this, new ArrayList<SpreadsheetEntry>()));
	}
}

class SpreadsheetFilesLoader extends AsyncTaskLoader<List<SpreadsheetEntry>> {
	
	public SpreadsheetFilesLoader(Context context) {
		super(context);
	}

	@Override
	public List<SpreadsheetEntry> loadInBackground() {
		// Do the actual hard thing! Load the spreadsheets. 
		if (!AccountManager.hasStoredAccount(getContext())) {
			// TODO(nbeckman): What happens here if we don't have an account yet?
			// Any good way to force one?
			return new ArrayList<SpreadsheetEntry>(0);
		}
		final String account = AccountManager.getStoredAccount(getContext());
		SpreadsheetFeed feed = null;
		try { 
			feed = SpreadsheetUtils.currentSpreadsheetFeedInThisThread(getContext(), account);
		} catch (GoogleAuthException e) {
			// TODO(nbeckman): Fast & loose here; can't we prevent this from happening?
			e.printStackTrace();
		}
		if (feed == null) {
			return new ArrayList<SpreadsheetEntry>(0);
		}
		return feed.getEntries();
	}

	@Override
	protected void onStartLoading() {
		// My understanding of this method is that it is called in
		// response to startLoading(), which is called by the client
		// probably after construction by the framework. forceLoad()
		// then causes loadInBackground() to ultimately be called,
		// delivering the results. I believe we should be caching these
		// hard-earned spreadsheet files, but we won't until it becomes
		// a problem. See these links for more:
		// http://developer.android.com/reference/android/content/AsyncTaskLoader.html
		// and
		// http://stackoverflow.com/questions/17489145/populating-a-listview-using-asynctaskloader
		forceLoad();
	}
	
}

// It basically knows how to take a spreadsheet and turn it into an indexable
// thing that the list view can display. 
class SpreadsheetFilesAdapter extends BaseAdapter {

	// Used to inflate view from XML.
	private final LayoutInflater inflater_;
	// Spreadsheets given at entry.
	private final List<SpreadsheetEntry> spreadsheets_;
	
	public SpreadsheetFilesAdapter(Context context, List<SpreadsheetEntry> spreadsheets_) {
		this.spreadsheets_ = spreadsheets_;
		this.inflater_ = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return spreadsheets_.size();
	}

	@Override
	public SpreadsheetEntry getItem(int i) {
		return spreadsheets_.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup group) {
		LinearLayout rowView;
		if (view == null) {
			rowView = (LinearLayout)inflater_.inflate(R.layout.choose_file_row, null);
		} else {
			rowView = (LinearLayout)view;
		}
		
		// Add the details of the spreadsheet to the row for display.
		SpreadsheetEntry entry = getItem(i);
		TextView label = (TextView)rowView.findViewById(R.id.label);
		label.setText(entry.getTitle().getPlainText());
		TextView details = (TextView)rowView.findViewById(R.id.details);
		details.setText(entry.getUpdated().toUiString());
		return rowView;
	}
}
