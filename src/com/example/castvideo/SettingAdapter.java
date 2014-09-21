package com.example.castvideo;


import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SettingAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<String> mSetting;

	/**
	 * Creates a new MediaAdapter for the given activity.
	 */
	public SettingAdapter(Activity activity, List<String> setting) {
		mInflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mSetting = setting;
	}
	
	@Override
	public int getCount() {
		return mSetting.size();
	}

	@Override
	public String getItem(int position) {
		return mSetting.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		SettingViewHolder viewHolder;

		if (view == null) {
			view = mInflater.inflate(R.layout.item_cast_media, null);
			viewHolder = new SettingViewHolder(view);
			view.setTag(viewHolder);
		} else {
			viewHolder = (SettingViewHolder) view.getTag();
		}
		viewHolder.setPosition(position);
		return view;
        
	}

	private class SettingViewHolder {
		private TextView mVideoTitle;

		public SettingViewHolder(View view) {
			mVideoTitle = (TextView) view
					.findViewById(R.id.item_cast_video_title_textview);
		}

		public void setPosition(int position) {
			Log.e("SettingAdapter", String.format("set position（%d）:%s", position, mSetting.get(position)));
			mVideoTitle.setText(mSetting.get(position));
		}
	}

}
