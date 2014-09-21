package com.example.castvideo;

import java.util.List;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UrlContentExAdapter extends BaseExpandableListAdapter {

	private static final String TAG = LocalMediaExAdapter.class.getSimpleName();
	private Context mContext; 
    private LayoutInflater mInflater = null;  
	public  UrlContentExAdapter(Context ctx)  
	{  
		mContext = ctx; 
        mInflater = (LayoutInflater) mContext 
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}  
	public  CastMediaUnit getChild(int  groupPosition, int  childPosition)  
	{  
		return  MainActivity.myVideos.get(groupPosition).geMediabyIdx(childPosition);  
	}  
	public  long  getChildId(int  groupPosition, int  childPosition)  
	{  
		return  childPosition;  
	}  
	public  int  getChildrenCount(int  groupPosition)  
	{  
		return  MainActivity.myVideos.get(groupPosition).getSubMediaNum();  
	}  
	// group method stub   
	public  CastMedia getGroup(int  groupPosition)  
	{  
		return  MainActivity.myVideos.get(groupPosition);  
	}  
	public  int  getGroupCount()  
	{  
		return  MainActivity.myVideos.size();  
	}  
	public  long  getGroupId(int  groupPosition)  
	{  
		return  groupPosition;  
	}  
	public  View getGroupView(int  groupPosition, boolean  isExpanded,  
			View convertView, ViewGroup parent)  
	{  
		if (convertView == null) { 
            convertView = mInflater.inflate(R.layout.local_media_playback, null); 
        } 
		CastFoldViewHolder holder = new CastFoldViewHolder(convertView); 
		holder.setPosition(groupPosition);
        return convertView; 
	}  
	
	 @Override 
	 public View getChildView(int groupPosition, int childPosition, 
			 boolean isLastChild, View convertView, ViewGroup parent) { 
		 // TODO Auto-generated method stub 
		 if (convertView == null) { 
			 convertView = mInflater.inflate(R.layout.video_list_item, null); 
		 } 
		 CastVideoViewHolder holder = new CastVideoViewHolder(convertView); 
		 holder.setPosition(groupPosition, childPosition);
		 return convertView; 
	 } 
	 
	 @Override 
	 public boolean isChildSelectable(int groupPosition, int childPosition) { 
		 // TODO Auto-generated method stub 
		 /*很重要：实现ChildView点击事件，必须返回true*/ 
		 return true; 
	 } 
	 
	
	public  boolean  hasStableIds()  
	{  
		return  false ;  
	}  
	private class CastFoldViewHolder {
		TextView mTextView_FoldName;

		public CastFoldViewHolder(View view) {
			mTextView_FoldName = (TextView) view.findViewById(R.id.local_media_fold_textview);
		}

		public void setPosition(int position) {
			mTextView_FoldName.setText(MainActivity.myVideos.get(position).getTitle());			
			Log.e("TAG", String.format("set position %d done", position));
		}
	}	
	private class CastVideoViewHolder {
		ImageView mImageView, mFavView;
		TextView mTitleText, mVideoSizeText, mDurationText;

        
		public CastVideoViewHolder(View view) {
			mImageView = (ImageView) view.findViewById(R.id.videoImage);
			mFavView = (ImageView) view.findViewById(R.id.videoFav);
			mTitleText = (TextView) view.findViewById(R.id.videoTitleText);
			mVideoSizeText = (TextView) view.findViewById(R.id.videoSizeText);
			mDurationText = (TextView) view.findViewById(R.id.videoDurationText);
		}

		public void setPosition(final int groupPosition, final int childPosition) {
			CastMediaUnit tempVideo = MainActivity.myVideos.get(groupPosition).geMediabyIdx(childPosition);
			Log.e(TAG, String.format("set position（%d:%d）:%s", groupPosition, childPosition, tempVideo.getFileName()));

			int hour, min, sec, Duration;
			Duration =  tempVideo.GetLength();
	    	hour = Duration/3600;
	    	min = Duration%3600/60;
			sec = Duration%60;

			mVideoSizeText.setText(String.format("%.2fMB", tempVideo.GetSize()));
			mImageView.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.video));
			mFavView.setVisibility(View.INVISIBLE);
			mTitleText.setText(tempVideo.getFileName());
			
			mDurationText.setText(String.format("%02d:%02d:%02d", hour, min, sec));
			
		}
	}
}
