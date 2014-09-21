package com.example.castvideo;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class Player implements OnBufferingUpdateListener,
		OnCompletionListener, MediaPlayer.OnPreparedListener,
		SurfaceHolder.Callback {
	private static final String TAG = Player.class.getSimpleName();
	public static final boolean ENABLE_LOGV = true;
	public mediaPlayer mediaPlayer;
	//private SurfaceHolder s;
	private SeekBar skbProgress;
	private Timer mTimer=new Timer();
	private boolean fgTimerInit = false;
	private ImageView mImg_playpause, mImg_playnext, mImg_playprev, mImg_repeatmode;
	private TextView mPosition, mDuration, mText_repeatmode;

	public String local_media_file, local_media_title, local_media_size, local_media_duration, local_media_songer, local_media_mimetype;
	private int local_media_type;
	
	private Activity mActivity;
	private int mRepeatmode;
	

	private String getLocalIpAddress() {  
		try {
			String ipv4;
			ArrayList<NetworkInterface> mylist = Collections
					.list(NetworkInterface.getNetworkInterfaces());

			for (NetworkInterface ni : mylist) {

				ArrayList<InetAddress> ialist = Collections.list(ni
						.getInetAddresses());
				for (InetAddress address : ialist) {
					if (!address.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(ipv4 = address
							.getHostAddress())) {
						return ipv4;
					}
				}
			}

		} catch (SocketException ex) {

		}
		return null;
	}
	
	public void cast_localfile(){		
		String ip=getLocalIpAddress();
		if (ip != null){
			MainActivity.fg_localplayback = true;
			MainActivity.mMedia = new CastMedia(local_media_title, null, local_media_type, local_media_mimetype, true);
			MainActivity.logVIfEnabled(TAG, "ip:"+ip); 
			String url = String.format("http://%s:8080/%s", ip, local_media_file);
			MainActivity.mMedia.AddMedia(local_media_title, local_media_file, url, local_media_size, local_media_duration);
			
			EVideo mediahistory_temp = new EVideo(true);
			mediahistory_temp.setPath(local_media_file);
			mediahistory_temp.setTitle(local_media_title);
			mediahistory_temp.setType(local_media_mimetype);
			mediahistory_temp.setFormat(local_media_type);
			mediahistory_temp.setSize(local_media_size);
			mediahistory_temp.setDuration(local_media_duration);
			
	        MainActivity.AddHistory(mediahistory_temp);
	        //UpdateHistoryInCfg();
		}else{
			return ;
		}		
	}
	public void cast_urlfile(){
		MainActivity.myVideos = MainActivity.mAnalyzeMP4Url.GetVideos();
		MainActivity.mMedia = MainActivity.mAnalyzeMP4Url.GetBestVideo();
		MainActivity.fg_localplayback = false;
		EVideo mediahistory_temp = new EVideo(false);
		mediahistory_temp.setPath(MainActivity.mAnalyzeMP4Url.GetUrl());
		mediahistory_temp.setTitle(MainActivity.mAnalyzeMP4Url.GetTitle());
		mediahistory_temp.setType(MainActivity.mMedia.getMimeType());
		mediahistory_temp.setSize(MainActivity.mMedia.getSize());
		mediahistory_temp.setDuration(MainActivity.mMedia.getDuration());

		MainActivity.AddHistory(mediahistory_temp);
        //UpdateHistoryInCfg();
		MainActivity.tabhostcontent.tabhost_url_content_update();
	}
	

    public void play_prev_localmedia(){
    	List<EVideo> temp_medias;
    	EVideo eVideo;
    	int media_idx = 0;
    	if (MainActivity.mMedia == null){
    		return ;
    	}

    	if (MainActivity.mMedia.getSubMediaNum() > 1){
    		prevChapter();
    		return;
    	}
    	if (MainActivity.mMedia.getType() == 0){
    		temp_medias = MainActivity.mvideos;
    	}else if (MainActivity.mMedia.getType() == 1){
    		temp_medias = MainActivity.maudios;
    	}else if (MainActivity.mMedia.getType() == 2){
    		temp_medias = MainActivity.mphotos;
    	}else{
    		temp_medias = null;
    		return;
    	}

    	boolean fg_hasfav = false;
		for (int location=0; location<temp_medias.size(); location++){
			if (temp_medias.get(location).fg_fav()){
				fg_hasfav = true;
				break;
			}
		}
		if (!fg_hasfav){
			return;
		}
		
    	for (int i=0; i < temp_medias.size(); i++){
    		if (local_media_file.equals(temp_medias.get(i).getPath())){
    			media_idx = i;
    			break;
    		}
    	}

    	if (mRepeatmode == 0){
    		for (int temp_idx=0; temp_idx< temp_medias.size(); temp_idx++){
    			eVideo = temp_medias.get((temp_medias.size() + media_idx - 1 - temp_idx)%temp_medias.size());
	    		if (eVideo.fg_fav()){
	    			media_idx = (temp_medias.size() + media_idx - 1 - temp_idx)%temp_medias.size();
	    			break;
	    		}
	    	}
    	}
    	else if (mRepeatmode == 1){
    		while (true){
    			Random rand = new Random();
    			int temp_idx;
    			temp_idx = rand.nextInt(temp_medias.size());
    			if (media_idx == temp_idx || temp_medias.get(temp_idx).fg_fav() == false){
    				continue;
    			}
    			media_idx = temp_idx;
    			break;
    		}
    	}
    	eVideo = temp_medias.get(media_idx);
    	local_media_file = eVideo.getPath();
    	local_media_title = eVideo.getTitle();
    	local_media_size = eVideo.getSize();
    	local_media_songer = eVideo.getSonger();
    	local_media_duration = eVideo.getDuration();
    	local_media_type = eVideo.getFormat();
    	local_media_mimetype = eVideo.getType();
    	cast_localfile();
		playUrl();
    }
    
	
	public void play_next_localmedia(){
    	List<EVideo> temp_medias;
    	EVideo eVideo;
    	int media_idx = 0;
    	if (MainActivity.mMedia == null){
    		return ;
    	}
    	if (MainActivity.mMedia.getSubMediaNum() > 1){
    		nextChapter();
    		return;
    	}
    	
    	if (MainActivity.mMedia.getType() == 0){
    		temp_medias = MainActivity.mvideos;
    	}else if (MainActivity.mMedia.getType() == 1){
    		temp_medias = MainActivity.maudios;
    	}else if (MainActivity.mMedia.getType() == 2){
    		temp_medias = MainActivity.mphotos;
    	}else{
    		temp_medias = null;
    		return;
    	}
    	
    	boolean fg_hasfav = false;
		for (int location=0; location<temp_medias.size(); location++){
			if (temp_medias.get(location).fg_fav()){
				fg_hasfav = true;
				break;
			}
		}
		if (!fg_hasfav){
			return;
		}
    	
    	for (int i=0; i < temp_medias.size(); i++){
    		if (local_media_file.equals(temp_medias.get(i).getPath())){
    			media_idx = i;
    			break;
    		}
    	}

    	if (mRepeatmode == 0){
    		for (int temp_idx=0; temp_idx< temp_medias.size(); temp_idx++){
	    		eVideo = temp_medias.get((temp_idx + media_idx + 1)%temp_medias.size());
	    		if (eVideo.fg_fav()){
	    			media_idx = (temp_idx + media_idx + 1)%temp_medias.size();
	    			break;
	    		}
	    	}
    	}
    	else if (mRepeatmode == 1){
    		while (true){
    			Random rand = new Random();
    			int temp_idx;
    			temp_idx = rand.nextInt(temp_medias.size());
    			if (media_idx == temp_idx || temp_medias.get(temp_idx).fg_fav() == false){
    				continue;
    			}
    			media_idx = temp_idx;
    			break;
    		}
    	}
    	eVideo = temp_medias.get(media_idx);
    	local_media_file = eVideo.getPath();
    	local_media_title = eVideo.getTitle();
    	local_media_size = eVideo.getSize();
    	local_media_songer = eVideo.getSonger();
    	local_media_duration = eVideo.getDuration();
    	local_media_type = eVideo.getFormat();
    	local_media_mimetype = eVideo.getType();
    	cast_localfile();
		playUrl();
    }
    
	
	
	protected void Player_GUI_init(){
		mPosition = (TextView) mActivity.findViewById(R.id.position_str);
		mDuration = (TextView) mActivity.findViewById(R.id.duration_str);
		mText_repeatmode = (TextView) mActivity.findViewById(R.id.repeat_mode_des);
		mImg_repeatmode = (ImageView) mActivity.findViewById(R.id.repeat_mode); 
		mRepeatmode = 0;
		mImg_repeatmode.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (MainActivity.mMedia == null || MainActivity.mMedia.fg_local() == false){
					return;
				}
				MainActivity.logVIfEnabled(TAG, "mText_repeatmode clicked");
				if (mRepeatmode == 0){
					mRepeatmode = 1;
					mText_repeatmode.setText("play random");
					mImg_repeatmode.setImageResource(R.drawable.random_repeat);
				}else if (mRepeatmode == 1){
					mRepeatmode = 0;
					mText_repeatmode.setText("play sequence");
					mImg_repeatmode.setImageResource(R.drawable.seq_repeat);
				}
			}
		});


		mImg_playpause = (ImageView) mActivity.findViewById(R.id.play_pause);
		mImg_playpause.setImageResource(R.drawable.play);
		mImg_playpause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (MainActivity.mMedia == null ||
						MainActivity.mRemoteMediaPlayer == null){
					return ;
				}
				if (mediaPlayer.isPlaying()){
					mediaPlayer.pause();
					mImg_playpause.setImageResource(R.drawable.play);
				}else{
					mediaPlayer.play();
					mImg_playpause.setImageResource(R.drawable.pause);
				}
			}
		});

		skbProgress = (SeekBar) mActivity.findViewById(R.id.skbProgress);
		skbProgress.setOnSeekBarChangeListener(new SeekBarChangeEvent());
		skbProgress.setProgress(0);


		mImg_playnext = (ImageView) mActivity.findViewById(R.id.play_next);
		mImg_playnext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				play_next_localmedia();
			}
		});
		mImg_playprev = (ImageView) mActivity.findViewById(R.id.play_prev);
		mImg_playprev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				play_prev_localmedia();
			}
		});
	}
	


	class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {
		double progress;

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (!MainActivity.readyCast || MainActivity.mRemoteMediaPlayer == null){				
				return;
			}
			if (progress >= seekBar.getMax()-2){
				progress = seekBar.getMax()-2;
			}
			this.progress = progress * mediaPlayer.getAbsDuration() / seekBar.getMax();
			mPosition.setText(getTimeStr((int)(this.progress)));
			MainActivity.logVIfEnabled(TAG, "onProgressChanged :"+this.progress);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if (!MainActivity.readyCast || MainActivity.mRemoteMediaPlayer == null){
				MainActivity.logVIfEnabled(TAG, String.format("onProgressChanged:%s %s", MainActivity.readyCast, MainActivity.mMedia==null));
				return;
			}
			mediaPlayer.SeekTo(progress);
		}
	}
    
	
	public Player(Activity activity, mediaPlayer mediaplayer)
	{
		mActivity = activity;
		Player_GUI_init();
		mediaPlayer = mediaplayer;
		mTimer.schedule(mTimerTask, 0, 1000);
		//surfaceHolder=surfaceView.getHolder();
		//surfaceHolder.addCallback(this);
	}
	/*******************************************************
	 * ͨ��ʱ����Handler�����½����
	 ******************************************************/
	TimerTask mTimerTask = new TimerTask() {
		@Override
		public void run() {
			if (!skbProgress.isPressed()) {
				handleProgress.sendEmptyMessage(0);
			}
		}
	};
	
	public String getTimeStr(int seconds){
		return String.format("%02d:%02d:%02d", seconds/3600, seconds/60%60, seconds%60); 
	}
	
	Handler handleProgress = new Handler() {
		public void handleMessage(Message msg) {
			if (MainActivity.mRemoteMediaPlayer == null){
				return;
			}
			int position = (int)mediaPlayer.getCurrentPosition();
			int duration = (int)mediaPlayer.getDuration();

			int abs_pos = (int)mediaPlayer.getCurrentAbsPosition();
			int abs_dur = (int)mediaPlayer.getAbsDuration();


/*			MainActivity.logVIfEnabled(TAG, 
					String.format("position:%d duration:%d; abs_pos:%d abs_dur:%d; title:%s Loading:%s; "+mediaPlayer.getPlayerState(), 
							position, 
							duration, 
							abs_pos,
							abs_dur,
							mediaPlayer.getTitle(), 
							mediaPlayer.fg_loading()));*/
			if (duration > 0 && !mediaPlayer.fg_seeking()) {
				long pos = skbProgress.getMax() * abs_pos / abs_dur;
				skbProgress.setProgress((int) pos);

				mPosition.setText(getTimeStr(abs_pos));
				mDuration.setText(getTimeStr(abs_dur));
			}
		};
	};
	

	//*****************************************************
    public void play_from_url(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
    	builder.setTitle("Play from URL");
    	final EditText input = new EditText(mActivity);
    	input.setInputType(InputType.TYPE_CLASS_TEXT);
    	builder.setView(input);
    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
    	    @Override
    	    public void onClick(DialogInterface dialog, int which) {
    	    	
    	    	String URL = input.getText().toString();
    	    	MainActivity.logVIfEnabled(TAG, String.format("Get URL :%s", URL));
				MainActivity.mAnalyzeMP4Url.GetMP4Addr(URL, "unknown");
				MainActivity.ringProgressDialog = ProgressDialog.show(mActivity, 
						"Please wait ...", 
						String.format("Progress url:\"%s\" ...", URL), true);
		        MainActivity.ringProgressDialog.setCancelable(false);
    	    }
    	});
    	builder.show();
    }

	
	//*****************************************************
	public void play_media(EVideo eVideo){
		if (eVideo.fg_local()){
            local_media_file = eVideo.getPath();
            local_media_title = eVideo.getTitle();
            local_media_size = eVideo.getSize();
            local_media_songer = eVideo.getSonger();
            local_media_duration = eVideo.getDuration();
            local_media_type = eVideo.getFormat();
            local_media_mimetype = eVideo.getType();
            if (MainActivity.fgReadyCast()){
            	ConfirmCastLocalVideoDlg();
            }
        }else{
			MainActivity.mAnalyzeMP4Url.GetMP4Addr(eVideo.getPath(), eVideo.getTitle());
			MainActivity.ringProgressDialog = ProgressDialog.show(mActivity, 
					"Please wait ...", 
					String.format("Progress %s;\r\nurl:\"%s\" ...", eVideo.getTitle(), eVideo.getPath()), true);
			MainActivity.ringProgressDialog.setCancelable(false);
        }
	}
	
	private void ConfirmCastLocalVideoDlg() {
		// TODO Auto-generated method stub
		String AlertMsg;
		if (MainActivity.mMediaRouter != null 
				&& MainActivity.mMediaRouter.getSelectedRoute() != null 
				&& MainActivity.mMediaRouter.getSelectedRoute().getDescription() != null){
			AlertMsg = String.format("%s;\r\nPress yes to play %s;\r\nPress no to cancel", 
					MainActivity.mMediaRouter.getSelectedRoute().getDescription(), 
					local_media_title);
			MainActivity.myDialog.ConfirmAlertDlg("Warnning", 
					AlertMsg, 
					"Yes",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
					cast_localfile();
					playUrl();
				}
			}, 
			"No",
			null);
		}else{
			cast_localfile();
			playUrl();
		}		
	}
	

	public void ConfirmCastUrlVideoDlg(){		
		if (MainActivity.mMediaRouter != null 
				&& MainActivity.mMediaRouter.getSelectedRoute() != null 
				&& MainActivity.mMediaRouter.getSelectedRoute().getDescription() != null){
			String[] media_list = new String[MainActivity.mAnalyzeMP4Url.GetVideos().size()];
			final boolean[] select_list = new boolean[MainActivity.mAnalyzeMP4Url.GetVideos().size()];
			for (int idx=0; idx<MainActivity.mAnalyzeMP4Url.GetVideos().size(); idx++){
				media_list[idx] = MainActivity.mAnalyzeMP4Url.GetVideos().get(idx).getTitle();
				select_list[idx] = false;
			}
			select_list[0] = true;
			String AlertMsg = String.format("%s;\r\nPress yes to play %s;\r\nPress no to cancel",	
					MainActivity.mMediaRouter.getSelectedRoute().getDescription(), 
					MainActivity.mAnalyzeMP4Url.GetTitle());
			String title = String.format("Do you want to play %s", MainActivity.mAnalyzeMP4Url.GetTitle());
			MainActivity.myDialog.ConfirmAlertDlg(
					title,
					select_list,
					media_list,
					"Yes",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
					int which = 0;
					for (int idx=0; idx<select_list.length; idx++){
						if (select_list[idx]){
							which = idx;
						}
					}
					cast_urlfile();
					if (which < MainActivity.mAnalyzeMP4Url.GetVideos().size()){
						MainActivity.mMedia = MainActivity.mAnalyzeMP4Url.GetVideos().get(which);
					}
					playUrl();
				}
			},
			"No",
			null);
		}else{
			cast_urlfile();
			playUrl();
		}
	}

	
	public void play(){
		mediaPlayer.play();
	}
		
	public void pause()
	{
		mediaPlayer.pause();
	}
	
	public void stop()
	{
		mediaPlayer.stop();
        skbProgress.setProgress(0);
        MainActivity.logVIfEnabled(TAG, "Stopped");
	}
	
	public void playUrl(){
		if (MainActivity.readyCast){
			mediaPlayer.start();
			if (!fgTimerInit) {
				//mTimer.schedule(mTimerTask, 0, 1000);
				fgTimerInit = true;
			}
			mImg_playpause.setImageResource(R.drawable.pause);
		}else{
			MainActivity.logVIfEnabled(TAG, "Chromecast not connected, Please try to reconnect");
			MainActivity.myDialog.ConfirmAlertDlg("Warnning", "Chromecast not connected, Please try to reconnect", "OK", null);
		}
	}
	
	public void nextChapter(){
		mediaPlayer.nextChapter();
	}
	public void prevChapter(){
		mediaPlayer.prevChapter();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		MainActivity.logVIfEnabled(TAG, "surface changed");
	}

	
	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		stop();
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int bufferingProgress) {
			skbProgress.setSecondaryProgress(bufferingProgress);
			/*int currentProgress=(int)(skbProgress.getMax()*mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration());
			MainActivity.logVIfEnabled(TAG, currentProgress+"% play" + bufferingProgress + "% buffer");*/
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
	}
}