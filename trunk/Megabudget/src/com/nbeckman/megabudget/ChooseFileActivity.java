package com.nbeckman.megabudget;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

// This class shows users a list of available files, and then
// forces them to choose the default file that will be edited
// from that point forward.
public class ChooseFileActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Show res/layout/file_chooser.xml
		this.setContentView(R.layout.file_chooser);
		
		// Get file names from input.
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    String[] file_names = extras.getStringArray("spreadsheet_files");
		    System.err.println("file_names size is: " + file_names.length);
		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
		            android.R.layout.simple_list_item_1, file_names);
			ListView list_view = 
					(ListView)this.findViewById(R.id.file_chooser_list_view);
			list_view.setAdapter(adapter);
		}	
	}
	
}
