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

public class LocalMediaExAdapter extends BaseExpandableListAdapter {
	private static final String TAG = LocalMediaExAdapter.class.getSimpleName();
	private Context mContext; 
    private LayoutInflater mInflater = null;  
    private boolean fg_HideFav;
    private String mFoldName[] = {"Video", "Audio", "Photo"};
	public  LocalMediaExAdapter(Context ctx, boolean fgHideFav)  
	{  
		mContext = ctx; 
        mInflater = (LayoutInflater) mContext 
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        fg_HideFav = fgHideFav;
	}  
	public  EVideo getChild(int  groupPosition, int  childPosition)  
	{  
		return  MainActivity.childArray.get(groupPosition).get(childPosition);  
	}  
	public  long  getChildId(int  groupPosition, int  childPosition)  
	{  
		return  childPosition;  
	}  
	public  int  getChildrenCount(int  groupPosition)  
	{  
		return  MainActivity.childArray.get(groupPosition).size();  
	}  
	// group method stub   
	public  List<EVideo> getGroup(int  groupPosition)  
	{  
		return  MainActivity.childArray.get(groupPosition);  
	}  
	public  int  getGroupCount()  
	{  
		return  MainActivity.groupArray.size();  
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
			mTextView_FoldName.setText(mFoldName[position]);			
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
			EVideo tempVideo = MainActivity.childArray.get(groupPosition).get(childPosition);
			Log.e(TAG, String.format("set position（%d:%d）:%s", groupPosition, childPosition, tempVideo.getTitle()));

			mVideoSizeText.setText( tempVideo.getSize());
			
			if (tempVideo.getBmp() != null){
				mImageView.setImageBitmap(tempVideo.getBmp());
			}else{

				if (groupPosition == 0){
					mImageView.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.video));
				}else if (groupPosition == 1){
					mImageView.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.audio));
				}else if (groupPosition == 2){
					mImageView.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.photo));
				}
			}
			if (fg_HideFav){
				mFavView.setVisibility(View.INVISIBLE);
			}else{
				mFavView.setVisibility(View.VISIBLE);
			}
			mTitleText.setText(tempVideo.getTitle());
			
			if (groupPosition == 0 || groupPosition == 1){
				mDurationText.setText(tempVideo.getDuration());
			}else{
				mDurationText.setText(null);
			}
			if (tempVideo.fg_fav()){
				mFavView.setImageResource(R.drawable.fav);
			}else{
				mFavView.setImageResource(R.drawable.unfav);
			}
			

			mFavView.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (fg_HideFav){
						return ;
					}
					Log.i(TAG, String.format("click %d:%d", groupPosition, childPosition));
					EVideo evideo = getChild(groupPosition, childPosition);
					evideo.set_fav(!evideo.fg_fav());
					if (evideo.fg_fav()){
						mFavView.setImageResource(R.drawable.fav);
					}else{
						mFavView.setImageResource(R.drawable.unfav);
					}
					MainActivity.AddHistory(evideo);
				}
			});
		}
	}
}
