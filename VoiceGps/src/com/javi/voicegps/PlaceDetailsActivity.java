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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;



public class PlaceDetailsActivity extends Activity implements TextToSpeech.OnInitListener{
	WebView mWvPlaceDetails;
	
    private static final int REQUEST_CODEPD = 4567;
    String lati=null;
    String longi=null;
    String teli=null;
    String urli=null;
    String webi=null;
    
    //variable for checking TTS engine data on user device
    private int MY_DATA_CHECK_CODE = 0;
    //Text To Speech instance
    private TextToSpeech pdTts;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_place_details);
		
		//prepare the TTS to repeat chosen words
        Intent checkTTSIntent = new Intent();
        //check TTS data
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        //start the checking Intent - will retrieve result in onActivityResult
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
		// Array of place types
        
        // Instantiating TextToSpeech class
        pdTts = new TextToSpeech(this, this);
        //Button with the speaker to hear the options
        Button htmloptions = (Button) findViewById(R.id.html_options);
        htmloptions.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
            	 pdTts.speak("Las opciones son, Llamar, ver sitio web, ver sitio google, e iniciar la navegación." +
             			" ¿Cuál le interesa?", TextToSpeech.QUEUE_FLUSH, null);
            	// SLEEP 8 SECONDS HERE ...
 			    Handler handler = new Handler(); 
 			    handler.postDelayed(new Runnable() { 
 			         public void run() { 
 			        	startVoiceRecognitionActivityPD();
 			         } 
 			    }, 8000);
	          }
         });
        
		// Getting reference to WebView ( wv_place_details ) of the layout activity_place_details
		mWvPlaceDetails = (WebView) findViewById(R.id.wv_place_details);
		
		mWvPlaceDetails.getSettings().setUseWideViewPort(false);
		
		// Getting place reference from the map	
		String reference = getIntent().getStringExtra("reference");
		
		
		StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
		sb.append("reference="+reference);
		sb.append("&sensor=true");
		sb.append("&key=AIzaSyBEq_WB0F_LF1tyilH36nZlRpHSqej5fqw");
		
		
		// Creating a new non-ui thread task to download Google place details 
        PlacesTask placesTask = new PlacesTask();		        			        
        
		// Invokes the "doInBackground()" method of the class PlaceTask
        placesTask.execute(sb.toString());	
		
	};
	
	
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

	
	/** A class, to download Google Place Details */
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
			
			// Start parsing the Google place details in JSON format
			// Invokes the "doInBackground()" method of the class ParseTask
			parserTask.execute(result);
		}
	}
	
	
	/** A class to parse the Google Place Details in JSON format */
	private class ParserTask extends AsyncTask<String, Integer, HashMap<String,String>>{

		JSONObject jObject;
		
		// Invoked by execute() method of this object
		@Override
		protected HashMap<String,String> doInBackground(String... jsonData) {
		
			HashMap<String, String> hPlaceDetails = null;
			PlaceDetailsJSONParser placeDetailsJsonParser = new PlaceDetailsJSONParser();
        
	        try{
	        	jObject = new JSONObject(jsonData[0]);
	        	
	            // Start parsing Google place details in JSON format
	            hPlaceDetails = placeDetailsJsonParser.parse(jObject);
	            
	        }catch(Exception e){
	                Log.d("Exception",e.toString());
	        }
	        return hPlaceDetails;
		}
		
		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(HashMap<String,String> hPlaceDetails){			
			
			
			String name = hPlaceDetails.get("name");
			String icon = hPlaceDetails.get("icon");
			String lat = hPlaceDetails.get("lat");
			String lng = hPlaceDetails.get("lng");
			String formatted_address = hPlaceDetails.get("formatted_address");
			String website = hPlaceDetails.get("website");
			String rating = hPlaceDetails.get("rating");
			String international_phone_number = hPlaceDetails.get("international_phone_number");
			String url = hPlaceDetails.get("url");
			
			lati = lat;
			longi = lng;
			teli = international_phone_number;
			urli = url;
			webi = website;
				
			String mimeType = "text/html";
			String encoding = "utf-8";
			
			String data = 	"<html>"+							
							"<body><img style='float:left' src="+icon+" /><h3><center>"+name+"</center></h3>" +
							"<br style='clear:both' />" +
							"<hr  />"+
							"<p>Coordenadas : " + lat + "," + lng + "</p>" +
							"<p>Dirección : " + formatted_address + "</p>" +
							
							"<p>Puntuación Google: " + rating + "</p>" +
							"<p>Teléfono : <a href= 'tel:" + international_phone_number + "'>"+ international_phone_number +"</a></p>" +
							"<p>Sitio web : <a href='" + website +"'>" +website+ "</a></p>" +
							"<p>Google+  : <a href='" + url + "'/>" + url + "</a></p>" +			
							"</body></html>";
			
			// Setting the data in WebView
			mWvPlaceDetails.loadDataWithBaseURL("", data, mimeType, encoding, "");	
			
			Button htmlgo = (Button) findViewById(R.id.html_go);
			htmlgo.setOnClickListener(new View.OnClickListener() {
	             public void onClick(View v) {
	                  	
	                  Intent openNavigation = new Intent(Intent.ACTION_VIEW, 
		         				Uri.parse("google.navigation:ll=" + lati + "," + longi + "&mode=d"));
	                  startActivity(openNavigation);

	             }
	         });
			
			ImageButton speakButton = (ImageButton) findViewById(R.id.html_mic);
			// Disable button if no recognition service is present
	        PackageManager pm = getPackageManager();
	        List<ResolveInfo> activities = pm.queryIntentActivities(
	                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		        if (activities.size() == 0)
		        {
		            speakButton.setEnabled(false);
		            Toast.makeText(getBaseContext(), "Recognizer no presente", Toast.LENGTH_SHORT).show();
		        }
			
			
		}
	}
	/**
     * Handle the action of the button being clicked
     */
    public void speakButtonClicked(View v)
    {
   	 startVoiceRecognitionActivityPD();
	    
    }
 
    /**
     * Fire an intent to start the voice recognition activity.
     */
    private void startVoiceRecognitionActivityPD()
    {
    	
    	//start the speech recognition intent passing required data
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //set speech model
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //specify number of results to retrieve
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        //message to display while listening
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Las opciones son: \nLlamar, Sitio web y Google+");
        startActivityForResult(intent, REQUEST_CODEPD);
    }
    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODEPD && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String escucha = matches.get(0);
            //String escucha22 =matches.get(0);


            if(escucha.equals("navegación")||escucha.equals("ruta")||escucha.equals("ir al destino")
            		||escucha.equals("iniciar navegación")||escucha.equals("iniciar la navegación")
            		||escucha.equals("quiero iniciar la navegación")){
            	//Start navigation with native navigator app
            	Intent openNavigation = new Intent(Intent.ACTION_VIEW, 
         				Uri.parse("google.navigation:ll=" + lati + "," + longi + "&mode=d"));
            	startActivity(openNavigation);
            	
            }else if(escucha.equals("web")||escucha.equals("dirección web")||escucha.equals("página web")
            		||escucha.equals("sitio web")||escucha.equals("website")||escucha.equals("websait")
            		||escucha.equals("ver sitio web")||escucha.equals("ver el sitio web")||escucha.equals("quiero ver el sitio web")){
            	
            	if(!webi.equals("-NA-")){	
	            	//Open a web browser with the website
	            	Intent i = new Intent(Intent.ACTION_VIEW);
	            	i.setData(Uri.parse(webi));
	            	startActivity(i);
            	}else{
            		pdTts.speak("No se conoce la web de este establecimiento", TextToSpeech.QUEUE_FLUSH, null);
                	
            	}
				    
            }else if(escucha.equals("teléfono")||escucha.equals("llamar")||escucha.equals("llamar por teléfono")
            		||escucha.equals("quiero llamar")||escucha.equals("pues quiero llamar")){
            	
            	if(!teli.equals("-NA-")){
	            	//Dial the phone number with native phone app
	            	String uri = "tel:" + teli;
	            	Intent intent = new Intent(Intent.ACTION_DIAL);
	            	intent.setData(Uri.parse(uri));
	            	startActivity(intent);
            	}else{
            		pdTts.speak("No se conoce el teléfono de este establecimiento", TextToSpeech.QUEUE_FLUSH, null);
            	}
            	
            }else if(escucha.equals("google")||escucha.equals("google plus")||escucha.equals("google plas")
            		||escucha.equals("ver google+")||escucha.equals("ver google plas")||escucha.equals("google +")
            		||escucha.equals("ver google plus")||escucha.equals("ver el sitio de google")||escucha.equals("ver sitio google")
            		||escucha.equals("quiero ver el sitio de google")||escucha.equals("ver perfil")||escucha.equals("ver el perfil")){
            	if(!urli.equals("-NA-")){
            		//Open the Google+ site
	            	
	            	Intent i = new Intent(Intent.ACTION_VIEW);
	            	i.setData(Uri.parse(urli));
	            	startActivity(i);
            	}else{
            		pdTts.speak("No se conoce el sitio Google de este establecimiento", TextToSpeech.QUEUE_FLUSH, null);
            	}
            	
            }else if(escucha.equals("cancelar")||escucha.equals("atrás")||escucha.equals("volver atrás")||escucha.equals("ninguna")
            		||escucha.equals("salir")){
            }else{
            	pdTts.speak("por favor,¿Podría repetir?. Si desea salir, diga cancelar", TextToSpeech.QUEUE_FLUSH, null);
            	// SLEEP 5 SECONDS HERE ...
 			    Handler handler = new Handler(); 
 			    handler.postDelayed(new Runnable() { 
 			         public void run() { 
 			        	startVoiceRecognitionActivityPD();
 			         } 
 			    }, 5000);
            }
        }
    }


    /**
     * onInit fires when TTS initializes
     */
    public void onInit(int initStatus) {

    	Locale loc = new Locale ("spa", "ESP");

    	//if successful, set locale
        if (initStatus == TextToSpeech.SUCCESS)
        	pdTts.setLanguage(loc);
    }
}