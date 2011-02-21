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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Listener for the main, "How Much Beer?" button. Does the requesting
 * work, for now in the GUI thread. :-(
 * @author nbeckman
 *
 */
public final class LaunchButtonListener implements OnClickListener {

	private final Activity mainActivity;
	
	private final ArrayAdapter<CharSequence> safe_adapter; 
	
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
		
		text_view.setText("Loading...");
		try {
			// Make GET request
			String url = "http://www.howmuchbeer.com/mobile?craziness=";
			url += craziness_spinner.getSelectedItem().toString();
			url += "&attendees=";
			url += attendees_view.getText().toString();
			url += "&safe=";
			url += correctSafetyName(safety_spinner.getSelectedItem().toString());
			// http://localhost:8888/mobile?craziness=chill&attendees=4&safe=normal
			
			HttpClient client = new DefaultHttpClient();
			HttpGet getMethod = new HttpGet((url));
			HttpResponse httpResponse = client.execute(getMethod);
			HttpEntity httpEntity = httpResponse.getEntity();

			BufferedReader reader = new BufferedReader(new InputStreamReader(httpEntity.getContent()));

			String line;
			StringBuffer contents = new StringBuffer();
			while((line=reader.readLine()) != null) {
				// Write to text box
				contents.append(line);
				contents.append("\n");
			}
			text_view.setText(contents.toString());

		} catch (ClientProtocolException e) {
			text_view.setText("Error1");
			e.printStackTrace();
		} catch (IOException e) {
			text_view.setText("Error2");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
