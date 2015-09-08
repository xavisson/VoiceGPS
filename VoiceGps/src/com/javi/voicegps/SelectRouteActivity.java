package com.javi.voicegps;

import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
 
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;


public class SelectRouteActivity extends Activity {
	

	Button btnGo, btnBack;
	TextView textTo;
	EditText origin, destination;
	
    LatLng latLng;
	
	protected void onCreate(Bundle savedInstanceState){
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_route);
		
		
		btnGo = (Button)findViewById(R.id.buttonGo);
		btnBack = (Button) findViewById(R.id.buttonBack);
		
		// Getting reference to EditText to get the user input location
        destination = (EditText) findViewById(R.id.fillDestination);
        origin = (EditText) findViewById(R.id.fillOrigin);
		
		btnGo.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
                //LatLng startPoint = new LatLng(mLatitude, mLongitude);
                //Location targetPoint = new Location ("location");
                //LatLng targetPoint = new LatLng(targetLat, targetLon);
				//Intent openNavigation = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:ll=" + mLatitude + "," + mLongitude + "&mode=d"));
				//startActivity(openNavigation);
				//Intent openNavigation = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=START_LAT,START_LON&daddr=END_LAT,END_LON"));
				//startActivity(openNavigation);
 
                               
                Intent go = new Intent(SelectRouteActivity.this, MainActivity.class);
				startActivity(go);
				go.putExtra("destino", destination.getText().toString());
				go.putExtra("origen", origin.getText().toString());
				
 
                
				
			}
			
		});
		
		btnBack.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Intent goBack = new Intent(SelectRouteActivity.this, MainActivity.class);
				startActivity(goBack);
				
			}
			
	
		});
	}
}
