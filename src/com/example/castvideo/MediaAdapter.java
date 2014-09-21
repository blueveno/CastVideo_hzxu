package com.example.castvideo;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A BaseAdapter containing a fixed set of CastMedia objects.
 */
public class MediaAdapter extends BaseAdapter {
	private LayoutInflater mInflater;

	/**
	 * Creates a new MediaAdapter for the given activity.
	 */
	public MediaAdapter(Activity activity) {
		mInflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setSelect(View v, int position){
		v.setSelected(true);
		Log.i("test", String.format("id:%d", v.getId()));
		TextView VideoTitle = (TextView) v
				.findViewById(R.id.videoTitleText);
		VideoTitle.setSelected(true);
		Log.i("test", String.format("name:%s", VideoTitle.getText()));
	}
	
	@Override
	public int getCount() {
		return MainActivity.myVideos.size();
	}

	@Override
	public CastMedia getItem(int position) {
		return MainActivity.myVideos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		CastVideoViewHolder viewHolder;

		if (view == null) {
			view = mInflater.inflate(R.layout.video_list_item, null);
			viewHolder = new CastVideoViewHolder(view);
			view.setTag(viewHolder);
		} else {
			viewHolder = (CastVideoViewHolder) view.getTag();
		}
		viewHolder.setPosition(position);
		return view;        
	}

	 /**
     * 获取网落图片资源 
     * @param url
     * @return
     */
    public static Bitmap getHttpBitmap(String url){
    	URL myFileURL;
    	Bitmap bitmap=null;
    	try{
    		myFileURL = new URL(url);
    		//获得连接
    		HttpURLConnection conn=(HttpURLConnection)myFileURL.openConnection();
    		//设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
    		conn.setConnectTimeout(6000);
    		//连接设置获得数据流
    		conn.setDoInput(true);
    		//不使用缓存
    		conn.setUseCaches(false);
    		//这句可有可无，没有影响
    		//conn.connect();
    		//得到数据流
    		InputStream is = conn.getInputStream();
    		//解析得到图片
    		bitmap = BitmapFactory.decodeStream(is);
    		//关闭数据流
    		is.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
		return bitmap;
    	
    }
    
	private class CastVideoViewHolder {
		private TextView mVideoTitle, mVideoSize, mVideoDuration;
		private ImageView mFavImg, mVideoImage;

		public CastVideoViewHolder(View view) {
			mVideoTitle = (TextView) view
					.findViewById(R.id.videoTitleText);
			mVideoSize = (TextView) view
					.findViewById(R.id.videoSizeText);
			mVideoDuration = (TextView) view
					.findViewById(R.id.videoDurationText);
			mFavImg = (ImageView) view.findViewById(R.id.videoFav);
			mVideoImage = (ImageView) view.findViewById(R.id.videoImage);
		}

		public void setPosition(int position) {
			Log.e("MediaAdapter", String.format("set position（%d）:%s %s %s", 
					position, 
					MainActivity.myVideos.get(position).getTitle(),
					MainActivity.myVideos.get(position).getSize(),
					MainActivity.myVideos.get(position).getDuration()));
			mVideoTitle.setText(MainActivity.myVideos.get(position).getTitle());
			mVideoSize.setText(MainActivity.myVideos.get(position).getSize());
			mVideoDuration.setText(MainActivity.myVideos.get(position).getDuration());
			mFavImg.setVisibility(View.INVISIBLE);
			/*if (MainActivity.myVideos.get(position).getImgUrl() != null){
				Bitmap bitmap = getHttpBitmap(MainActivity.myVideos.get(position).getImgUrl());
				if (bitmap != null){
					mVideoImage.setImageBitmap(bitmap);
				}
			}*/
		}
	}
}
