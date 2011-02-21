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
import android.widget.TextView;

/**
 * Listener for the main, "How Much Beer?" button. Does the requesting
 * work, for now in the GUI thread. :-(
 * @author nbeckman
 *
 */
public final class LaunchButtonListener implements OnClickListener {

	private final Activity mainActivity;
	
	public LaunchButtonListener(HowMuchBeerActivity howMuchBeerActivity) {
		this.mainActivity = howMuchBeerActivity;
	}

	@Override
	public void onClick(View arg0) {
		TextView text_view = (TextView)mainActivity.findViewById(R.id.textView4);
		text_view.setText("Loading...");
		try {
			// Make GET request
			String url = "http://www.howmuchbeer.com/result.jsp?attendees=4&craziness=CHILL";
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
