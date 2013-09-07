package com.nbeckman.megabudget;

import java.util.ArrayList;
import java.util.List;

import com.google.gdata.data.spreadsheet.SpreadsheetEntry;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

// This class shows users a list of available files, and then
// forces them to choose the default file that will be edited
// from that point forward.
public class ChooseFileActivity extends Activity
		implements LoaderManager.LoaderCallbacks<List<SpreadsheetEntry>> {

	private String[] mSpreadsheetFileNames = new String[0];
	
	private final static int kSpreadsheetFileLoaderId = 1;
	
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
	    	// Return the file name to the calling activity.
	    	Intent returnIntent = new Intent();
	    	returnIntent.putExtra("result", mSpreadsheetFileNames[position]);
	    	setResult(RESULT_OK, returnIntent);     
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
		// TODO Auto-generated method stub
		return null;
	}
	
	// In examples, a number of other classes are overloaded, e.g.,
	// http://developer.android.com/reference/android/content/AsyncTaskLoader.html
	// and
	// http://stackoverflow.com/questions/17489145/populating-a-listview-using-asynctaskloader
	// but I will wait to see if they are needed.
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
