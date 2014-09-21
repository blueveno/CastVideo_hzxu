package com.example.castvideo;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.support.v7.media.MediaRouter.RouteInfo;

public class DeviceAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private List<RouteInfo> mRoutes;

	/**
	 * Creates a new MediaAdapter for the given activity.
	 */
	public DeviceAdapter(Activity activity, List<RouteInfo> routes) {
		mContext = activity.getApplicationContext();
		mInflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRoutes = new ArrayList<RouteInfo>();
		RouteInfo temprouteinfo;
		for (int i=0; i<routes.size(); i++){
			temprouteinfo = routes.get(i);
			Log.i("", String.format("route[%d]: id:%s name:%s isdefault:%s, des:%s",
					i,
					temprouteinfo.getId(),
					temprouteinfo.getName(),
					temprouteinfo.isDefault(),
					temprouteinfo.getDescription()));
			if (temprouteinfo.getDescription() != null ){
				mRoutes.add(temprouteinfo);
			}
		}
	}
	
	@Override
	public int getCount() {
		return mRoutes.size();
	}

	@Override
	public RouteInfo getItem(int position) {
		return mRoutes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		DeviceViewHolder viewHolder;

		if (view == null) {
			view = mInflater.inflate(R.layout.item_cast_media, null);
			viewHolder = new DeviceViewHolder(view);
			view.setTag(viewHolder);
		} else {
			viewHolder = (DeviceViewHolder) view.getTag();
		}
		viewHolder.setPosition(position);
		return view;
        
	}

	private class DeviceViewHolder {
		private TextView mVideoTitle;

		public DeviceViewHolder(View view) {
			mVideoTitle = (TextView) view
					.findViewById(R.id.item_cast_video_title_textview);
		}

		public void setPosition(int position) {
			mVideoTitle.setText(mRoutes.get(position).getName());
		}
	}

}
