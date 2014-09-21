package com.example.castvideo;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.images.WebImage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class mediaPlayer  {
	private static final String TAG = mediaPlayer.class.getSimpleName();
	public static final boolean ENABLE_LOGV = true;
	private double mVolume = 0.5;
	private boolean fgPlaying;
	private boolean fgLoading=false;
	private Context mcontext;
	private double CurrentPosition;
	private int is_needseek = 0; //0 -- no need 1--need seek 2 --seeking
	
	public mediaPlayer(Context context)
	{
		fgPlaying = false;
		is_needseek = 0;
		mcontext = context;
	}
	
	public boolean fg_sync(){
		return MainActivity.mMedia.getFileName().equalsIgnoreCase(MainActivity.mRemoteMediaPlayer.getNamespace());
	}
	
	private void reconnect_device(){
		MainActivity.mMediaRouteActionProvider.getMediaRouteButton().performClick();
        MainActivity.mMediaRouter.selectRoute(MainActivity.mMediaRouter.getRoutes().get(0));
        warnning_box("Please try to re-connect your devices; and replay the media");
	}
	
	public boolean start_next(){
		if (MainActivity.mMedia == null){
			return false;
		}
		if (MainActivity.mMedia.getSubMediaNum() > 1 && MainActivity.mMedia.getNextMedia() > 0){
			MainActivity.logVIfEnabled(TAG, 
					String.format("mMedia.title:%s mMetaData.title:%s", 
							MainActivity.mMedia.getFileName(), 
							MainActivity.mRemoteMediaPlayer.getNamespace()));
			start();
			return true;
		}
		return false;
	}
	
	public void start()	{
        if (MainActivity.mRemoteMediaPlayer == null) {
            Log.e(TAG, "Trying to play a video with no active media session");
            reconnect_device();
            return;
        }
        if (MainActivity.mMedia == null || MainActivity.mMedia.getUrl() == null){
            Log.e(TAG, "mMedia is null");
        	return;
        }
		Log.i(TAG, String.format("Loading selected media, filename:%s title:%s url:%s idx:%d",
				MainActivity.mMedia.getFileName(),
				MainActivity.mMedia.getTitle(),
				MainActivity.mMedia.getUrl(),
				MainActivity.mMedia.getCurrMedia()));
		
		MediaMetadata metadata;
		if (MainActivity.mMedia.getType() == 0){
			metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_TV_SHOW);
		}else if (MainActivity.mMedia.getType() == 1){
			metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
		}else if (MainActivity.mMedia.getType() == 2){
			metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
		}else {
			Log.e(TAG, "Invalid mMedia type:"+MainActivity.mMedia.getType());
			return ;
		}
		metadata.putString(MediaMetadata.KEY_TITLE,MainActivity.mMedia.getTitle());
		metadata.putString(MediaMetadata.KEY_SERIES_TITLE, MainActivity.mMedia.getFileName());
		Uri imageUrl = (MainActivity.mMedia.getImgUrl() != null) ? Uri.parse(MainActivity.mMedia.getImgUrl()) : null;
		if (imageUrl != null){
			metadata.addImage(new WebImage(imageUrl));
		}
		MediaInfo media = new MediaInfo.Builder(MainActivity.mMedia.getUrl())
								.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
								.setContentType(MainActivity.mMedia.getMimeType()).setMetadata(metadata)
								.build();
								
		if (media == null) {
			MainActivity.logVIfEnabled(TAG, "create MediaInfo failed");
			Toast.makeText(mcontext, "Cast failed, try again!!!", Toast.LENGTH_SHORT).show();
            return;
        }
        fgLoading = true;

        if (MainActivity.mMedia.getType() == 0 || MainActivity.mMedia.getType() == 1){
        	MainActivity.mRemoteMediaPlayer.load(MainActivity.mApiClient, media, true).setResultCallback(
        			new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
        				@Override
        				public void onResult(MediaChannelResult result) {
        					if (!result.getStatus().isSuccess()) {
        						Log.e(TAG, "Failed to load media.");
        						Toast.makeText(mcontext, "Failed to load media, try again!!!", Toast.LENGTH_SHORT).show();
        						fgLoading = false;
        						MainActivity.logVIfEnabled(TAG, "Load cancelled");
        					}else{
        						fgLoading = false;
        						MainActivity.logVIfEnabled(TAG, "Load completed - starting playback is_needseek="+is_needseek);
        						if (is_needseek == 1) {
        							MainActivity.logVIfEnabled(TAG, String.format("seek to :%f", CurrentPosition));
        							seekTo(CurrentPosition);
        							is_needseek = 2;
        						}
        						onSetVolume(mVolume);
        						fgPlaying = true;
        					}
        				}
        			});
        }else if (MainActivity.mMedia.getType() == 2){
        	MainActivity.mRemoteMediaPlayer.load(MainActivity.mApiClient, media, false).setResultCallback(
        			new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
        				@Override
        				public void onResult(MediaChannelResult result) {
        					fgLoading = false;
        					if (!result.getStatus().isSuccess()) {
        						MainActivity.logVIfEnabled(TAG, "Failed to load photo;Load cancelled");
        					}else{
        						MainActivity.logVIfEnabled(TAG, "Load photo completed");
        					}
        				}
        			});
        }
	}
	public void play()	{
		if (MainActivity.mRemoteMediaPlayer == null){
			reconnect_device();
			return ;
		}
		try {
			MainActivity.mRemoteMediaPlayer.play(MainActivity.mApiClient);
			fgPlaying = true;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void pause()	{
		if (MainActivity.mRemoteMediaPlayer == null){
			reconnect_device();
			return ;
		}
		try {
			MainActivity.mRemoteMediaPlayer.pause(MainActivity.mApiClient);
			fgPlaying = false;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void stop()	{
		if (MainActivity.mRemoteMediaPlayer == null){
			reconnect_device();
			return ;
		}
		try {
			MainActivity.mRemoteMediaPlayer.stop(MainActivity.mApiClient);
			fgPlaying = false;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void warnning_box(String warnning_str){
		MainActivity.myDialog.ConfirmAlertDlg("Warnning", warnning_str, "OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
				MainActivity.mMediaRouteActionProvider.getMediaRouteButton().performClick();
			}
		} );
		/*
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				mcontext);
		// set title
		alertDialogBuilder.setTitle("Warnning");
		// set dialog message
		alertDialogBuilder.setMessage(warnning_str).setCancelable(true).setPositiveButton("OK",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked, close
				// current activity
				dialog.cancel();
			}}
			);
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();	*/
	}
	
	public void nextChapter(){
		if (MainActivity.mRemoteMediaPlayer == null){
			reconnect_device();
			return ;
		}
		if (MainActivity.mMedia.getSubMediaNum() <= 1){
			Toast.makeText(mcontext, String.format("%s only have 1 chapter", MainActivity.mMedia.getFileName()), Toast.LENGTH_SHORT).show();
		} else if ( MainActivity.mMedia.getCurrMedia() >= MainActivity.mMedia.getSubMediaNum() - 1) {
			Toast.makeText(mcontext, "It is the last chapter!!!", Toast.LENGTH_SHORT).show();
		}
		else {
			MainActivity.mMedia.getNextMedia();
			start();
		}
	}
	public void prevChapter(){
		if (MainActivity.mRemoteMediaPlayer == null){
			reconnect_device();
			return ;
		}
		if (MainActivity.mMedia.getSubMediaNum() <= 1){
			Toast.makeText(mcontext, String.format("%s only have 1 chapter", MainActivity.mMedia.getFileName()), Toast.LENGTH_SHORT).show();
		} else if ( MainActivity.mMedia.getCurrMedia() > 0){
			MainActivity.mMedia.getPrevMedia();
			start();
		}
		else {
			Toast.makeText(mcontext, "It is the last chapter!!!", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	public boolean isPlaying(){
		return fgPlaying;
	}
	public void resume() {
		start();
	}
	public void seekTo(double position){
		if (MainActivity.mRemoteMediaPlayer == null){
			reconnect_device();
			return ;
		}
		try {
			MainActivity.mRemoteMediaPlayer.seek(MainActivity.mApiClient, (long)position*1000, RemoteMediaPlayer.RESUME_STATE_PLAY).setResultCallback(
	                new ResultCallback<MediaChannelResult>() {
	                    @Override
	                    public void onResult(MediaChannelResult result) {
	                        Status status = result.getStatus();
	                        if (status.isSuccess()) {
	                        	is_needseek = 0;
	                        } else {
	                            Log.w(TAG, "Unable to seek: " + status.getStatusCode());
	                            Log.d(TAG, "Unable to seek: " + status);
	                        }
	                    }

	                });
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void SeekTo(double position){
		if (MainActivity.mRemoteMediaPlayer == null){
			reconnect_device();
			return ;
		}
		if (MainActivity.mMedia == null){
			MainActivity.mRemoteMediaPlayer.seek(MainActivity.mApiClient, (long)position*1000, RemoteMediaPlayer.RESUME_STATE_PLAY).setResultCallback(
	                new ResultCallback<MediaChannelResult>() {
	                    @Override
	                    public void onResult(MediaChannelResult result) {
	                        Status status = result.getStatus();
	                        if (status.isSuccess()) {
	                        	is_needseek = 0;
	                        } else {
	                            Log.w(TAG, "Unable to seek: " + status.getStatusCode());
	                        }
	                    }

	                });
			return ;
		}
		try {
			is_needseek = 1;
			if (MainActivity.mMedia.seekTo(position)){
				CurrentPosition = position - MainActivity.mMedia.getBasePosition();
				MainActivity.logVIfEnabled(TAG, String.format("seek to MainActivity.mMedia:%d pos:%f", MainActivity.mMedia.getCurrMedia(), CurrentPosition));
				start();
			}else{
				MainActivity.logVIfEnabled(TAG, String.format("seek to MainActivity.mMedia: pos:%f", position - MainActivity.mMedia.getBasePosition()));
				MainActivity.mRemoteMediaPlayer.seek(MainActivity.mApiClient, ((long)position - (long)MainActivity.mMedia.getBasePosition())*1000, RemoteMediaPlayer.RESUME_STATE_PLAY).setResultCallback(
		                new ResultCallback<MediaChannelResult>() {
		                    @Override
		                    public void onResult(MediaChannelResult result) {
		                        Status status = result.getStatus();
		                        if (status.isSuccess()) {
		                        	is_needseek = 0;
		                        } else {
		                            Log.w(TAG, "Unable to seek: " + status.getStatusCode());
		                        }
		                    }

		                });
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public double getCurrentPosition(){
		if (MainActivity.mRemoteMediaPlayer == null){
			return 0;
		}
		double temp = MainActivity.mRemoteMediaPlayer.getApproximateStreamPosition()/1000;
		if (is_needseek > 0){
			if (temp > CurrentPosition && is_needseek == 2){
				//seek done
				is_needseek = 0;
				CurrentPosition = temp;
			}
			return temp;
		}
		CurrentPosition = temp;
		return temp;
	}	
	public double getDuration(){
		if (MainActivity.mRemoteMediaPlayer == null){
			return 0;
		}
		double dur = (double)MainActivity.mRemoteMediaPlayer.getStreamDuration()/1000;
		if (MainActivity.mMedia != null && (MainActivity.mMedia.getFcsDuration() != (int)dur)){
			MainActivity.mMedia.setFcsDuration((int)(dur));
			MainActivity.logVIfEnabled(TAG, String.format("fcsDur:%d dur:%d", MainActivity.mMedia.getFcsDuration(), (int)(dur)));
			MainActivity.tabhostcontent.mUrlAdapter.notifyDataSetChanged();
		}
		return dur;
	}

	public double getCurrentAbsPosition(){
		if (MainActivity.mRemoteMediaPlayer == null){
			return 0;
		}
		if (MainActivity.mMedia == null){
			return MainActivity.mRemoteMediaPlayer.getApproximateStreamPosition()/1000;
		}
		return MainActivity.mRemoteMediaPlayer.getApproximateStreamPosition()/1000 + MainActivity.mMedia.getBasePosition();
	}	
	public double getAbsDuration(){
		if (MainActivity.mMedia == null || MainActivity.mMedia.getDoubleDuration() == 0){
			return getDuration();
		}
		return MainActivity.mMedia.getDoubleDuration();
	}
	
	public String getTitle(){
		if (MainActivity.mRemoteMediaPlayer == null){
			return null;
		}
		return MainActivity.mRemoteMediaPlayer.getNamespace();
	}
	public MediaStatus getPlayerState(){
		if (MainActivity.mRemoteMediaPlayer == null){
			return null;
		}
		return MainActivity.mRemoteMediaPlayer.getMediaStatus();
	}
	public boolean fg_loading(){
		return fgLoading;
	}
	public boolean fg_seeking(){
		if (is_needseek == 0){return false;}
		return true;
	}
	public double onGetVolume(){
		return mVolume;
	}
	public void onSetVolume(double Volume){
		if (MainActivity.mRemoteMediaPlayer == null){
			warnning_box("Please try to re-connect your devices");
			return ;
		}
		try {
			if (Volume > 1.0){Volume = 1.0;}
			if (Volume < 0.0){Volume = 0.0;}
			mVolume = Volume;
			Log.w(TAG, "try to set volume to "+Volume);
			try {
				Cast.CastApi.setVolume(MainActivity.mApiClient, Volume);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MainActivity.mRemoteMediaPlayer.setStreamVolume(MainActivity.mApiClient, 1.0).setResultCallback(null);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
