package com.example.castvideo;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

public class TabhostContent {
	private static final String TAG = TabhostContent.class.getSimpleName();
	private Activity mActivity;
	private SettingAdapter mSettingAdapter;
	public MediaAdapter mUrlAdapter;
	//public UrlContentExAdapter mUrlAdapter;
	private TabHost tabHost;
	private String change_app_id_str="change APP ID", clear_history_str="clear history", play_from_url_str="play from URL";
	private static List<String> msetting =  new ArrayList<String>();
	Handler mHandler;
	private Player mplayer;
	public TabhostContent(Activity activity, Handler handler){
		mActivity = activity;
		mHandler = handler;
		mplayer = MainActivity.player;
    	msetting.add(change_app_id_str);
    	msetting.add(clear_history_str);
    	msetting.add(play_from_url_str);
    	
		mSettingAdapter = new SettingAdapter(mActivity, msetting);
		mUrlAdapter = new MediaAdapter(mActivity);
		//mUrlAdapter = new UrlContentExAdapter(mActivity);
		tabhost_init();
	}
/

	public void chk_select_device(){		
		if ( (MainActivity.mMediaRouter.getSelectedRoute() != null && MainActivity.mMediaRouter.getSelectedRoute().getDescription() != null)
				|| MainActivity.fg_devices_ready() == false){
			return;
		}
		Log.d(TAG, "select route"+MainActivity.mMediaRouter.getSelectedRoute());
		/*final DeviceAdapter deviceadapter = new DeviceAdapter(mActivity, MainActivity.mMediaRouter.getRoutes());	
		AlertDialog.Builder  malertdialog = new AlertDialog.Builder(mActivity).
				setAdapter(deviceadapter, new DialogInterface.OnClickListener(){                                        
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	RouteInfo route = (RouteInfo) deviceadapter.getItem(which);
                    	MainActivity.mMediaRouter.selectRoute(route);
                    }
				});
		malertdialog.setTitle("Select a device to cast");
		malertdialog.show();*/
		MainActivity.mMediaRouteActionProvider.getMediaRouteButton().performClick();
	}	

    public void play_from_history(){    	    	
    	MainActivity.groupArray.clear();
    	MainActivity.childArray.clear();
    	MainActivity.groupArray.add("video" );
    	MainActivity.groupArray.add("audio" );
    	MainActivity.groupArray.add("photo" ); 

    	List<EVideo> videoArray = new ArrayList<EVideo>();
    	for(int location = 0; location < MainActivity.mediaHistory.size(); location ++){
    		if (MainActivity.mediaHistory.get(location).getFormat() == 0){
    			videoArray.add(MainActivity.mediaHistory.get(location));
    		}
    	}
    	List<EVideo> audioArray = new ArrayList<EVideo>();
    	for(int location = 0; location < MainActivity.mediaHistory.size(); location ++){
    		if (MainActivity.mediaHistory.get(location).getFormat() == 1){
    			audioArray.add(MainActivity.mediaHistory.get(location));
    		}
    	}
    	List<EVideo> photoArray = new ArrayList<EVideo>();
    	for(int location = 0; location < MainActivity.mediaHistory.size(); location ++){
    		if (MainActivity.mediaHistory.get(location).getFormat() == 2){
    			photoArray.add(MainActivity.mediaHistory.get(location));
    		}
    	}

    	MainActivity.childArray.add(videoArray);  
    	MainActivity.childArray.add(audioArray);  
    	MainActivity.childArray.add(photoArray);  


    	final LocalMediaExAdapter historyAdapter = new LocalMediaExAdapter(mActivity, true);
    	ExpandableListView expandableListView = (ExpandableListView) mActivity.findViewById(R.id.media_from_history_list);
    	expandableListView.setAdapter(historyAdapter);	
    	expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener(){

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				EVideo eVideo = historyAdapter.getChild(groupPosition, childPosition);
                if (eVideo == null) {
                    return true;
                }
                mplayer.play_media(eVideo);
				return false;
			}    		
    	});        
    }
    

    public void play_from_local(){
    	MainActivity.groupArray.clear();
    	MainActivity.childArray.clear();
    	MainActivity.groupArray.add("video" );
    	MainActivity.groupArray.add("audio" );
    	MainActivity.groupArray.add("photo" ); 

    	List<EVideo> videoArray = new ArrayList<EVideo>();
    	for(int location = 0; location < MainActivity.mvideos.size(); location ++){
    		videoArray.add(MainActivity.mvideos.get(location));
    	}
    	List<EVideo> audioArray = new ArrayList<EVideo>();
    	for(int location = 0; location < MainActivity.maudios.size(); location ++){
    		audioArray.add(MainActivity.maudios.get(location));  		
    	}
    	List<EVideo> photoArray = new ArrayList<EVideo>();
    	for(int location = 0; location < MainActivity.mphotos.size(); location ++){
    		photoArray.add(MainActivity.mphotos.get(location));
    	}

    	MainActivity.childArray.add(videoArray);  
    	MainActivity.childArray.add(audioArray);  
    	MainActivity.childArray.add(photoArray);  


    	final LocalMediaExAdapter historyAdapter = new LocalMediaExAdapter(mActivity, false);
    	ExpandableListView expandableListView = (ExpandableListView) mActivity.findViewById(R.id.media_from_local_list);
    	expandableListView.setAdapter(historyAdapter);	
    	expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				EVideo eVideo = historyAdapter.getChild(groupPosition, childPosition);
                if (eVideo == null) {
                    return true;
                }
                
                mplayer.play_media(eVideo);
				return false;
			}
		} );
    }


	public void tabhost_url_content_update(){
		ListView mediaListView = (ListView) mActivity.findViewById(R.id.media_from_url_list);
        mediaListView.setAdapter(mUrlAdapter);
		mediaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> a, View v, int position, long id) {
	        	CastMedia urlMedia = mUrlAdapter.getItem(position);
	        	if (urlMedia == null) {
	            	return;
	        	}
				if (urlMedia.equals(MainActivity.mMedia)){
					Toast.makeText(mActivity, String.format("%s has been cast", MainActivity.mMedia.getTitle()), Toast.LENGTH_SHORT).show();
					return;
				}
	        	MainActivity.logVIfEnabled(TAG, String.format("Get %d media; title:%s url:%s filename:%s", position, urlMedia.getTitle(), urlMedia.getUrl(), urlMedia.getFileName()));
	        	MainActivity.mMedia = urlMedia;
	        	MainActivity.player.playUrl();
	        	//mUrlAdapter.setSelect(v, position);
	        }
        } );
		/*ExpandableListView mediaListView = (ExpandableListView) mActivity.findViewById(R.id.media_from_url_list);
		
        mediaListView.setAdapter(mUrlAdapter);	
        mediaListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
	        	CastMedia urlMedia =MainActivity.myVideos.get(groupPosition);
	        	urlMedia.setCurrMedia(childPosition);
	        	MainActivity.logVIfEnabled(TAG, String.format("Get %d media; title:%s", groupPosition, urlMedia.getTitle()));
	        	MainActivity.mMedia = urlMedia;
	        	MainActivity.player.playUrl();
	        	return false;
			}
		} );*/
        
	}
    public void setting(){
    	ListView listViewtemp = (ListView) mActivity.findViewById(R.id.setting);
    	listViewtemp.setAdapter(mSettingAdapter);	
    	listViewtemp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> a, View v,
	                int position, long id) {
	        	if (mSettingAdapter.getItem(position).equals(change_app_id_str)){
	        		chg_APP_ID();
	        	}else if (mSettingAdapter.getItem(position).equals(clear_history_str)){
	        		clear_url_history();
	        	}else if (mSettingAdapter.getItem(position).equals(play_from_url_str)){
	        		MainActivity.player.play_from_url();
	        	}
	            
	        }
	    } );    
    }

    public void setCurrentTab(int idx){
    	tabHost.setCurrentTab(idx);
    }
	public void tabhost_init(){
		MainActivity.fg_localplayback = false;
    	tabHost=(TabHost)mActivity.findViewById(R.id.tabhost);
		tabHost.setup();

		TabSpec spec1=tabHost.newTabSpec("tab_play_from_url");
		spec1.setContent(R.id.tab_play_from_url);
		spec1.setIndicator("Media List",null);

		TabSpec spec2=tabHost.newTabSpec("tab_play_from_history");
		spec2.setContent(R.id.tab_play_from_history);
		spec2.setIndicator("History",null);

		TabSpec spec3=tabHost.newTabSpec("tab_play_from_local");
		spec3.setContent(R.id.tab_play_from_local);
		spec3.setIndicator("Local Playback",null);
		

		TabSpec spec4=tabHost.newTabSpec("tab_setting");
		spec4.setContent(R.id.tab_setting);
		spec4.setIndicator("Tools",null);


		tabHost.addTab(spec1);
		tabHost.addTab(spec2);
		tabHost.addTab(spec3);
		tabHost.addTab(spec4);

		tabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String arg0) {
				if (arg0.equals("tab_play_from_url")){
					MainActivity.logVIfEnabled(TAG, String.format("Play from url :%s", arg0));
					chk_select_device();
					tabhost_url_content_update();
				}else if  (arg0.equals("tab_play_from_history")){
					MainActivity.logVIfEnabled(TAG, String.format("Play from history :%s", arg0));
					chk_select_device();
					play_from_history();
				}else if (arg0.equals("tab_play_from_local")){
					MainActivity.logVIfEnabled(TAG, String.format("Play from local :%s", arg0));	
					chk_select_device();
					play_from_local();
				}else if (arg0.equals("tab_setting")){
					MainActivity.logVIfEnabled(TAG, String.format("setting :%s", arg0));		
					setting();
				}else{
					MainActivity.logVIfEnabled(TAG, String.format("Play from unknown :%s", arg0));		
				}
			}
		});
		//setTabColor(tabHost);
    }
	
	public void chg_APP_ID(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
    	builder.setTitle("Change your APP_ID here\r\nYou need restart the apk!!!");

    	// Set up the input
    	final EditText input = new EditText(mActivity);
    	input.setText(MainActivity.APP_ID);
    	// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
    	input.setInputType(InputType.TYPE_CLASS_TEXT);
    	builder.setView(input);

    	// Set up the buttons
    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
    	    @Override
    	    public void onClick(DialogInterface dialog, int which) {
    	    	MainActivity.logVIfEnabled(TAG, String.format("chage APP_ID from %s to %s", MainActivity.APP_ID, input.getText().toString()));
    	    	MainActivity.APP_ID = input.getText().toString();
    	    }
    	});
    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	    @Override
    	    public void onClick(DialogInterface dialog, int which) {
    	        dialog.cancel();
    	    }
    	});

    	builder.show();
    }
    public void clear_url_history(){
    	MainActivity.myDialog.ConfirmAlertDlg("Warnning",
    			"Do you want to clear the history?",
    			"Yes",
    			new DialogInterface.OnClickListener() {         
		    		@Override                                                        
		    		public void onClick(DialogInterface dialog, int which) {
		    			MainActivity.mediaHistory.clear();
		    		}                                                                  
		    	},
		    	"No",
		    	null);
    }
}
