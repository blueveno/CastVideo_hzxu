package com.example.castvideo;
import java.io.IOException;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.castvideo.Player;
import com.example.castvideo.R;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.images.WebImage;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

/***
 * An activity that plays a chosen sample video on a Cast device and exposes
 * playback and volume controls in the UI.
 */
public class MainActivity extends ActionBarActivity 
			implements OnSharedPreferenceChangeListener {

	private static final String TAG = MainActivity.class.getSimpleName();

	public static final boolean ENABLE_LOGV = true;

	protected static final double MAX_VOLUME_LEVEL = 20;
	private static final double VOLUME_INCREMENT = 0.05;
	

	public static CastMedia mMedia;

	private CastDevice mSelectedDevice = null;
	public static MediaRouter mMediaRouter;
	private MediaRouteSelector mMediaRouteSelector;
	private ApplicationMetadata mAppMetadata;
	public static MediaRouteActionProvider mMediaRouteActionProvider;
	
    protected static Handler mHandler;
    
	private Cast.Listener mCastListener;
	private ConnectionCallbacks mConnectionCallbacks;
    private ConnectionFailedListener mConnectionFailedListener;
    public static GoogleApiClient mApiClient;
    public static RemoteMediaPlayer mRemoteMediaPlayer;
	
	public static MediaRouter.Callback mMediaRouterCallback;


	public static Player player;
	public static TabhostContent tabhostcontent;
	public static boolean readyCast = false;
	private mediaPlayer mediaplayer;
	private Context context = this;
	private TextView mTitle;
	public static String APP_ID;
	UpdateStatusThread myThread = null;
	
	
	public static List<CastMedia> myVideos;
	public static List<EVideo> mvideos = new ArrayList<EVideo>();
	public static List<EVideo> maudios = new ArrayList<EVideo>();
	public static List<EVideo> mphotos = new ArrayList<EVideo>();
	public static List<EVideo> mediaHistory = new ArrayList<EVideo>();
	
	
	public static ProgressDialog ringProgressDialog;
	public static MyDialog myDialog;
	public static boolean fg_localplayback;
	
	public static int repeat_mode;
    private boolean mWaitingForReconnect;
    public static List<String> groupArray = new  ArrayList<String>();  
	public static List<List<EVideo>> childArray = new  ArrayList<List<EVideo>>();
	public static AnalyzeMP4Url mAnalyzeMP4Url;
	private String cast_des;
	private long cast_curr_time = System.currentTimeMillis();
	
	
	
	public static boolean fgReadyCast(){
		//need porting
		if (readyCast) {
			return true;
		}
		myDialog.ConfirmAlertDlg("Warnning", "Chromecast not connected, Please try to reconnect it", "OK", null);
		return false;
	}
	public static boolean fg_devices_ready(){
		List<RouteInfo> routes = mMediaRouter.getRoutes();
		for (int location = 0; location < routes.size(); location ++){
			if (routes.get(location) != null && routes.get(location).getDescription() != null){
				return true;
			}
			Log.d(TAG, "route:"+routes.get(location));
			Log.d(TAG, "routes number "+routes.size());
		}
		return false;
	}
	
	
	public static void AddHistory(EVideo media_history){
		for (int i = 0; i<mediaHistory.size(); i++){
			if (media_history.getTitle().equals(mediaHistory.get(i).getTitle())){
				//sort
				mediaHistory.remove(i);
				break ;
			}
		}
		mediaHistory.add(0, media_history);
	}
	
	public void UpdateHistoryInCfg(){
		/*UrlHistory urlHistory = null;*/
		EVideo mediahistory_temp = null;

		SharedPreferences share = getSharedPreferences("config", MODE_PRIVATE);  
        Editor editor = share.edit();  
        editor.clear();
		for (int i=0; i<mediaHistory.size(); i++){
			mediahistory_temp = mediaHistory.get(i);
	        editor.putString(String.format("url_%d", i), mediahistory_temp.getPath());
	        editor.putString(String.format("title_%d", i), mediahistory_temp.getTitle());
	        editor.putString(String.format("type_%d", i), String.format("%s", mediahistory_temp.fg_local()));
	        editor.putString(String.format("size_%d", i), mediahistory_temp.getSize());
	        editor.putString(String.format("duration_%d", i), mediahistory_temp.getDuration());
	        editor.putString(String.format("mimetype_%d", i), mediahistory_temp.getType());
	        editor.putString(String.format("format_%d", i), ""+mediahistory_temp.getFormat());
	        editor.putString(String.format("fav_%d", i), String.format("%s", mediahistory_temp.fg_fav()));
	        logVIfEnabled(TAG, String.format("save config url:%s title:%s type:%s", 
	        		mediahistory_temp.getPath(), 
	        		mediahistory_temp.getTitle(), 
	        		mediahistory_temp.getType()));
		}  
        editor.commit(); 
	}
	
	public void GetHistoryInCfg(){
		String url, title, type, size, duration, mimetype, format, fav;
		EVideo mediahistory_temp;
		mediaHistory.clear();
		SharedPreferences share = getSharedPreferences("config", MODE_PRIVATE);
		for (int i=0; ;i++){
			url = share.getString(String.format("url_%d", i), null);
			title = share.getString(String.format("title_%d", i), null);
			type = share.getString(String.format("type_%d", i), null);
			size = share.getString(String.format("size_%d", i), null);
			duration = share.getString(String.format("duration_%d", i), null);
			mimetype = share.getString(String.format("mimetype_%d", i), null);
			format = share.getString(String.format("format_%d", i), null);
			fav = share.getString(String.format("fav_%d", i), null);
			logVIfEnabled(TAG, String.format("get from config url:%s title:%s type:%s", url, title, type));
			if (url != null && url != "" && title != null && title != ""){
				if (type.equals("true")){
					mediahistory_temp = new EVideo(true);
				}else{
					mediahistory_temp = new EVideo(false);
				}
				
				if (fav.equals("true")){
					mediahistory_temp.set_fav(true);
				}else {
					mediahistory_temp.set_fav(false);
				}
				mediahistory_temp.setPath(url);
				mediahistory_temp.setTitle(title);
				mediahistory_temp.setType(mimetype);
				mediahistory_temp.setSize(size);
				mediahistory_temp.setDuration(duration);
				mediahistory_temp.setFormat(Integer.parseInt(format));
				mediaHistory.add(mediahistory_temp);
			}else{
				break;
			}
		}
	}
	
	
	public Handler handler = new Handler() {  
        //处理消息  
        @Override  
        public void handleMessage(Message msg) {  
            // TODO Auto-generated method stub  
            super.handleMessage(msg);  
            String cmd = msg.obj.toString();
            logVIfEnabled(TAG, String.format("Get msg:%s", cmd));
            if (msg.arg1 == 0){
            	myDialog.ConfirmAlertDlg("Error", "Analyze url failed!!!\r\n"+cmd, "OK", null);
                if (ringProgressDialog != null){
                	ringProgressDialog.dismiss();
                	ringProgressDialog = null;
                }
            	return;
            }
            //Toast.makeText(MainActivity.this, cmd, Toast.LENGTH_SHORT).show();
            if (fgReadyCast()){
            	player.ConfirmCastUrlVideoDlg();
            }
            if (ringProgressDialog != null){
            	ringProgressDialog.dismiss();
            	ringProgressDialog = null;
            }
            tabhostcontent.setCurrentTab(0);
        }    
    };  
    

    /**
     * Called when the options menu is first created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        mMediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mMediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
        return true;
    }    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    
 
    protected void route_init(String app_id){
    	mMedia = null;
    	mMediaRouter = MediaRouter.getInstance(getApplicationContext());
    	
    	
    	mMediaRouteSelector = new MediaRouteSelector.Builder()
		    	.addControlCategory(CastMediaControlIntent.categoryForCast(app_id))
		    	.build();
    	mMediaRouterCallback = new MyMediaRouterCallback();
    	
    	mCastListener = new CastListener();
    	mConnectionCallbacks = new ConnectionCallbacks();
        mConnectionFailedListener = new ConnectionFailedListener();
        
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);		
    }    
 
    public void update_th_destroy() {
    	if (myThread != null){
    		while (myThread.isAlive()){
    			myThread.stopThread();
    			try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			logVIfEnabled(TAG, String.format("update thread status:%s", myThread.isAlive()));
    		}
    	}
    }
    public void update_th_init(){
    	myThread = new UpdateStatusThread();
    	myThread.start();
    	logVIfEnabled(TAG, "Starting statusRunner thread");
    }
   
	/**
	 * Initializes MediaRouter information and prepares for Cast device
	 * detection upon creating this activity.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		logVIfEnabled(TAG, "onCreate called");
		mHandler = new Handler();
		try {
			NanoHTTPD mhttpServer = new NanoHTTPD();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (myVideos == null) {
			myVideos = new ArrayList<CastMedia>();
		}
		
		APP_ID = getResources().getString(R.string.app_name);
		

		SharedPreferences share = getSharedPreferences("config", MODE_PRIVATE);  
        String value = share.getString("APP_ID", null);  
        logVIfEnabled(TAG, "get from config.xml"+value);

        if (value == null || value == ""){
        	Editor editor = share.edit();  
            editor.putString("APP_ID", APP_ID);  
            editor.commit(); 
        }else{
        	APP_ID = value;
        }
		route_init(APP_ID);

		mAnalyzeMP4Url = new AnalyzeMP4Url(this, handler);
		mediaplayer = new mediaPlayer(getApplicationContext());
		player = new Player(this, mediaplayer);
		tabhostcontent = new TabhostContent(this, handler);
				
		mTitle = (TextView) this.findViewById(R.id.title);
		update_th_destroy();
		update_th_init();

		//mLocalFileAdapter = new LocalMediaAdapter(MainActivity.this, LoadType.UPLOAD);
		logVIfEnabled(TAG, "Adapter init done");
		GetHistoryInCfg();
		
		
		shareURL();
		myDialog = new MyDialog(context);
		

		LocalMedia_Collect localmedia_collect = new LocalMedia_Collect(this);
		localmedia_collect.collect();
	}
	

	void handleSendText(String sharedText, String title) {
		if (sharedText != null) {
			sharedText = sharedText.replace("\n", "");
			sharedText = sharedText.replace("\r", "");
			Log.i(TAG, String.format("sharedText:\"%s\"", sharedText));
			Pattern p = Pattern
					.compile(".*(http://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?).*");
			Matcher m = p.matcher(sharedText);
			if (m.matches()) {
				String link = m.group(1);
				Log.i("S2C", "Get Link:" + link + "\n");
				link =link.replace("m.pptv.com", "v.pptv.com");
				Toast toast = Toast.makeText(getApplicationContext(),
						"What you shared :"+link,
						Toast.LENGTH_SHORT);
				toast.show();
				mAnalyzeMP4Url.GetMP4Addr(link, title);
				if (ringProgressDialog != null){ringProgressDialog.dismiss();}

				ringProgressDialog = ProgressDialog.show(MainActivity.this, 
						"Please wait ...", 
						String.format("Progress %s;\r\nurl:\"%s\" ...", title, link), true);
		        ringProgressDialog.setCancelable(false);
			} else {
				Toast toast = Toast.makeText(getApplicationContext(),
						"What you shared does not contain a link",
						Toast.LENGTH_SHORT);
				toast.show();
				Log.i("TAG", "Can not find a link!\n");
			}
		}
	}


	public void onSetVolume(double volume) {
		mediaplayer.onSetVolume(volume);
	}
	
	public double onGetVolume(){
		return mediaplayer.onGetVolume();
	}
	
	/**
	 * Processes volume up and volume down actions upon receiving them as key
	 * events.
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int action = event.getAction();
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (action == KeyEvent.ACTION_DOWN) {
				double currentVolume;
				if (readyCast) {
					currentVolume = onGetVolume();
					if (currentVolume < 1.0) {
						logVIfEnabled(TAG, "New volume: " + (currentVolume + VOLUME_INCREMENT));
						onSetVolume(currentVolume + VOLUME_INCREMENT);
					}
				} else {
					Log.e(TAG, "dispatchKeyEvent - volume up - mMPMS==null");
				}
			}

			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (action == KeyEvent.ACTION_DOWN) {
				double currentVolume;
				if (readyCast) {
					currentVolume = onGetVolume();
					if (currentVolume > 0.0) {
						logVIfEnabled(TAG, "New volume: " + (currentVolume - VOLUME_INCREMENT));
						onSetVolume(currentVolume - VOLUME_INCREMENT);
					}
				} else {
					Log.e(TAG, "dispatchKeyEvent - volume down - mMPMS==null");
				}
			}
			return true;
		default:
			return super.dispatchKeyEvent(event);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
				MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
		logVIfEnabled(TAG, "onStart called and callback added");
	}

	/**
	 * Closes a running session upon destruction of this Activity.
	 */
	@Override
	protected void onStop() {
		mMediaRouter.removeCallback(mMediaRouterCallback);
		UpdateHistoryInCfg();
		super.onStop();
		logVIfEnabled(TAG, "onStop called and callback removed");
	}

	@Override
	protected void onDestroy() {	
		UpdateHistoryInCfg();
		//logVIfEnabled(TAG, "onDestroy called, ending session if session exists");	
		PreferenceManager.getDefaultSharedPreferences(this)
        	.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	/**
	 * A callback class which listens for route select or unselect events and
	 * processes devices and sessions accordingly.
	 */
	private class MyMediaRouterCallback extends MediaRouter.Callback {
		@Override
		public void onRouteAdded(MediaRouter router, RouteInfo route){
			final RouteInfo route_temp=route;
			Log.d(TAG, "Add route=" + route);
			if ( mMediaRouter.getSelectedRoute() != null && mMediaRouter.getSelectedRoute().getDescription() != null ){
				return;
			}
			MainActivity.mMediaRouteActionProvider.getMediaRouteButton().performClick();
		}
		@Override
		public void onRouteRemoved(MediaRouter router, RouteInfo route){
			Log.d(TAG, "Remove route=" + route);
		}
		@Override
		public void onRouteSelected(MediaRouter router, RouteInfo route) {
			Log.d(TAG, "onRouteSelected: route=" + route);
	        CastDevice device = CastDevice.getFromBundle(route.getExtras());
	        setSelectedDevice(device);
		}

		@Override
		public void onRouteUnselected(MediaRouter router, RouteInfo route) {
			Log.d(TAG, "onRouteUnselected: route=" + route);
	        setSelectedDevice(null);
	        mAppMetadata = null;
	        clearMediaState();
		}
	}

    /**
     * Updates the currently-playing-item metadata display. If the image URL is non-null and is
     * different from the one that is currently displayed, an asynchronous request will be started
     * to fetch the image at that URL.
     */
    protected final void setCurrentMediaMetadata(String title, String subtitle, Uri imageUrl) {
        Log.d(TAG, "setCurrentMediaMetadata: " + title + "," + subtitle + "," + imageUrl);
    }
	private void clearMediaState() {
        setCurrentMediaMetadata(null, null, null);
    }

	/**
	 * Updates the status of the currently playing video in the dedicated
	 * message view.
	 */
	public void updateStatus() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					updateCurrentlyPlaying();

				} catch (Exception e) {
					Log.e(TAG, "Status request failed: " + e);
				}
			}
		});
	}

	/**
	 * Updates a view with the title of the currently playing media.
	 */
	protected void updateCurrentlyPlaying() {
		String des = mMediaRouter.getSelectedRoute().getDescription();
		if (mMediaRouter.getSelectedRoute().getDescription() != null){
			mTitle.setText(Html.fromHtml(String.format("%s<br><font color=#0066FF>(%s)%s</font>", 
					getString(R.string.title_name),
					mMediaRouter.getSelectedRoute().getName(),
					des)));
		}
		
		if (cast_des != null && cast_des.equals(des) && System.currentTimeMillis() - cast_curr_time > 5000){
			cast_des = null;
			if (fg_localplayback && mMedia.getType() == 2){
				player.play_next_localmedia();
			}
		
		}else if (cast_des == null || cast_des.equals(des) == false){
			cast_des = mMediaRouter.getSelectedRoute().getDescription();
			cast_curr_time = System.currentTimeMillis();
		}
		
		
	}
	
	private class UpdateStatusThread extends  Thread {
		private boolean  _run  = true;
	    public void stopThread( ) {
	         this._run = false;
	    }
	    @Override
	    public void  run() {
	    	while (this._run){
	    		try {
					updateStatus();
					Thread.sleep(1500);
				} catch (Exception e) {
					Log.e(TAG, "Thread interrupted: " + e);
				}
	    	}
	    }
	    
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(true);  
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	

	/**
	 * Logs in verbose mode with the given tag and message, if the LOCAL_LOGV
	 * tag is set.
	 */
	public static void logVIfEnabled(String tag, String message) {
		if (ENABLE_LOGV) {
			Log.v(tag, message);
		}
	}
	private void shareURL(){
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		Log.i("S2C", String.format("action:%s type:%s getCategories:%s package:%s subject:%s", 
				action, 
				type,
				intent.getCategories(),
				intent.getPackage(),
				intent.getStringExtra(Intent.EXTRA_SUBJECT)));
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			Log.i("S2C", String.format("from share;action:%s type:%s getCategories:%s package:%s", 
					action, 
					type,
					intent.getCategories(),
					intent.getPackage()));
			if ("text/plain".equals(type)) {
				handleSendText(intent.getStringExtra(Intent.EXTRA_TEXT), intent.getStringExtra(Intent.EXTRA_SUBJECT));
			}
		} else {
			Log.i("S2C", "not from share");
		}
	}
	
	public void shareURLandCast(){
		if (fg_devices_ready() == false){
			//could not find any devices
			myDialog.ConfirmAlertDlg("Warnning", "Could not find any valid devices!!!", "OK", null);
			return;
		}
		if ( mMediaRouter.getSelectedRoute() != null && mMediaRouter.getSelectedRoute().getDescription() != null ){
			shareURL();
			return;
		}
		Log.d(TAG, "select route"+mMediaRouter.getSelectedRoute());
		MainActivity.mMediaRouteActionProvider.getMediaRouteButton().performClick();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		setIntent(intent);// must store the new intent unless getIntent() will
							// return the old one
		shareURL();
	}
	
	private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
		@Override
		public void onConnectionSuspended(int cause) {
			Log.d(TAG, "ConnectionCallbacks.onConnectionSuspended");
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					// TODO: need to disable all controls, and possibly display a
					// "reconnecting..." dialog or overlay
					detachMediaPlayer();
					//updateButtonStates();
					mWaitingForReconnect = true;
				}
			});
		}

		@Override
		public void onConnected(final Bundle connectionHint) {
			Log.d(TAG, "ConnectionCallbacks.onConnected");
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (mApiClient == null) {
						return;
					}
					try {
						Cast.CastApi.requestStatus(mApiClient);
					} catch (IOException e) {
						Log.d(TAG, "error requesting status", e);
					}

					if (mWaitingForReconnect) {
						mWaitingForReconnect = false;
						if ((connectionHint != null)
								&& connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
							Log.d(TAG, "App  is no longer running");
							mAppMetadata = null;
							clearMediaState();
						} else {
							reattachMediaPlayer();
						}
					}else{
						
					}
					Cast.CastApi.launchApplication(mApiClient, 
							CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID, 
			        		false).setResultCallback(new ApplicationConnectionResultCallback("LaunchApp"));
					readyCast = true;
				}
			});
		}
    }
	

    private void attachMediaPlayer() {
        if (mRemoteMediaPlayer != null) {
            return;
        }
        mRemoteMediaPlayer = new RemoteMediaPlayer();
        mRemoteMediaPlayer.setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {
            @Override
            public void onStatusUpdated() {
                Log.d(TAG, "MediaControlChannel.onStatusUpdated:"+mRemoteMediaPlayer.getMediaStatus());
                if (mRemoteMediaPlayer.getMediaStatus() != null){
                	Log.i(TAG, "status:"+mRemoteMediaPlayer.getMediaStatus());
                }
                if (mediaplayer.fg_seeking() || mediaplayer.fg_loading()){
                	return;
                }
                // If item has ended, clear metadata.
                MediaStatus mediaStatus = mRemoteMediaPlayer.getMediaStatus();
                if (mediaStatus != null
                        && (mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_IDLE)) {
                    clearMediaState();
                    if (fg_localplayback == false && mediaplayer.start_next() == false) {
						player.stop();						
                    }
                    if (fg_localplayback && mMedia.getType() != 2){
                    	mHandler.post(new Runnable() {
            				@Override
            				public void run() {
            					try {
									Thread.sleep(5000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
            					player.play_next_localmedia();
            				}
            			});
                    	
                    }
                }
            }
        });

        mRemoteMediaPlayer.setOnMetadataUpdatedListener(
                new RemoteMediaPlayer.OnMetadataUpdatedListener() {
            @Override
            public void onMetadataUpdated() {
                Log.d(TAG, "MediaControlChannel.onMetadataUpdated");
                String title = null;
                String artist = null;
                Uri imageUrl = null;
                

                if (mRemoteMediaPlayer.getMediaInfo() != null && mRemoteMediaPlayer.getMediaInfo().getCustomData() != null){
                	Log.i(TAG, "Get custom data:"+mRemoteMediaPlayer.getMediaInfo().getCustomData().toString());
                }

                MediaInfo mediaInfo = mRemoteMediaPlayer.getMediaInfo();
                if (mediaInfo != null) {
                    MediaMetadata metadata = mediaInfo.getMetadata();
                    if (metadata != null) {
                        title = metadata.getString(MediaMetadata.KEY_TITLE);
                        artist = metadata.getString(MediaMetadata.KEY_ARTIST);
                        if (artist == null) {
                            artist = metadata.getString(MediaMetadata.KEY_STUDIO);
                        }

                        List<WebImage> images = metadata.getImages();
                        if ((images != null) && !images.isEmpty()) {
                            WebImage image = images.get(0);
                            imageUrl = image.getUrl();
                        }
                    }
                    setCurrentMediaMetadata(title, artist, imageUrl);
                }
            }
        });

        try {
            Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
        } catch (IOException e) {
            Log.w(TAG, "Exception while launching application", e);
        }
    }
    

    private final class ApplicationConnectionResultCallback implements
            ResultCallback<Cast.ApplicationConnectionResult> {
        private final String mClassTag;

        public ApplicationConnectionResultCallback(String suffix) {
            mClassTag = TAG + "_" + suffix;
        }

        @Override
        public void onResult(ApplicationConnectionResult result) {
            Status status = result.getStatus();
            Log.d(mClassTag, "ApplicationConnectionResultCallback.onResult: statusCode" + status.getStatusCode());
            if (status.isSuccess()) {
                ApplicationMetadata applicationMetadata = result.getApplicationMetadata();
                String sessionId = result.getSessionId();
                String applicationStatus = result.getApplicationStatus();
                boolean wasLaunched = result.getWasLaunched();
                Log.d(mClassTag, "application name: " + applicationMetadata.getName()
                        + ", status: " + applicationStatus + ", sessionId: " + sessionId
                        + ", wasLaunched: " + wasLaunched);
                attachMediaPlayer();
                mAppMetadata = applicationMetadata;
                Log.d(mClassTag, "requesting current media status");
                mRemoteMediaPlayer.requestStatus(mApiClient).setResultCallback(
                		new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                			@Override
                			public void onResult(MediaChannelResult result) {
                				Status status = result.getStatus();
                				if (!status.isSuccess()) {
                					Log.w(mClassTag, "Unable to request status: " + status.getStatusCode());
                				}
                			}
                		});

            } else {
                myDialog.ConfirmAlertDlg("Error", "Lanuch application failed;");
            }
        }
    }


	private class CastListener extends Cast.Listener {
        @Override
        public void onApplicationDisconnected(int statusCode) {
            Log.d(TAG, "Cast.Listener.onApplicationDisconnected: " + statusCode);
            mAppMetadata = null;
            detachMediaPlayer();
            clearMediaState();
        }
    }
	
    private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.d(TAG, "onConnectionFailed");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    clearMediaState();
                    myDialog.ConfirmAlertDlg("Error", "error no device connect");
                }
            });
        }
    }

    private void reattachMediaPlayer() {
        if ((mRemoteMediaPlayer != null) && (mApiClient != null)) {
            try {
                Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
            } catch (IOException e) {
                Log.w(TAG, "Exception while launching application", e);
            }
        }
    }
    private void detachMediaPlayer() {
        if ((mRemoteMediaPlayer != null) && (mApiClient != null)) {
            try {
                Cast.CastApi.removeMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace());
            } catch (IOException e) {
                Log.w(TAG, "Exception while launching application", e);
            }
        }
        mRemoteMediaPlayer = null;
    }
    private void setSelectedDevice(CastDevice device) { 
    	mSelectedDevice = device;
	    if (mSelectedDevice == null) {
	        detachMediaPlayer();
	        if ((mApiClient != null) && mApiClient.isConnected()) {
	            mApiClient.disconnect();
	        }
	        mApiClient = null;
	    } else {
	        Log.d(TAG, "acquiring controller for " + mSelectedDevice);
	        try {
	            Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(
	                    mSelectedDevice, mCastListener);
	            apiOptionsBuilder.setDebuggingEnabled();
	
	            mApiClient = new GoogleApiClient.Builder(this)
	                    .addApi(Cast.API, apiOptionsBuilder.build())
	                    .addConnectionCallbacks(mConnectionCallbacks)
	                    .addOnConnectionFailedListener(mConnectionFailedListener)
	                    .build();
	            mApiClient.connect();
	        } catch (IllegalStateException e) {
	            Log.w(TAG, "error while creating a device controller", e);
	            myDialog.ConfirmAlertDlg("Error", "error while creating a device controller");
	        }
	        
	    }
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		
	}
}
