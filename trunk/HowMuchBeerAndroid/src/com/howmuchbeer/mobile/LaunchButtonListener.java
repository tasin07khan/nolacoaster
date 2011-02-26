package com.howmuchbeer.mobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Listener for the main, "How Much Beer?" button. Does the requesting
 * work, for now in the GUI thread. :-(
 * @author nbeckman
 *
 */
public final class LaunchButtonListener implements OnClickListener {

	private final Activity mainActivity;
	
	private final ArrayAdapter<CharSequence> safe_adapter; 
	
	class ResponseThread extends Thread {
		private final String url;
		private final TextView view;
		
		ResponseThread(String url, TextView view) {
			this.url = url;
			this.view = view;
		}

		@Override
		public void run() {
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet getMethod = new HttpGet((url));
				HttpResponse httpResponse = client.execute(getMethod);
				HttpEntity httpEntity = httpResponse.getEntity();

				BufferedReader reader = new BufferedReader(new InputStreamReader(httpEntity.getContent()));

				String line;
				final StringBuffer contents = new StringBuffer("Results:\n");
				while((line=reader.readLine()) != null) {
					// Write to text box
					contents.append(line);
					contents.append("\n");
				}
				
				mainActivity.runOnUiThread(new Runnable(){
					@Override
					public void run() { view.setText(contents.toString()); }
				});
				
			} catch (ClientProtocolException e) {
				mainActivity.runOnUiThread(new Runnable(){
					@Override
					public void run() { view.setText("Could not contact remote host. Check network connection and try again."); }
				});
			} catch (IOException e) {
				mainActivity.runOnUiThread(new Runnable(){
					@Override
					public void run() { view.setText("Could not contact remote host. Check network connection and try again."); }
				});
			} finally {
				mainActivity.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						// Enable button...
						Button button = (Button)mainActivity.findViewById(R.id.button1);
						button.setEnabled(true);
					}
				});
			}
		}
		
		
	}
	
	public LaunchButtonListener(HowMuchBeerActivity howMuchBeerActivity) {
		this.mainActivity = howMuchBeerActivity;
		safe_adapter = ArrayAdapter.createFromResource(
	            howMuchBeerActivity, R.array.safe_values, android.R.layout.simple_spinner_item);
	}
	
	private String correctSafetyName(String s) {
		if(this.safe_adapter.getItem(0).toString().equals(s)) {
			return "NORMAL";
		} else if(this.safe_adapter.getItem(1).toString().equals(s)) {
			return "BELOW";
		} else if(this.safe_adapter.getItem(2).toString().equals(s)) {
			return "ABOVE";
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void onClick(View arg0) {
		TextView text_view = (TextView)mainActivity.findViewById(R.id.textView4);
		EditText attendees_view = (EditText)mainActivity.findViewById(R.id.editText1);
		Spinner craziness_spinner = (Spinner)mainActivity.findViewById(R.id.spinner1);
		Spinner safety_spinner = (Spinner)mainActivity.findViewById(R.id.spinner2);
		
		// Validate input
		if( !verifyAttendeesAndDisplay(attendees_view.getText().toString()) ) {
			return;
		}
		
		// Disable button...
		Button button = (Button)mainActivity.findViewById(R.id.button1);
		button.setEnabled(false);
		
		// Hide keyboard
		InputMethodManager inputManager = (InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(attendees_view.getWindowToken(), 0);
		
		text_view.setText("Loading...");
		// Make GET request
		String url = "http://www.howmuchbeer.com/mobile?craziness=";
		url += craziness_spinner.getSelectedItem().toString();
		url += "&attendees=";
		url += attendees_view.getText().toString();
		url += "&safe=";
		url += correctSafetyName(safety_spinner.getSelectedItem().toString());
		// http://localhost:8888/mobile?craziness=chill&attendees=4&safe=normal

		(new ResponseThread(url, text_view)).start();		
	}
	
	/**
	 * Verify the attendees field. Display a toast warning if it is invalid. Return false if same.
	 * @param attendees
	 * @return
	 */
	private boolean verifyAttendeesAndDisplay(String attendees) {
		Context context = mainActivity.getApplicationContext();
		int duration = Toast.LENGTH_SHORT;

		Long attendees_;
		try {
			attendees_ = Long.parseLong(attendees);
		} catch(NumberFormatException nfe) {
			// Attendees is not a number
			Toast toast = Toast.makeText(context, "Attendees must be a number.", duration);
			toast.show();
			return false;
		}
		
		if( attendees_ <= 0 ) {
			Toast toast = Toast.makeText(context, "Attendees must be greater than zero.", duration);
			toast.show();
			return false;
		}

		return true;
	}

}
