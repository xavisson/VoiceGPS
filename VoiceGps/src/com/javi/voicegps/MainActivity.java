package com.javi.voicegps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.PolylineOptions;

 



public class MainActivity extends FragmentActivity implements LocationListener, TextToSpeech.OnInitListener{
	
	GoogleMap mGoogleMap;	
	ArrayList<LatLng> mMarkerPoints;
	
	//To select map type on the menu
	private int vista = 0;

	//Array with the available points of interest types
	String[] mPlaceType=null;
	String[] mPlaceTypeName=null;
	
	//Current position
	double mLatitude=0;
	double mLongitude=0;
	
	//Destination position
	double dLatitude=0;
	double dLongitude=0;
	
	//Car position
	double cLat=0;
	double cLon=0;
	
	//Radius is 2km by default
	String radio = "2000";
	//Type of Poin Of Interest
	String type=null;
	//To send info about the places to a new activity
	HashMap<String, String> mMarkerPlaceLink = new HashMap<String, String>();
	//Displays the address over the marker in the map
	MarkerOptions markerOptions;
	//Coordinates
    LatLng latLng;
    LatLng pOrigen = new LatLng(mLatitude, mLongitude);
    LatLng pDestino = new LatLng(dLatitude, dLongitude);
    LatLng cPosition;

    //Displays the time and distance to the destination
    TextView tvDistanceDuration;
    //Codes to identify the vocal query
    private static final int REQUEST_CODE = 1234;
    private static final int REQUEST_CODE2 = 2345;
    private static final int REQUEST_CODE3 = 3333;
    private static final int REQUEST_CODE4 = 4444;
    private static final int REQUEST_CODE5 = 5555;


    
    //variable for checking TTS engine data on user device
    private int MY_DATA_CHECK_CODE = 0;
    //Text To Speech instance
    private TextToSpeech mTts;
    
    private ProgressDialog mProgressDialog;
    
    String escucha22=null;
    
	int iThread=0;
    
    //flags to determine weather a voice message is executed later or not
    int flag_buscar=0;
    int flag_radio=0;
    //indicates if the car location has been saved
    int flag_car=0;
    
    // Creating MarkerOptions for the car
    MarkerOptions car = new MarkerOptions();

     // The serialization (saved instance state) Bundle key representing the
     // current dropdown position.
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    //private int mStatus = 0;
    //private boolean mProcessed = false;
    //private static final String TAG = "SpeechRecognizingAndSpeakingActivity";
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		
		tvDistanceDuration = (TextView) findViewById(R.id.tv_distance_time);
		ImageButton speakButton = (ImageButton) findViewById(R.id.btnSpeak);
		
		// Disable button if no recognition service is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0)
        {
            speakButton.setEnabled(false);
            Toast.makeText(getBaseContext(), "Recognizer no presente", Toast.LENGTH_SHORT).show();
        }
      	//prepare the TTS to repeat chosen words
        Intent checkTTSIntent = new Intent();
        //check TTS data
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        //start the checking Intent - will retrieve result in onActivityResult
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
		// Array of place types
        
        // Instantiating TextToSpeech class
        mTts = new TextToSpeech(this, this);
 
        // Creating a progress dialog window
        mProgressDialog = new ProgressDialog(this);
        /** Setting a message for this progress dialog
         * Use the method setTitle(), for setting a title
         * for the dialog window
         *  */
        mProgressDialog.setMessage("Please wait ...");
        
        //Array of place types for the spinner
		mPlaceType = getResources().getStringArray(R.array.place_type);
		
		// Array of place type names
		mPlaceTypeName = getResources().getStringArray(R.array.place_type_name);
		
		final ActionBar actionBar = getActionBar();
	    actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		// Array of place types
	    //final String[] dropdownValues = getResources().getStringArray(R.array.place_type_name);

	    // Specify a SpinnerAdapter to populate the dropdown list.
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(actionBar.getThemedContext(),
	        android.R.layout.simple_spinner_dropdown_item,
	        mPlaceTypeName);	 
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

	    // Set up the dropdown list navigation in the action bar.
	    actionBar.setListNavigationCallbacks(adapter, new OnNavigationListener() {
	    		 	    	
	        @Override
	    		 public boolean onNavigationItemSelected(int itemPosition, long itemId) {
	        	
		        	int selectedPosition = itemPosition;
					type = mPlaceType[selectedPosition];
					// Query to Places API with the desired parameters
					queryPlacesAPI();
					   		 
		    		return true;
	    		 }
	    });
		
		// Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        
        if(status!=ConnectionResult.SUCCESS){ // Google Play Services are not available

        	int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        }else { // Google Play Services are available
        	
        	// Initializing
            mMarkerPoints = new ArrayList<LatLng>();
        	
	    	// Getting reference to the SupportMapFragment
	    	SupportMapFragment fragment = ( SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
	    			
	    	// Getting Google Map
	    	mGoogleMap = fragment.getMap();
	    			
	    	// Enabling MyLocation in Google Map
	    	mGoogleMap.setMyLocationEnabled(true);
	    	    		    	
	    	// Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location From GPS
            Location location = locationManager.getLastKnownLocation(provider);

            if(location!=null){
                    onLocationChanged(location);
                    mLongitude=location.getLongitude();
                    mLatitude=location.getLatitude();
            }
            // Set the current location every 10 seconds
            locationManager.requestLocationUpdates(provider, 10000, 0, this);
            
            dLongitude = mLongitude;
            dLatitude = mLatitude;
                        
            mGoogleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
				
				@Override
				public void onInfoWindowClick(Marker arg0) {
					Intent intent = new Intent(getBaseContext(), PlaceDetailsActivity.class);
					String reference = mMarkerPlaceLink.get(arg0.getId());
					String posi_enviar = mMarkerPlaceLink.get(arg0.getPosition());
					intent.putExtra("reference", reference);
					intent.putExtra("position", posi_enviar);
					
					// Starting the Place Details Activity
					startActivity(intent);
				}
			});
            
            // Setting onclick event listener for the map
            mGoogleMap.setOnMapClickListener(new OnMapClickListener() {
 
                @Override
                public void onMapClick(LatLng point) {
 
                	//Set the destination coordinates
                	dLatitude = point.latitude;
                    dLongitude = point.longitude;
                    LatLng startPoint = new LatLng(mLatitude, mLongitude);
                    pOrigen = startPoint;
                    pDestino = point;
                	
                	// Already map contain destination location
                    if(mMarkerPoints.size()>1){
 
                        //FragmentManager fm = getSupportFragmentManager();
                        mMarkerPoints.clear();
                        mGoogleMap.clear();
                        // draw the marker at the current position
                        drawMarker(startPoint);
                    }
 
                    // draws the marker at the currently touched location
                    drawMarker(point);
 
                    // Checks, whether start and end locations are captured
                    if(mMarkerPoints.size() >= 2){
                        LatLng origin = mMarkerPoints.get(0);
                        LatLng dest = mMarkerPoints.get(1);
 
                        // Getting URL to the Google Directions API
                        String url = getDirectionsUrl(origin, dest);
 
                        DownloadTask downloadTask = new DownloadTask();
 
                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url);
                    }
                }
            });
            
         // The map will be cleared on long click
            mGoogleMap.setOnMapLongClickListener(new OnMapLongClickListener() {
     
                @Override
                public void onMapLongClick(LatLng point) {
                    
                    clearMap();
                }
            });
            
        }	
        
        initTask();
        
	}
	
	// An AsyncTask class for accessing the GeoCoding Web Service
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>>{
 
        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;
 
            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }
 
        @Override
        protected void onPostExecute(List<Address> addresses) {
 
            if(addresses==null || addresses.size()==0){
                Toast.makeText(getBaseContext(), "No se ha encontrado la localización introducida",
                		Toast.LENGTH_SHORT).show();
            }
 
            // Clears all the existing markers on the map
            mGoogleMap.clear();
 
            // Adding Markers on Google Map for each matching address
            for(int i=0;i<addresses.size();i++){
 
                Address address = (Address) addresses.get(i);
 
                // Creating an instance of GeoPoint, to display in Google Map
                latLng = new LatLng(address.getLatitude(), address.getLongitude());
 
                String addressText = String.format("%s, %s",
                address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                address.getCountryName());
 
                markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(addressText);
 
                mGoogleMap.addMarker(markerOptions);
 
                // Locate the first location
                if(i==0){
                	mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                
                
                	//Set the destination coordinates
                	
                    LatLng startPoint = new LatLng(mLatitude, mLongitude);
                    pOrigen = startPoint;
                    pDestino = latLng;
                	// Already map contain destination location
                    if(mMarkerPoints.size()>1){
 
                        FragmentManager fm = getSupportFragmentManager();
                        mMarkerPoints.clear();
                        mGoogleMap.clear();
 
                        // draw the marker at the current position
                        drawMarker(startPoint);
                        
                    }
                    dLatitude = latLng.latitude;
                    dLongitude = latLng.longitude;
                    // draws the marker at the end location
                    drawMarker(latLng);
 
                    // Checks, whether start and end locations are captured
                    if(mMarkerPoints.size() >= 2){
                        LatLng origin = mMarkerPoints.get(0);
                        LatLng dest = mMarkerPoints.get(1);
 
                        // Getting URL to the Google Directions API
                        String url = getDirectionsUrl(origin, dest);
 
                        DownloadTask downloadTask = new DownloadTask();
 
                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url);
                    }
                }
            }
        }
    }
	
	private String getDirectionsUrl(LatLng origin,LatLng dest){
		 
        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
 
        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
 
        // Sensor enabled
        String sensor = "sensor=false";
 
        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
 
        // Output format
        String output = "json";
 
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
 
        return url;
    }
	
	/** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
                URL url = new URL(strUrl);                
                

                // Creating an http connection to communicate with url 
                urlConnection = (HttpURLConnection) url.openConnection();                

                // Connecting to url 
                urlConnection.connect();                

                // Reading data from url 
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb  = new StringBuffer();

                String line = "";
                while( ( line = br.readLine())  != null){
                        sb.append(line);
                }

                data = sb.toString();

                br.close();

        }catch(Exception e){
                Log.d("Exception while downloading url", e.toString());
        }finally{
                iStream.close();
                urlConnection.disconnect();
        }

        return data;
    }         

    /** A class to download data from Google Directions URL */
    private class DownloadTask extends AsyncTask<String, Void, String>{
 
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
 
            // For storing data from web service
            String data = "";
 
            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
 
        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
 
            ParserTaskRoute parserTaskRoute = new ParserTaskRoute();
 
            // Invokes the thread for parsing the JSON data
            parserTaskRoute.execute(result);
        }
    }
	
    /** A class to parse the Google Directions in JSON format */
    private class ParserTaskRoute extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
 
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
 
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
 
            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
 
                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }
 
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";
 
            if(result.size()<1){
                //Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }
 
            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                
 
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
 
                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    
                    if(j==0){    // Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }
 
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
 
                    points.add(position);
                }
 
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(4);
                lineOptions.color(Color.RED);
            }
 
            tvDistanceDuration.setText("Distance:"+distance + ", Duration:"+duration);
            
            // Drawing polyline in the Google Map for the i-th route
            mGoogleMap.addPolyline(lineOptions);
        }
    }
	/** A class, to download Google Places */
	private class PlacesTask extends AsyncTask<String, Integer, String>{

		String data = null;
		
		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... url) {
			try{
				data = downloadUrl(url[0]);
			}catch(Exception e){
				 Log.d("Background Task",e.toString());
			}
			return data;
		}
		
		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(String result){			
			ParserTask parserTask = new ParserTask();
			
			// Start parsing the Google places in JSON format
			// Invokes the "doInBackground()" method of the class ParseTask
			parserTask.execute(result);
		}
		
	}
	
	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

		JSONObject jObject;
		
		// Invoked by execute() method of this object
		@Override
		protected List<HashMap<String,String>> doInBackground(String... jsonData) {
		
			List<HashMap<String, String>> places = null;			
			PlaceJSONParser placeJsonParser = new PlaceJSONParser();
        
	        try{
	        	jObject = new JSONObject(jsonData[0]);
	        	
	            /** Getting the parsed data as a List construct */
	            places = placeJsonParser.parse(jObject);
	            
	        }catch(Exception e){
	                Log.d("Exception",e.toString());
	        }
	        return places;
		}
		
		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(List<HashMap<String,String>> list){			
			
			
			if(mMarkerPoints.size() < 2){
			
				// Clears all the existing markers and the map
				mGoogleMap.clear();
				mMarkerPoints.clear();
				// draws the marker at the current position
				pOrigen = new LatLng(mLatitude, mLongitude);
	            drawMarker(pOrigen);
	            // Already map contain destination location
	            if(mMarkerPoints.size()>1){
	            
	            	// draws the marker at the end location
	            	drawMarker(pDestino);
	            }
			}
            
            // Checks, whether start and end locations are captured
            if(mMarkerPoints.size() >= 2){
                LatLng origin = mMarkerPoints.get(0);
                LatLng dest = mMarkerPoints.get(1);
                
                // Clears all the existing markers and the map
				mGoogleMap.clear();
				mMarkerPoints.clear();

				// draws the marker at the current position
	            drawMarker(origin);
            	drawMarker(dest);
	            

                // Getting URL to the Google Directions API
                String url = getDirectionsUrl(origin, dest);

                DownloadTask downloadTask = new DownloadTask();

                // Start downloading json data from Google Directions API
                downloadTask.execute(url);
            }
			
			for(int i=0;i<list.size();i++){
			
				// Creating a marker
	            MarkerOptions markerOptions = new MarkerOptions();
	            
	            // Getting a place from the places list
	            HashMap<String, String> hmPlace = list.get(i);
	
	            // Getting latitude of the place
	            double lat = Double.parseDouble(hmPlace.get("lat"));	            
	            
	            // Getting longitude of the place
	            double lng = Double.parseDouble(hmPlace.get("lng"));
	            
	            // Getting name
	            String name = hmPlace.get("place_name");
	            
	            // Getting vicinity
	            String vicinity = hmPlace.get("vicinity");
	            
	            LatLng latLng = new LatLng(lat, lng);
	            
	            // Setting the position for the marker
	            markerOptions.position(latLng);
	
	            // Setting the title for the marker. 
	            //This will be displayed on taping the marker
	            markerOptions.title(name + " : " + vicinity);	            
	
	            // Placing a marker on the touched position
	            Marker m = mGoogleMap.addMarker(markerOptions);	            

	            // Linking Marker id and place reference
	            mMarkerPlaceLink.put(m.getId(), hmPlace.get("reference"));	            
	           
            
			}		
			
		}
		
	}
	//Dialog from Search button
	public class DialogoText extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	 

	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	 
	    builder.setView(inflater.inflate(R.layout.intro_text, null))
	       .setPositiveButton("Mostrar Ruta", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   	 
	        	   	//get the address introduced by the user
	     	      	EditText etLocation2 = (EditText) getDialog().findViewById(R.id.et_location2);
	     	      	String desti = etLocation2.getText().toString();
	        	    //searches the destination
	     	      	doSearch(desti);
           		}
	    });
	 
	    return builder.create();
	    
	    }
	}
	// Dialog from Route button
	public class DialogoPersonalizado extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	 
		    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		    LayoutInflater inflater = getActivity().getLayoutInflater();
		 
		    builder.setView(inflater.inflate(R.layout.dialogo_elegir, null))
		    	.setPositiveButton("Coche", new DialogInterface.OnClickListener()  {
	            public void onClick(DialogInterface dialog, int id) {
	                 Log.i("Dialogos", "Confirmacion Aceptada.");
	         			//If the user selects car, open navigation with driving route
	         			Intent openNavigation = new Intent(Intent.ACTION_VIEW, 
	         				Uri.parse("google.navigation:ll=" + pDestino.latitude + "," + pDestino.longitude + "&mode=d"));
	         			startActivity(openNavigation);	 
	         		}
	            })
	            .setNegativeButton("Caminando", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	                     Log.i("Dialogos", "Confirmacion Cancelada.");
	                     	//If the user selects caminando, open navigation with driving route
		         			Intent openNavigation = new Intent(Intent.ACTION_VIEW, 
		         				Uri.parse("google.navigation:ll=" + pDestino.latitude + "," + pDestino.longitude + "&mode=walking"));
		         			startActivity(openNavigation);	                
		         		}
	            })
	            .setNeutralButton("Bici", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	                     Log.i("Dialogos", "Confirmacion Cancelada.");
	                     	//If the user selects Bici, open navigation with cycling route
		         			Intent openNavigation = new Intent(Intent.ACTION_VIEW, 
		         				Uri.parse("google.navigation:ll=" + pDestino.latitude + "," + pDestino.longitude + "&mode=bicycling"));
		         			startActivity(openNavigation);
		         		}
	            });
		   
		 
		    return builder.create();
		    }
	    
	}
	//Dialog to set the radius in order to change the searching area
	public class DialogoSeleccion extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	 
	     final String[] items = {"1km", "2km", "5km", "10km"};
	 
	        AlertDialog.Builder builder =
	                new AlertDialog.Builder(getActivity());
	        
	        builder.setTitle("Seleccione el radio de búsqueda")
	        .setSingleChoiceItems(items, -1,
	                 new DialogInterface.OnClickListener() {
	              public void onClick(DialogInterface dialog, int item) {
	                  Log.i("Dialogos", "Opción elegida: " + items[item]);
	                  if(item==0)
	                	  radio="1000";
	                  if(item==1)
	                	  radio="2000";
	                  if(item==2)
	                	  radio="5000";
	                  if(item==3)
	                	  radio="10000";
	                  /*
	                  Toast.makeText(getBaseContext(), "Radio de búsqueda: "+escucha22, Toast.LENGTH_SHORT).show();
	                  if((escucha22.equals("uno"))||(escucha22.equals("1"))){
	                  	  radio="1000";
	                  	  dialog.dismiss();
	        	        }
	        	        if(escucha22.equals("dos"))
	        	          	  radio="2000";
	        	        if(escucha22.equals("cinco"))
	        	          	  radio="3000";
	        	        if(escucha22.equals("diez"))
	        	          	  radio="4000";
	                  */
	                  Toast.makeText(getBaseContext(), "Radio de búsqueda: "+items[item], Toast.LENGTH_SHORT).show();
	                  dialog.dismiss();
	              }
	              
	              
	          });
	        
	        
	 
	        return builder.create();
	    }
	}
	
	private void drawMarker(LatLng point){
        mMarkerPoints.add(point);
 
        // Creating MarkerOptions
        MarkerOptions options = new MarkerOptions();
 
        // Setting the position of the marker
        options.position(point);
 
        /**
        * For the start location, the color of marker is GREEN and
        * for the end location, the color of marker is RED.
        */
        if(mMarkerPoints.size()==1){
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }else if(mMarkerPoints.size()==2){
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
 
        // Add new marker to the Google Map Android API V2
        mGoogleMap.addMarker(options);
    }

	public void clearMap(){
		// Removes all the points from Google Map
    	mGoogleMap.clear();

        // Removes all the points in the ArrayList
        mMarkerPoints.clear();
        flag_car=0;
        //Set the origin
        pOrigen = new LatLng(mLatitude, mLongitude);
        
        //Erase destination point
        dLatitude = mLatitude;
        dLongitude = mLongitude;
        pDestino =pOrigen;
        //Erase time and distance in the text view
        tvDistanceDuration.setText("Seleccione destino");
        
        drawMarker(pOrigen);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{	
		switch(item.getItemId())
		{
			case R.id.menu_vista:
				alternarVista();
				break;
			
			// Set car position
			case R.id.menu_marcar:
				
				if(flag_car==0){
					setCarLocation();
					flag_car=1;
				}else{
					Toast.makeText(MainActivity.this, 
							"Ya ha guardado la posición del coche.\n" +
							"Limpie el mapa para poder marcarla de nuevo", 
							Toast.LENGTH_LONG).show();
				}
					
				
				break;
			// Find car
			case R.id.menu_coche:
				
				if(flag_car==1){
					findCar();
				}else{
					Toast.makeText(MainActivity.this, 
							"Primero debe indicar dónde ha aparcado el coche", 
							Toast.LENGTH_LONG).show();
				}
								
				break;
			case R.id.menu_radio:
				
				dialogRadio();
				break;
				
			case R.id.menu_posicion:
				
				obtenerCoordenadas();
				break;
			case R.id.menu_informacion:
				
				toastInfo();
				break;
				
			case R.id.action_buscar:
				
				dialogBuscar();
				break;
				
			case R.id.action_ruta:
				
				dialogRuta();
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Open the toast with information regarding the actions the user
	 * can perform
	 */
	public void toastInfo(){
		
		final Toast toastInfo = new Toast(getApplicationContext());
		 
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_info,
                        (ViewGroup) findViewById(R.id.lytLayoutV1));
 		 
        toastInfo.setDuration(Toast.LENGTH_LONG);
        toastInfo.setView(layout);
        toastInfo.show();
        //This solution makes the toast last for 10 seconds aprox
        new CountDownTimer(6000, 1000)
        {

            public void onTick(long millisUntilFinished) {toastInfo.show();}
            public void onFinish() {toastInfo.show();}

        }.start();
		
	}
	/**
	 * Open the toast with the options for navigation 
	 */
	public void toastRuta(){
		
		final Toast toastRuta = new Toast(getApplicationContext());
		 
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_ruta,
                        (ViewGroup) findViewById(R.id.lytLayoutVR1));
 		 
        toastRuta.setDuration(Toast.LENGTH_LONG);
        toastRuta.setView(layout);
        toastRuta.show();
        //This solution makes the toast last for 5 seconds aprox
        new CountDownTimer(5000, 1000)
        {

            public void onTick(long millisUntilFinished) {toastRuta.show();}
            public void onFinish() {toastRuta.show();}

        }.start();
		
	}
	/**
	 * Open the toast with the options of POI 
	 */
	public void toastOptions(){
		
		final Toast toastOptions = new Toast(getApplicationContext());
		 
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_opciones,
                        (ViewGroup) findViewById(R.id.lytLayoutHO));
 		 
        toastOptions.setDuration(Toast.LENGTH_LONG);
        toastOptions.setView(layout);
        toastOptions.show();
        //This solution makes the toast last for 5 seconds aprox
        new CountDownTimer(5000, 1000)
        {

            public void onTick(long millisUntilFinished) {toastOptions.show();}
            public void onFinish() {toastOptions.show();}

        }.start();
		
	}
	public void queryPlacesAPI(){
		// Query to Places API with the desired parameters
		StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
		sb.append("location="+dLatitude+","+dLongitude);
		sb.append("&radius="+radio);
		sb.append("&types="+type);
		sb.append("&sensor=true");
		sb.append("&key=AIzaSyBEq_WB0F_LF1tyilH36nZlRpHSqej5fqw");
		
		// Creating a new non-ui thread task to download Google place json data 
        PlacesTask placesTask = new PlacesTask();		        			        
        
		// Invokes the "doInBackground()" method of the class PlaceTask
        placesTask.execute(sb.toString());
	}
	
	/**
	* This initialization routine solves an issue when the user pressed the navigation button 
	* at the very begining 
    */
	public void initTask(){
		
		String inicializar = "majadahonda";
		clearMap();
		//doSearch(inicializar);
		//mMarkerPoints.set(0, pOrigen);
		//mMarkerPoints.set(1, pDestino);
		
	}
	
	/**
	* Opens a dialog that allows the user to enter a destination when he/she presses 
	* that option in the ActionBar
    */
	public void dialogBuscar(){
		
		FragmentManager fragmentMan = getSupportFragmentManager();
        
		DialogoText diaText= new DialogoText();
		diaText.show(fragmentMan, "tagAlerta");
	}
	
	/**
	* Opens a dialog to start the navigation when the user presses that option in the ActionBar
    */
	public void dialogRuta(){
		
		FragmentManager fragmentManager = getSupportFragmentManager();
        
		DialogoPersonalizado dialogo = new DialogoPersonalizado();
        dialogo.show(fragmentManager, "tagAlerta");
	}
	
	/**
	* Opens a dialog to select the radius when the user presses that option in the menu
    */
	public void dialogRadio(){
		
		FragmentManager fragmentSele = getSupportFragmentManager();
        
		DialogoSeleccion diaSele= new DialogoSeleccion();
		diaSele.show(fragmentSele, "tagAlerta");
		
	}
	
	/**
	* Display on screen the position the user is looking 
    */
	public void obtenerCoordenadas(){
		CameraPosition camPos2 = mGoogleMap.getCameraPosition();
		LatLng pos = camPos2.target;
		Toast.makeText(MainActivity.this, 
				"Lat: " + pos.latitude + " - Lng: " + pos.longitude, 
				Toast.LENGTH_LONG).show();
	}
	
	/**
	* Stores the position of the car 
    */
	public void setCarLocation(){
		
		// Current position where you have just parked the car
		cPosition = new LatLng(mLatitude, mLongitude);
		cLat=mLatitude;
		cLon=mLongitude;
		
 
        // Setting the position of the marker
        car.position(cPosition);
 
        // Blue icon at car's position
        car.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        
        // Add new marker to the Google Map Android API V2
        mGoogleMap.addMarker(car);
		
	}
	
	/**
	* Points the car location
    */
	public void findCar(){
		
		//Set the destination coordinates
    	dLatitude = cPosition.latitude;
        dLongitude = cPosition.longitude;
        LatLng startPoint = new LatLng(mLatitude, mLongitude);
        pOrigen = startPoint;
        pDestino = cPosition;

        // Setting the position of the marker
        //car.position(cPosition);
 
        // Blue icon at car's position
        car.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        
        // Add new marker to the Google Map Android API V2
        mGoogleMap.addMarker(car);
        CameraUpdate camUpd1 = CameraUpdateFactory.newLatLng(new LatLng(cLat, cLon));
		mGoogleMap.moveCamera(camUpd1);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        // Getting URL to the Google Directions API
        //String url = getDirectionsUrl(pOrigen, pDestino);

        //DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        //downloadTask.execute(url);
  	}
	/**
	* Moves the camera to the current position 
    */
	public void currentPos(){
		CameraUpdate camUpd1 = CameraUpdateFactory.newLatLng(new LatLng(mLatitude, mLongitude));
			mGoogleMap.moveCamera(camUpd1);
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
	}
	
	/**
	* Everytime is executed, switches to a different map view
    */
	private void alternarVista()
	{
		vista = (vista + 1) % 4;

		switch(vista)
		{
			case 0:
				mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				break;
			case 1:
				mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				break;
			case 2:
				mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				break;
			case 3:
				mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
				break;
		}
	}
	
	/**
	* Takes the address introduced by the user and searches the coordinates in the map 
	* using Geocoder
    */
	public void doSearch(String cadena){
		String sitio = cadena;
		if(sitio!=null && !sitio.equals("")){
				new GeocoderTask().execute(sitio);
			}
	}
	
	/**
	* Starts navigation for current position to selected destination
    */
	public void doGo(){
	  	LatLng startPoint = new LatLng(mLatitude, mLongitude);
		LatLng targetPoint = pDestino;
		
		Intent openNavigation = new Intent(Intent.ACTION_VIEW, 
				Uri.parse("google.navigation:ll=" + targetPoint.latitude + "," + targetPoint.longitude + "&mode=d"));
		startActivity(openNavigation);
		
	  }	
	
	/**
	* Stores the current location, update the marker and move the camera smoothly
    */
	@Override
	public void onLocationChanged(Location location) {
		// Draw the marker, if destination location is not set
        if(mMarkerPoints.size() < 2){
 
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            LatLng point = new LatLng(mLatitude, mLongitude);
 
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(point));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
 
            drawMarker(point);
        }
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub		
	}
	
	/**
	* Restore the previously serialized current dropdown position.
    */
	@Override
	  public void onRestoreInstanceState(Bundle savedInstanceState) {
	    // 
	    if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
	      getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
	    }
	  }
	
 	  /**
	  * Serialize the current dropdown position.
      */
	  @Override
	  public void onSaveInstanceState(Bundle outState) {
	    outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
	        .getSelectedNavigationIndex());
	  }
	  
  	/**
     * Handle the action of the button being clicked
     */
    public void speakButtonClicked(View v)
    {
        startVoiceRecognitionActivity();
    }
 
    /**
     * Fire an intent to start the voice recognition activity when the user presses the button 
     */
    private void startVoiceRecognitionActivity()
    {
    	//start the speech recognition intent passing required data
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //set speech model
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //specify number of results to retrieve
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        //message to display while listening
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga 'información' si no conoce las opciones disponibles");
        startActivityForResult(intent, REQUEST_CODE);
    }
    /**
     * Fire an intent to start the voice recognition activity after hearing 'navegación'
     */
    private void recognitionRuta()
    {
    	//start the speech recognition intent passing required data
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //set speech model
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //specify number of results to retrieve
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        //message to display while listening
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Por favor, elija cómo desea iniciar la ruta");
        startActivityForResult(intent, REQUEST_CODE2);
    }
    /**
     * Fire an intent to start the voice recognition activity after listening 'cambiar radio' 
     */
    private void recognitionRadio()
    {
    	//start the speech recognition intent passing required data
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //set speech model
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //specify number of results to retrieve
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        //message to display while listening
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Por favor, elija qué radio desea");
        startActivityForResult(intent, REQUEST_CODE3);
    }
    /**
     * Fire an intent to start the voice recognition activity after listening 'buscar' 
     */
    private void recognitionBuscar()
    {
    	//start the speech recognition intent passing required data
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //set speech model
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //specify number of results to retrieve
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        //message to display while listening
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Por favor, diga una dirección");
        startActivityForResult(intent, REQUEST_CODE4);
        
    }
    /**
     * Fire an intent to start the voice recognition activity after listening 'buscar punto de interés' 
     */
    private void recognitionPOI()
    {
    	//start the speech recognition intent passing required data
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //set speech model
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //specify number of results to retrieve
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        //message to display while listening
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Si no conoce las opciones, diga 'opciones'");
        startActivityForResult(intent, REQUEST_CODE5);
    }
    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	//First interaction with user. When the button is pushed..
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String escucha = matches.get(0);
            //String escucha22 =matches.get(0);

            //Shows a toast with the interactions available
            if(escucha.equals("información")||escucha.equals("info")||escucha.equals("instrucciones")){
            	toastInfo();
            	            	
            }
            //if the user says this words, the program looks for POIs
            else if(escucha.equals("buscar puntos de interés")||escucha.equals("buscar punto de interés")||escucha.equals("localizar puntos de interés")
            		||escucha.equals("buscar servicios")||escucha.equals("localizar servicios")||escucha.equals("punto de interés")
            		||escucha.equals("puntos de interés")){
            	//information using the TTS
				mTts.speak("¿Qué servicios le interesan?", TextToSpeech.QUEUE_FLUSH, null);
				// SLEEP 2 SECONDS HERE ...
			    Handler handler = new Handler(); 
			    handler.postDelayed(new Runnable() { 
			         public void run() { 
			        	 recognitionPOI();
			         } 
			    }, 1800); 
			    
            }
            //if the user says this words, the program search the address using Geocoder
            else if(escucha.equals("buscar")||escucha.equals("buscar localización")||escucha.equals("buscar calle")
            		||escucha.equals("buscar lugar")||escucha.equals("oscar")){
            	//information using the TTS
				mTts.speak("¿Qué lugar desea buscar?", TextToSpeech.QUEUE_FLUSH, null);
				// SLEEP 2 SECONDS HERE ...
			    Handler handler = new Handler(); 
			    handler.postDelayed(new Runnable() { 
			         public void run() { 
			        	 //flag_buscar=1;
			        	 recognitionBuscar();
			        	 
			         } 
			    }, 1800); 
			    
			    
			    
            }
            //If the user says this words, the program offers to launch navigation either by bike, car or walking
            else if((escucha.equals("navegación"))||(escucha.equals("ruta"))||(escucha.equals("iniciar la navegación"))
            		||(escucha.equals("iniciar navegación"))||(escucha.equals("iniciar ruta"))||(escucha.equals("iniciar la ruta"))){
				toastRuta();
				//information using the TTS
				mTts.speak("¿Desea iniciar la navegación en coche, en bicicleta o caminando?", TextToSpeech.QUEUE_FLUSH, null);
				// SLEEP 5 SECONDS HERE ...
			    Handler handler = new Handler(); 
			    handler.postDelayed(new Runnable() { 
			         public void run() { 
			        	 recognitionRuta();
			         } 
			    }, 5000); 
				
            	
            }
            //Command for the user to change the searching area
            else if((escucha.equals("área de búsqueda"))||(escucha.equals("cambiar radio"))||(escucha.equals("cambiar área de búsqueda"))
            		||(escucha.equals("cambiar radio"))||(escucha.equals("cambiar radio de búsqueda"))||(escucha.equals("cambiar área de búsqueda"))){
            	//dialogRadio();
            	//information using the TTS
				mTts.speak("¿Desea buscar en una distancia de 1, 2, 5 o 10 kilómetros?", TextToSpeech.QUEUE_FLUSH, null);
				// SLEEP 5 SECONDS HERE ...
			    Handler handler = new Handler(); 
			    handler.postDelayed(new Runnable() { 
			         public void run() { 
			        	 recognitionRadio();				         
			        	 } 
			    }, 5300);
			    
            }
            //Commands to set car location
            else if((escucha.equals("he aparcado aquí"))||(escucha.equals("marcar posición"))||(escucha.equals("marcador coche"))
            		||(escucha.equals("he aparcado mi coche aquí"))||(escucha.equals("marcar aparcamiento"))||(escucha.equals("marcar posición"))){
            	if(flag_car==0){
					setCarLocation();
					mTts.speak("Se ha guardado la posición del coche",
	            			TextToSpeech.QUEUE_FLUSH, null);
					flag_car=1;
				}else{
	            	mTts.speak("Ya ha guardado la posición del coche. Limpie el mapa para poder marcarla de nuevo",
	            			TextToSpeech.QUEUE_FLUSH, null);
				}
            }
            //Commands to ask for car location
            else if((escucha.equals("dónde está mi coche"))||(escucha.equals("encontrar coche"))||(escucha.equals("encontrar mi coche"))
            		||(escucha.equals("donde está mi coche"))||(escucha.equals("coche"))||(escucha.equals("buscar mi coche"))||(escucha.equals("donde he aparcado"))){
            	//information using the TTS
            	if(flag_car==1){
					findCar();
					mTts.speak("Aquí se encuentra su coche",
	            			TextToSpeech.QUEUE_FLUSH, null);
				}else{
	            	mTts.speak("Primero debe indicar dónde ha aparcado el coche", TextToSpeech.QUEUE_FLUSH, null);
				}
            }
            //Commands to change the map view
            else if((escucha.equals("mapa"))||(escucha.equals("cambiar mapa"))||(escucha.equals("cambiar vista"))){
            	alternarVista();
            }
            //Commands to get the current position
            else if(escucha.equals("obtener coordenadas")||escucha.equals("coordenadas")||escucha.equals("obtener posición")){
            	obtenerCoordenadas();
            }
            //Commands to clear the map
            else if(escucha.equals("limpiar mapa")||escucha.equals("limpiar")||escucha.equals("borrar")||escucha.equals("borrar mapa")
            		||escucha.equals("borrar el mapa")||escucha.equals("limpiar el mapa")){
            	clearMap();
            }
            //Commands to set the view at the current position
            else if(escucha.equals("posición actual")||escucha.equals("mi posición actual")||escucha.equals("ir a mi posición")
            		||escucha.equals("obtener posición actual")){
            	currentPos();
            //if the user wants to cancel
            }else if((escucha.equals("cancelar"))||escucha.equals("atrás")||escucha.equals("volver atrás")||escucha.equals("ninguna")
        			||escucha.equals("ninguno")||escucha.equals("no, gracias")||escucha.equals("no")||escucha.equals("no gracias")||escucha.equals("salir")
        			||escucha.equals("No")){
            //if non of the above, asks the user to repeat	
            }else{
            	//information using the TTS
				mTts.speak("Por favor, ¿podría repetir?", TextToSpeech.QUEUE_FLUSH, null);
	            Toast.makeText(getBaseContext(), "¿Ha dicho usted: "+escucha +"?", Toast.LENGTH_LONG).show();
	        	 // SLEEP 3 SECONDS HERE ...
			    Handler handler = new Handler(); 
			    handler.postDelayed(new Runnable() { 
			         public void run() { 
			        	 startVoiceRecognitionActivity();
			         } 
			    }, 3000); 
            }
        }
        //If user answers to launch navigation
        if (requestCode == REQUEST_CODE2 && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches2 = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String escucha2 = matches2.get(0);
            //String escucha22 =matches2.get(0);
            
            if(escucha2.equals("coche")||(escucha2.equals("en coche"))||(escucha2.equals("encoche"))){
            	//If the user selects car, open navigation with driving route
     			Intent openNavigation = new Intent(Intent.ACTION_VIEW, 
     				Uri.parse("google.navigation:ll=" + pDestino.latitude + "," + pDestino.longitude + "&mode=d"));
     			startActivity(openNavigation);
     			
            }else if((escucha2.equals("bici"))||(escucha2.equals("bicicleta"))||(escucha2.equals("en bici"))||(escucha2.equals("en bicicleta"))){
            	//If the user selects Bici, open navigation with cycling route
     			Intent openNavigation = new Intent(Intent.ACTION_VIEW, 
     				Uri.parse("google.navigation:ll=" + pDestino.latitude + "," + pDestino.longitude + "&mode=bicycling"));
     			startActivity(openNavigation);
     			
            }else if((escucha2.equals("caminando"))||(escucha2.equals("andando"))||(escucha2.equals("a pie"))){
            	//If the user selects caminando, open navigation with driving route
     			Intent openNavigation = new Intent(Intent.ACTION_VIEW, 
     				Uri.parse("google.navigation:ll=" + pDestino.latitude + "," + pDestino.longitude + "&mode=walking"));
     			startActivity(openNavigation);	                
                
     		//if the user wants to cancel
            }else if((escucha2.equals("cancelar"))||escucha2.equals("atrás")||escucha2.equals("volver atrás")||escucha2.equals("ninguna")
        			||escucha2.equals("ninguno")||escucha2.equals("no, gracias")||escucha2.equals("no")||escucha2.equals("no gracias")
        			||escucha2.equals("salir")||escucha2.equals("No")){
            	
            
            }//if non of the above, asks the user to repeat
            else{
            
            	mTts.speak("Debe elegir una de las tres opciones por favor", TextToSpeech.QUEUE_FLUSH, null);
				
				// SLEEP 4 SECONDS HERE ...
			    Handler handler = new Handler(); 
			    handler.postDelayed(new Runnable() { 
			         public void run() { 
			        	 recognitionRuta();
			         } 
			    }, 4000); 		        	
			    //startVoiceRecognitionActivity2();				          

            }
        }
        //When user asks to change the radius
        if (requestCode == REQUEST_CODE3 && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String escucha = matches.get(0);
            //Set radius 1km
            if((escucha.equals("uno"))||(escucha.equals("un kilómetro"))||(escucha.equals("1"))||(escucha.equals("1 km"))){
            	radio="1000";
            	queryPlacesAPI();
                Toast.makeText(getBaseContext(), "Radio de búsqueda: 1km", Toast.LENGTH_SHORT).show();

        	}
            //Set radius 2km
            else if((escucha.equals("dos"))||(escucha.equals("dos kilómetros"))||(escucha.equals("2"))||(escucha.equals("2 km"))){
        		radio="2000";
        		queryPlacesAPI();
                Toast.makeText(getBaseContext(), "Radio de búsqueda: 2km", Toast.LENGTH_SHORT).show();

        	}
            //Set radius 5km
            else if((escucha.equals("cinco"))||(escucha.equals("cinco kilómetros"))||(escucha.equals("5"))||(escucha.equals("5 km"))){
        		radio="5000";
        		queryPlacesAPI();
                Toast.makeText(getBaseContext(), "Radio de búsqueda: 5km", Toast.LENGTH_SHORT).show();
  
	        }
            //Set radius 10km
            else if((escucha.equals("diez"))||(escucha.equals("diez kilómetros"))||(escucha.equals("10"))||(escucha.equals("10 km"))){
        		radio="10000";
        		queryPlacesAPI();
        		Toast.makeText(getBaseContext(), "Radio de búsqueda: 10km", Toast.LENGTH_SHORT).show();

        	}
            //If the user wants to cancel
            else if((escucha.equals("cancelar"))||escucha.equals("atrás")||escucha.equals("volver atrás")||escucha.equals("ninguna")
        			||escucha.equals("ninguno")||escucha.equals("no, gracias")||escucha.equals("no")||escucha.equals("no gracias")
        			||escucha.equals("salir")||escucha.equals("No")){
            	

        	}//if non of the above, asks the user to repeat
            else{
	            Toast.makeText(getBaseContext(), "¿Ha dicho usted: "+escucha +"?", Toast.LENGTH_LONG).show();
	            mTts.speak("Por favor, ¿podría repetir?", TextToSpeech.QUEUE_FLUSH, null);
				
				// SLEEP 3 SECONDS HERE ...
			    Handler handler = new Handler(); 
			    handler.postDelayed(new Runnable() { 
			         public void run() { 
			        	 recognitionRadio();
			        	 } 
			    }, 3000); 	

        	}
            
        }
        //When the user asks for an address
        if (requestCode == REQUEST_CODE4 && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String escucha = matches.get(0);
            //set flag to 1 in order to activate following question
            flag_radio=1;
            //Search location introduced by user
            doSearch(escucha);
    	    	
		 	mTts.speak("¿Le gustaría ver establecimientos próximos?. Si es así, ¿de qué tipo?", TextToSpeech.QUEUE_FLUSH, null);
		    //flag_buscar=0;
			// SLEEP 5.7 SECONDS HERE ...
		    Handler handler2 = new Handler(); 
		    handler2.postDelayed(new Runnable() { 
		         public void run() { 
		        	 recognitionPOI();
		         } 
		    }, 5700); 
    		                
        }
        //When the user looks for a point of interest or establishment
        if (requestCode == REQUEST_CODE5 && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String escucha = matches.get(0);
            
            //commands for the user to hear the possible choices
            if((escucha.equals("opciones"))||(escucha.equals("cuales son las opciones"))||(escucha.equals("qué opciones hay"))
            		||(escucha.equals("qué opciones tengo"))||(escucha.equals("cuáles son las opciones"))){
                toastOptions();
                mTts.speak("Puede seleccionar cajeros gasolineras restaurantes, o cualquier categoría de las " +
                		"mostradas en pantalla. ¿Cuál le interesa?", TextToSpeech.QUEUE_FLUSH, null);
            	// SLEEP 8.65 SECONDS HERE ...
			    Handler handler = new Handler(); 
			    handler.postDelayed(new Runnable() { 
			         public void run() { 
			        	 recognitionPOI();
			         } 
			    }, 8650); 
        	}
            //If the user wants to stop the dialog
            else if((escucha.equals("cancelar"))||escucha.equals("atrás")||escucha.equals("volver atrás")||escucha.equals("ninguna")
        			||escucha.equals("ninguno")||escucha.equals("no, gracias")||escucha.equals("no")||escucha.equals("no gracias")
        			||escucha.equals("salir")||escucha.equals("No")){
            	flag_radio=0;

        	}
            //The following commands launch different queries
            else if((escucha.equals("aeropuerto"))||(escucha.equals("aeropuertos"))){
                
        		type="airport";
                queryPlacesAPI();
                
        	}else if((escucha.equals("cajero"))||(escucha.equals("cajeros"))||(escucha.equals("cajero automático"))||(escucha.equals("cajeros automáticos"))){
        		
                type="atm";
                queryPlacesAPI();

        	}else if((escucha.equals("banco"))||(escucha.equals("bancos"))||(escucha.equals("usureros"))||(escucha.equals("ladrones"))){
        		
                type="bank";
                queryPlacesAPI();

        	}else if((escucha.equals("gasolinera"))||(escucha.equals("gasolineras"))){
                
                type="gas_station";
                queryPlacesAPI();

        	}else if((escucha.equals("supermercados"))||(escucha.equals("supermercado"))||(escucha.equals("ultramarinos"))){
        		
        		type="grocery_or_supermarket";
                queryPlacesAPI();

        	}else if((escucha.equals("doctor"))||(escucha.equals("doctores"))||(escucha.equals("especialistas"))||(escucha.equals("salud"))
        			||(escucha.equals("médico"))||(escucha.equals("médicos"))){
        		
        		type="doctor";
                queryPlacesAPI();
        	}else if((escucha.equals("hospitales"))||(escucha.equals("hospital"))||(escucha.equals("urgencia"))||(escucha.equals("centros de urgencia"))
        			||(escucha.equals("centro de urgencia"))){
                
                type="hospital";
                queryPlacesAPI();

        	}else if((escucha.equals("cines"))||(escucha.equals("cine"))||(escucha.equals("salas de cine"))){
        		
        		type="movie_theater";
                queryPlacesAPI();
                
        	}else if((escucha.equals("restaurantes"))||(escucha.equals("restaurante"))||(escucha.equals("sitios para comer"))){
        		
        		type="restaurant";
                queryPlacesAPI();
                
        	}else if((escucha.equals("galería"))||(escucha.equals("galería de arte"))||(escucha.equals("galerías de arte"))
        			||(escucha.equals("arte"))){
        		
        		type="art_gallery";
                queryPlacesAPI();
                
        	}else if((escucha.equals("bar"))||(escucha.equals("bares"))||(escucha.equals("pubs"))||(escucha.equals("pabs"))
        			||(escucha.equals("baretos"))){
        		
        		type="bar";
                queryPlacesAPI();
                
        	}else if((escucha.equals("bus"))||(escucha.equals("estaciones de autobús"))||(escucha.equals("estación de autobus"))
        			||(escucha.equals("paradas de bus"))||(escucha.equals("paradas de autobús"))||(escucha.equals("estaciones de bus"))){
        		
        		type="bus_station";
                queryPlacesAPI();
                
        	}
        	else if((escucha.equals("café"))||(escucha.equals("cafetería"))||(escucha.equals("cafeterías"))){
        		
        		type="cafe";
                queryPlacesAPI();
                
        	}else if((escucha.equals("biblioteca"))||(escucha.equals("bibliotecas"))||(escucha.equals("biblio"))||(escucha.equals("biblios"))){
        		
        		type="library";
                queryPlacesAPI();
                
        	}else if((escucha.equals("museo"))||(escucha.equals("museos"))){
        		
        		type="museum";
                queryPlacesAPI();
                
        	}else if((escucha.equals("parque"))||(escucha.equals("parques"))||(escucha.equals("columpios"))||(escucha.equals("toboganes"))){
        		
        		type="park";
                queryPlacesAPI();
                
        	}else if((escucha.equals("parking"))||(escucha.equals("parkings"))||(escucha.equals("aparcamiento"))
        			||(escucha.equals("aparcamientos"))||(escucha.equals("estacionamiento"))||(escucha.equals("estacionamientos"))){
        		
        		type="parking";
                queryPlacesAPI();
                
        	}else if((escucha.equals("farmacia"))||(escucha.equals("farmacias"))){
        		
        		type="pharmacy";
                queryPlacesAPI();
                
        	}else if((escucha.equals("poli"))||(escucha.equals("policía"))||(escucha.equals("comisarías de policía"))
        			||(escucha.equals("comisaría"))||(escucha.equals("comisarías"))||(escucha.equals("comisaría de policía"))){
        		
        		type="police";
                queryPlacesAPI();
                
        	}else if((escucha.equals("correos"))||(escucha.equals("oficina de correos"))||(escucha.equals("mensajería"))
        			||(escucha.equals("correo"))){
        		
        		type="post_office";
                queryPlacesAPI();
                
        	}else if((escucha.equals("colegio"))||(escucha.equals("colegios"))||(escucha.equals("institutos"))
        			||(escucha.equals("educación"))){
        		
        		type="school";
                queryPlacesAPI();
                
        	}else if((escucha.equals("estadio"))||(escucha.equals("estadios"))||(escucha.equals("campos"))
        			||(escucha.equals("deportes"))){
        		
        		type="stadium";
                queryPlacesAPI();
                
        	}else if((escucha.equals("metro"))||(escucha.equals("estación de metro"))||(escucha.equals("estaciones de metro"))
        			||(escucha.equals("parada de metro"))||(escucha.equals("metros"))){
        		
        		type="subway_station";
                queryPlacesAPI();
                
        	}else if((escucha.equals("taxi"))||(escucha.equals("estación de taxi"))||(escucha.equals("paradas de taxi"))
        			||(escucha.equals("parada de taxi"))||(escucha.equals("taxis"))){
        		
        		type="taxi_stand";
                queryPlacesAPI();
                
        	}else if((escucha.equals("tren"))||(escucha.equals("estación de tren"))||(escucha.equals("estaciones de tren"))
        			||(escucha.equals("trenes"))){
        		
        		type="train_station";
                queryPlacesAPI();
                
        	}else if((escucha.equals("uni"))||(escucha.equals("universidad"))||(escucha.equals("universidades"))
        			||(escucha.equals("unis"))){
        		
        		type="university";
                queryPlacesAPI();
                
        	}
            
            
        	//If non of the above, asks the user to repeat
        	else{
	            Toast.makeText(getBaseContext(), "¿Ha dicho usted: "+escucha +"? \nPor favor, repita de nuevo la búsqueda", 
	            		Toast.LENGTH_LONG).show();
	            mTts.speak("¿podría repetir?. También puede cancelar", TextToSpeech.QUEUE_FLUSH, null);
    		    //flag_buscar=0;
    			// SLEEP 4 SECONDS HERE ...
    		    Handler handler2 = new Handler(); 
    		    handler2.postDelayed(new Runnable() { 
    		         public void run() { 
    		        	 recognitionPOI();
    		         } 
    		    }, 4000); 
            	flag_radio=0;


        	}
            //If the conversation started with 'buscar', the dialogue continues
            if(flag_radio==1){
	            mTts.speak("¿Le gustaría modificar el área de búsqueda?. Las opciones son 1,2,5 o 10 km", TextToSpeech.QUEUE_FLUSH, null);
	    		// SLEEP 6.7 SECONDS HERE ...
			    Handler handler = new Handler(); 
			    handler.postDelayed(new Runnable() { 
			         public void run() { 
			        	 recognitionRadio();
			         } 
			    }, 6700); 
			    flag_radio=0;
            }
            
        }
        
        
        //returned from TTS data check
        if (requestCode == MY_DATA_CHECK_CODE)
        {
            //we have the data - create a TTS instance
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
            	mTts = new TextToSpeech(this, this);
            //data not installed, prompt the user to install it
            else
            {
                //intent will take user to TTS download page in Google Play
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    /**
     * onInit fires when TTS initializes
     */
    public void onInit(int initStatus) {

    	Locale loc = new Locale ("spa", "ESP");

    	//if successful, set locale
        if (initStatus == TextToSpeech.SUCCESS)
        	mTts.setLanguage(loc);
    }
    


}