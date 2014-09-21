package com.example.castvideo;
import java.io.File;
import java.io.FileInputStream;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;


public class LocalMedia_Collect {
	Context mContext;
	public LocalMedia_Collect(Context context){
		mContext = context;
	}

	private void fillList() {
		int id, durationMs;
		long size;
		String title, album, artist, url, type, description;
		File file;
		Cursor cursor;
		
		

		cursor = mContext.getContentResolver().query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Video.Media.DEFAULT_SORT_ORDER);
		// �õ�cursor�����ǿ��Ե���Cursor����ط��������������Ϣ:
		if (cursor != null) {
			while (cursor.moveToNext()) {
				id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
				title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
				album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
				artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
				url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
				durationMs = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
				size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
				type = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
				description = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DESCRIPTION));
				file = new File(url);
				
				if (file.exists()) {
					String name = url.substring(url.lastIndexOf("/") + 1);
					EVideo video = new EVideo(true);
					video.setId(id);
					video.setName(name);
					video.setTitle(title);
					video.setAlbum(album);
					video.setSonger(artist);
					video.setPath(url);
					if (size < 1024){
						video.setSize(size + "B");
					}else if (size < 1024*1024){
						video.setSize(String.format("%.2fKB", (float)size/1024));
					}else if (size < 1024*1024*1024){
						video.setSize(String.format("%.2fMB", (float)size/(1024*1024)));
					}else {
						video.setSize(String.format("%.2fGB", (float)size/(1024*1024*1024)));
					}
					long mm = durationMs / 1000 / 60;
					long ss = durationMs / 1000 % 60;
					String duration = mm + ":" + (ss >= 10 ? ss : "0" + ss);
					video.setDuration(duration);
					// video.setYear(year+"");
					video.setType(type);
					video.setDescription(description);
					video.setFormat(0);
					
					for (int i=0; i<MainActivity.mediaHistory.size(); i++){
						if (video.getPath().equals(MainActivity.mediaHistory.get(i).getPath())){
							video.set_fav(MainActivity.mediaHistory.get(i).fg_fav());
						}
					}

					MainActivity.mvideos.add(video);
				}
			}
		}
			
		cursor = mContext.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		// �õ�cursor�����ǿ��Ե���Cursor����ط��������������Ϣ:
		if (cursor != null) {
			while (cursor.moveToNext()) {
				id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
				title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
				album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
				artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
				url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				durationMs = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
				size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
				type = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
				file = new File(url);

				if (file.exists()) {
					String name = url.substring(url.lastIndexOf("/") + 1);
					EVideo video = new EVideo(true);
					video.setId(id);
					video.setName(name);
					video.setTitle(title);
					video.setAlbum(album);
					video.setSonger(artist);
					video.setPath(url);
					if (size < 1024){
						video.setSize(size + "B");
					}else if (size < 1024*1024){
						video.setSize(String.format("%.2fKB", (float)size/1024));
					}else if (size < 1024*1024*1024){
						video.setSize(String.format("%.2fMB", (float)size/(1024*1024)));
					}else {
						video.setSize(String.format("%.2fGB", (float)size/(1024*1024*1024)));
					}
					long mm = durationMs / 1000 / 60;
					long ss = durationMs / 1000 % 60;
					String duration = mm + ":" + (ss >= 10 ? ss : "0" + ss);
					video.setDuration(duration);
					// video.setYear(year+"");
					video.setType(type);
					video.setFormat(1);
					MainActivity.maudios.add(video);

					for (int i=0; i<MainActivity.mediaHistory.size(); i++){
						if (video.getPath().equals(MainActivity.mediaHistory.get(i).getPath())){
							video.set_fav(MainActivity.mediaHistory.get(i).fg_fav());
						}
					}
				}
			}
		}
		
		cursor = mContext.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Images.Media.DEFAULT_SORT_ORDER);
		// �õ�cursor�����ǿ��Ե���Cursor����ط��������������Ϣ:
		if (cursor != null) {
			while (cursor.moveToNext()) {
				id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
				title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
				url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
				size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
				type = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
				file = new File(url);

				if (file.exists()) {
					String name = url.substring(url.lastIndexOf("/") + 1);
					EVideo video = new EVideo(true);
					video.setId(id);
					video.setName(name);
					video.setTitle(title);
					video.setSonger(null);
					video.setPath(url);
					if (size < 1024){
						video.setSize(size + "B");
					}else if (size < 1024*1024){
						video.setSize(String.format("%.2fKB", (float)size/1024));
					}else if (size < 1024*1024*1024){
						video.setSize(String.format("%.2fMB", (float)size/(1024*1024)));
					}else {
						video.setSize(String.format("%.2fGB", (float)size/(1024*1024*1024)));
					}
					String duration = null;
					video.setDuration(duration);
					// video.setYear(year+"");
					video.setType(type);
					video.setFormat(2);
					for (int i=0; i<MainActivity.mediaHistory.size(); i++){
						if (video.getPath().equals(MainActivity.mediaHistory.get(i).getPath())){
							video.set_fav(MainActivity.mediaHistory.get(i).fg_fav());
						}
					}

					MainActivity.mphotos.add(video);
				}
			}
		}
	}
	
	private void update_evideo(){
		EVideo video;
		Bitmap bmp = null;
		for (int i=0; i<MainActivity.mvideos.size(); i++)
		{
			video = MainActivity.mvideos.get(i);
			Log.e("update mvideos", String.format("set position（%d）:%s duration:%s", 
					i, 
					video.getName(),
					video.getDuration()));
			if (video.getBmp() == null) {
				bmp = MyThumbnailUtils.createVideoThumbnail(video.getPath(), Images.Thumbnails.MINI_KIND);
				if (bmp == null){
					Log.e("update_evideo", video.getPath() + " get thumb failed");
					bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.video);
				}
				bmp = MyThumbnailUtils.extractThumbnail(bmp, 48, 48);
				MainActivity.mvideos.get(i).setBmp(bmp);
			}
		}
		
		for (int i=0; i<MainActivity.maudios.size(); i++)
		{
			video = MainActivity.maudios.get(i);
			Log.e("update maudios", String.format("set position（%d）:%s duration:%s", 
					i, 
					video.getName(),
					video.getDuration()));
			if (video.getBmp() == null) {
				bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.audio);
				bmp = MyThumbnailUtils.extractThumbnail(bmp, 48, 48);
				MainActivity.maudios.get(i).setBmp(bmp);
			}
		}
		
		
		BitmapFactory.Options options=new BitmapFactory.Options();
		for (int i=0; i<MainActivity.mphotos.size(); i++)
		{
			video = MainActivity.mphotos.get(i);
			Log.e("update mphotos", String.format("set position（%d）:%s size:%s", 
					i, 
					video.getName(),
					video.getSize()));
			if (video.getBmp() == null) {
				options.inJustDecodeBounds = true;
				options.inSampleSize = 1;
				bmp  = BitmapFactory.decodeFile(video.getPath(), options);
				options.inSampleSize = options.outWidth/48;
				options.inJustDecodeBounds = false;
				bmp  = BitmapFactory.decodeFile(video.getPath(), options);
				bmp = MyThumbnailUtils.extractThumbnail(bmp, 48, 48);
				MainActivity.mphotos.get(i).setBmp(bmp);
			}
		}

	}
	
	private class update_evideoThread extends  Thread {
	    @Override
	    public void  run() {
	    	fillList();
	    	update_evideo();
	    }
	}
	
	public void collect(){
		update_evideoThread update_evideoth = new update_evideoThread();
		update_evideoth.start();
	}
}
