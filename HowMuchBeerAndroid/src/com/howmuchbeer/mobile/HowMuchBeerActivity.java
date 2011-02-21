package com.howmuchbeer.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class HowMuchBeerActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        // Populate spinners, AKA pull-down menus 
        Spinner craziness_spinner = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> craziness_adapter = ArrayAdapter.createFromResource(
                this, R.array.craziness_values, android.R.layout.simple_spinner_item);
        craziness_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        craziness_spinner.setAdapter(craziness_adapter);
        
        Spinner safe_spinner = (Spinner) findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> safe_adapter = ArrayAdapter.createFromResource(
                this, R.array.safe_values, android.R.layout.simple_spinner_item);
        safe_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        safe_spinner.setAdapter(safe_adapter);        
    }
}