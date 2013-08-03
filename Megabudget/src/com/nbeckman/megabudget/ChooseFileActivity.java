package com.nbeckman.megabudget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

// This class shows users a list of available files, and then
// forces them to choose the default file that will be edited
// from that point forward.
public class ChooseFileActivity extends Activity {

	private String[] mSpreadsheetFileNames = new String[0];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Show res/layout/file_chooser.xml
		this.setContentView(R.layout.file_chooser);
		
		// Get file names from input.
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mSpreadsheetFileNames = extras.getStringArray("spreadsheet_files");
		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
		            android.R.layout.simple_list_item_1, mSpreadsheetFileNames);
			ListView list_view = 
					(ListView)this.findViewById(R.id.file_chooser_list_view);
			list_view.setAdapter(adapter);
			list_view.setOnItemClickListener(mListClickedHandler); 
		}	
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
}
