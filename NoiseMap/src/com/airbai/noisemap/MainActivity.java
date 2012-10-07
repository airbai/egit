package com.airbai.noisemap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;
import android.support.v4.app.NavUtils;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.weibo.net.AccessToken;
import com.weibo.net.DialogError;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;

public class MainActivity extends MapActivity {
	public final static String EXTRA_MESSAGE = "com.airbai.noisemap.MESSAGE";
	public final static String AppName = "NoiseMap";
	private LocationManager locationManager;
	private Location currentLocation;
	private GeoPoint currentPoint;
	private MapController mapController;


    /** Called when the activity is first created. */
	private Button mLogin;
	private Button btnShareToMap;
	//private TextView mToken;

	private static final String URL_ACTIVITY_CALLBACK = "weiboandroidsdk://TimeLineActivity";
	private static final String FROM = "xweibo";
	
	private static final String CONSUMER_KEY = "187210693";
	private static final String CONSUMER_SECRET = "024942767c18021d0ed9febd5863aa20";
	
	private String username = "";
	private String password = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        TabHost tabHost=(TabHost)findViewById(R.id.tabhost);
        //LocalActivityManager mLocalActivityManager = new LocalActivityManager(this, false);
        tabHost.setup();

        TabSpec spec1=tabHost.newTabSpec("Tab 1");
        spec1.setContent(R.id.tab1);
        spec1.setIndicator("Tab 1");

        TabSpec spec2=tabHost.newTabSpec("Tab 2");
        Intent intent=new Intent(this.getBaseContext(), MapView.class);
        spec2.setIndicator("Tab 2");
        spec2.setContent(R.id.tab2);
        
        MapView mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.setSatellite(false);
        mapController = mapView.getController();
        mapController.setZoom(13);
        getLastLocation();
        animateToCurrentLocation();
        
        if(currentPoint != null){
        //mapView.getOverlays()用于得到所有图层对象
		List<Overlay> mapOverlays = mapView.getOverlays();
		
		Drawable drawable = this.getResources().getDrawable(R.drawable.ic_launcher);
		
		FirstOverlay firstOverlay = new FirstOverlay(drawable);
        OverlayItem overlayitem = new OverlayItem(currentPoint, "Hola, Mundo!","I'm in Mexico City!");  
        firstOverlay.addOverlay(overlayitem);
		
		mapOverlays.add(firstOverlay);
        }
        TabSpec spec3=tabHost.newTabSpec("Tab 3");
        spec3.setIndicator("Tab 3");
        spec3.setContent(R.id.tab3);

        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);

    		//mToken = (TextView) timeline.findViewById(R.id.tvToken);
    		mLogin = (Button) this.findViewById(R.id.btnShareToWeibo);
    		btnShareToMap = (Button)this.findViewById(R.id.btnShareToMap);
    		mLogin.setText("oauth!");
    		mLogin.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				if (v == mLogin) {
    					Weibo weibo = Weibo.getInstance();
    					weibo.setupConsumerConfig(CONSUMER_KEY, CONSUMER_SECRET);

    					// Oauth2.0
    					weibo.setRedirectUrl("http://1diantao.sinaapp.com/index.php/mobilemap");
    					weibo.authorize(MainActivity.this,
    							new AuthDialogListener());

    					// try {
    					// // Oauth2.0 璁よ瘉鏂瑰紡
    					// Weibo.setSERVER("https://api.weibo.com/2/");
    					// Oauth2AccessToken at =
    					// weibo.getOauth2AccessToken(AuthorizeActivity.this,
    					// Weibo.getAppKey(), Weibo.getAppSecret(), username,
    					// password);
    					// // xauth璁よ瘉鏂瑰紡
    					// /*
    					// * Weibo.setSERVER("http://api.t.sina.com.cn/");
    					// * AccessToken at =
    					// * weibo.getXauthAccessToken(TextActivity.this,
    					// * Weibo.APP_KEY, Weibo.APP_SECRET, "", "");
    					// * mToken.setText(at.getToken());
    					// */
    					// RequestToken requestToken =
    					// weibo.getRequestToken(AuthorizeActivity.this,
    					// Weibo.getAppKey(), Weibo.getAppSecret(),
    					// AuthorizeActivity.URL_ACTIVITY_CALLBACK);
    					// mToken.setText(requestToken.getToken());
    					// Uri uri =
    					// Uri.parse(AuthorizeActivity.URL_ACTIVITY_CALLBACK);
    					// startActivity(new Intent(Intent.ACTION_VIEW, uri));
    					//
    					// } catch (WeiboException e) {
    					// e.printStackTrace();
    					// } // mToken.setText(at.getToken());
    					//

    				}
    			}
    		});
    		btnShareToMap.setOnClickListener(new OnClickListener(){
    			public void onClick(View v) {
    				if (v == btnShareToMap) {
    						
    						new Thread(){
    							@Override 
    							public void run() {

    	    			            HttpClient client = Utility.getNewHttpClient(getApplicationContext());
    	    			            HttpUriRequest request = null;
    	    			            ByteArrayOutputStream bos = null;

    	    			            WeiboParameters params = new WeiboParameters();
    	    			            String url = "http://2.1diantao.sinaapp.com/index.php/pages/insertdb/";
    	    			            url = url + "39.888665/116.354776/111";
    				                HttpGet get = new HttpGet(url);
    				                request = get;
    	    			            HttpResponse response = null;
									try {
										response = client.execute(request);
									} catch (ClientProtocolException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
    	    			            StatusLine status = response.getStatusLine();
    	    			            int statusCode = status.getStatusCode();
    	    			            
    	    			            String result = "";
    	    			            if (statusCode != 200) {
    	    			                try {
											result = read(response);
										} catch (WeiboException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
    	    			                String err = null;
    	    			                int errCode = 0;
    	    							try {
    	    								JSONObject json = new JSONObject(result);
    	    								err = json.getString("error");
    	    								errCode = json.getInt("error_code");
    	    							} catch (JSONException e) {
    	    								e.printStackTrace();
    	    							}
    	    			            }
    	    			            // parse content stream from response
    	    			            try {
										result = read(response);
									} catch (WeiboException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
    				            
    							}
    						}.start();	
    				}
    			}
    		});
    	}
    
    private static String read(HttpResponse response) throws WeiboException {
        String result = "";
        HttpEntity entity = response.getEntity();
        InputStream inputStream;
        try {
            inputStream = entity.getContent();
            ByteArrayOutputStream content = new ByteArrayOutputStream();

            Header header = response.getFirstHeader("Content-Encoding");
            if (header != null && header.getValue().toLowerCase().indexOf("gzip") > -1) {
                inputStream = new GZIPInputStream(inputStream);
            }

            // Read response into a buffered stream
            int readBytes = 0;
            byte[] sBuffer = new byte[512];
            while ((readBytes = inputStream.read(sBuffer)) != -1) {
                content.write(sBuffer, 0, readBytes);
            }
            // Return result from buffered stream
            result = new String(content.toByteArray());
            return result;
        } catch (IllegalStateException e) {
            throw new WeiboException(e);
        } catch (IOException e) {
            throw new WeiboException(e);
        }
    }

    	public void onResume() {
    		super.onResume();
    	}

    	class AuthDialogListener implements WeiboDialogListener {

    		@Override
    		public void onComplete(Bundle values) {
    			String token = values.getString("access_token");
    			String expires_in = values.getString("expires_in");
//    			mToken.setText("access_token : " + token + "  expires_in: "
//    					+ expires_in);
    			AccessToken accessToken = new AccessToken(token, CONSUMER_SECRET);
    			accessToken.setExpiresIn(expires_in);
    			Weibo.getInstance().setAccessToken(accessToken);
    			Intent intent = new Intent();
    			intent.setClass(MainActivity.this, TestActivity.class);
    			startActivity(intent);
    		}

    		@Override
    		public void onError(DialogError e) {
    			Toast.makeText(getApplicationContext(),
    					"Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
    		}

    		@Override
    		public void onCancel() {
    			Toast.makeText(getApplicationContext(), "Auth cancel",
    					Toast.LENGTH_LONG).show();
    		}

    		@Override
    		public void onWeiboException(WeiboException e) {
    			Toast.makeText(getApplicationContext(),
    					"Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
    					.show();
    		}
    	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void sendMessage(View view){
        // Capture mono data at 16kHz
           int frequency = getValidSampleRates();
           
           if(frequency < 0) return;
           int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
           int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    
           // The minimal buffer size CANNOT be merely 20ms of data, it must be
           // at least 1024 bytes in this case, this is most likely due to a MMIO
           // hardware limit.
           final int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
    
           // Setup the audio recording machinery
           AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
           frequency, channelConfiguration,
           audioEncoding, bufferSize);
    
           // The short and file buffers, this might not be the most
           // efficient way to do things, but since we're planning on
           // redirecting this data into an encoder in a later version
           // of this project, we're not worried about it.
    
           // 320 = 16kHz * 20ms - Number of frames of audio required.
           short[] buffer = new short[320];
           byte[] fileBuffer = new byte[320 * 2];
           audioRecord.startRecording();
           
           FileOutputStream f = null;
           File sdCard = Environment.getExternalStorageDirectory();
           File dir = new File(sdCard.getAbsolutePath() + "/audioTest");
           dir.mkdirs();
           File file = new File(dir, "testAudio.wav");
           try {
               f = new FileOutputStream(file, true);
           } catch (FileNotFoundException e) {
               e.printStackTrace();
           }
    
           // Blocking loop uses about 40% of the CPU to do this.
           int sampleNumber = 0;
    
           // We'll capture 3000 samples of 20ms each,
           // giving us 60 seconds of audio data.
           while(sampleNumber < 3000) {
               audioRecord.read(buffer, 0, 320);
    
               for(int i = 0; i < buffer.length; i++) {
                   fileBuffer[i*2] = (byte)(buffer[i] & (short)0xFF);
                   fileBuffer[i*2 + 1] = (byte)(buffer[i] >> 8);
               }
    
               try {
                   f.write(fileBuffer);
               } catch (IOException e) {
                   e.printStackTrace();
               }
    
               sampleNumber++;
           }
    
           try {
               f.close();
           } catch (IOException e) {
               e.printStackTrace();
           }
           

       	Intent intent = new Intent(this, DisplayMessageActivity.class);
       	EditText editText = (EditText) findViewById(R.id.edit_message);
       	String message = editText.getText().toString();
       	intent.putExtra(EXTRA_MESSAGE, message);
       	startActivity(intent);
    

    }
    
    
    public int getValidSampleRates() {
    	int tempRate = -1;
        for (int rate : new int[] {8000, 11025, 16000, 22050, 44100}) {  // add the rates you wish to check against
            try
            {
	        	int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
	            if (bufferSize > 0) {
	                // buffer size is valid, Sample rate supported
	            	tempRate = rate;
	            	break;
	            }
            }
            catch(IllegalArgumentException ex)
            {
            	Log.i(AppName, ex.getMessage());
            }
        }
        
        return tempRate;
    }
    /**
     * Scan for the best configuration parameter for AudioRecord object on this device.
     * Constants value are the audio source, the encoding and the number of channels.
     * That means were are actually looking for the fitting sample rate and the minimum
     * buffer size. Once both values have been determined, the corresponding program
     * variable are initialized (audioSource, sampleRate, channelConfig, audioFormat)
     * For each tested sample rate we request the minimum allowed buffer size. Testing the
     * return value let us know if the configuration parameter are good to go on this
     * device or not.
     * 
     * This should be called in at start of the application in onCreate().
     * 
     * */
    public void initRecorderParameters(int[] sampleRates){

        for (int i = 0; i < sampleRates.length; ++i){
            try {
                Log.i(AppName, "Indexing "+sampleRates[i]+"Hz Sample Rate");
                int tmpBufferSize = AudioRecord.getMinBufferSize(sampleRates[i], 
                                AudioFormat.CHANNEL_IN_MONO,
                                AudioFormat.ENCODING_PCM_16BIT);

                // Test the minimum allowed buffer size with this configuration on this device.
                if(tmpBufferSize != AudioRecord.ERROR_BAD_VALUE){
                    // Seems like we have ourself the optimum AudioRecord parameter for this device.
                    AudioRecord tmpRecoder = new AudioRecord(MediaRecorder.AudioSource.MIC, 
                                                            sampleRates[i], 
                                                            AudioFormat.CHANNEL_IN_MONO,
                                                            AudioFormat.ENCODING_PCM_16BIT,
                                                            tmpBufferSize);
                    // Test if an AudioRecord instance can be initialized with the given parameters.
                    if(tmpRecoder.getState() == AudioRecord.STATE_INITIALIZED){
                        String configResume = "initRecorderParameters(sRates) has found recorder settings supported by the device:"  
                                            + "\nSource   = MICROPHONE"
                                            + "\nsRate    = "+sampleRates[i]+"Hz"
                                            + "\nChannel  = MONO"
                                            + "\nEncoding = 16BIT";
                        Log.i(AppName, configResume);

                        //+++Release temporary recorder resources and leave.
                        tmpRecoder.release();
                        tmpRecoder = null;

                        return;
                    }                   
                }else{
                    Log.w(AppName, "Incorrect buffer size. Continue sweeping Sampling Rate...");
                }
            } catch (IllegalArgumentException e) {
                Log.e(AppName, "The "+sampleRates[i]+"Hz Sampling Rate is not supported on this device");
            }
        }
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void getLastLocation(){
	    String provider = getBestProvider();
	    if(provider == null){
	    	return;
	    }
	    currentLocation = locationManager.getLastKnownLocation(provider);
	    if(currentLocation != null){
	        setCurrentLocation(currentLocation);
	    }
	    else
	    {
	        //Toast.makeText(this, "Location not yet acquired", Toast.LENGTH_LONG).show();
	    }
	}
	 
	public void animateToCurrentLocation(){
	    if(currentPoint!=null){
	        mapController.animateTo(currentPoint);
	    }
	}
	 
	public String getBestProvider(){
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    Criteria criteria = new Criteria();
	    criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
	    criteria.setAccuracy(Criteria.NO_REQUIREMENT);
	    String bestProvider = locationManager.getBestProvider(criteria, true);
	    return bestProvider;
	}
	 
	public void setCurrentLocation(Location location){
	    int currLatitude = (int) (location.getLatitude()*1E6);
	    int currLongitude = (int) (location.getLongitude()*1E6);
	    currentPoint = new GeoPoint(currLatitude,currLongitude);
	 
	    currentLocation = new Location("");
	    currentLocation.setLatitude(currentPoint.getLatitudeE6() / 1e6);
	    currentLocation.setLongitude(currentPoint.getLongitudeE6() / 1e6);
	}
}
