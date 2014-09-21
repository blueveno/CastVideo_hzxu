package com.example.castvideo;

import java.util.ArrayList;
import java.util.List;


import android.util.Log;

/**
 * A storage class containing the title and URL of a piece of playable media.
 */
public class CastMedia {
	private static final String TAG = CastMedia.class.getSimpleName();
	private boolean fgUsed;
	private int mPlayIdx, mDuration, mType;
	private String mHashCode, mTitle, mSize, mImgUrl, mMimeType;
	private boolean fgLocal;
	
	private List<CastMediaUnit> mMediaUnits;
	    
	public CastMedia(String title, String imgurl, int type, String mimetype, boolean fglocal) {
		mHashCode = "";
		fgUsed = false;
		mPlayIdx = 0;
		mTitle = title;
		mImgUrl = imgurl;
		mType = type;
		mMimeType = mimetype;
		fgLocal = fglocal;
		mMediaUnits = new ArrayList<CastMediaUnit>();
	}
	
	public boolean fg_local(){
		return fgLocal;
	}
	public String getImgUrl(){
		return mImgUrl;
	}
	public int getType(){
		return mType;
	}
	public String getMimeType(){
		return mMimeType;
	}
	public boolean AddMedia(String mtitle, String filename, String url, String size, String length){
		CastMediaUnit tempCastMediaUnit = new CastMediaUnit(filename, url, size, length);
		if (! fgUsed){
			fgUsed = true;
			mTitle = mtitle;
			mMediaUnits.add(tempCastMediaUnit);
			mHashCode = tempCastMediaUnit.GetHashCode();
			mDuration = tempCastMediaUnit.DurationS2I(length);
			mSize = size;
			return true;
		}
		else{
			if (mHashCode != null && tempCastMediaUnit.GetHashCode().equals(mHashCode)){
				mTitle = mtitle;
				mMediaUnits.add(tempCastMediaUnit);
				mDuration += tempCastMediaUnit.DurationS2I(length);
				mSize = SizeAdd(mSize, size);
				return true;
			}
			else{
				return false;
			}
		}
	}
	
	public boolean AddMedia(String mtitle, String filename, String url, int idx){

		CastMediaUnit tempCastMediaUnit = new CastMediaUnit(filename, url, null, null);
		tempCastMediaUnit.SetVideoIdx(idx);
		if (! fgUsed){
			fgUsed = true;
			mTitle = mtitle;
			mMediaUnits.add(tempCastMediaUnit);
			mHashCode = null;
			mDuration = 0;
			mSize = null;
			Log.i("", String.format("new mtitle:%s filename:%s url:%s video idx:%d", 
					tempCastMediaUnit.GetTitle(), 
					tempCastMediaUnit.getFileName(), 
					tempCastMediaUnit.GetVideoUrl(),
					tempCastMediaUnit.GetVideoIdx()));
			return true;
		}
		else{
			if (mTitle.equals(mtitle)){
				Log.i("", String.format("append mtitle:%s filename:%s url:%s video:%d", 
						tempCastMediaUnit.GetTitle(), 
						tempCastMediaUnit.getFileName(), 
						tempCastMediaUnit.GetVideoUrl(),
						tempCastMediaUnit.GetVideoIdx()));
				mMediaUnits.add(tempCastMediaUnit);
				return true;
			}
			else{
				return false;
			}
		}
	}
	
	public boolean AddMedia(String mtitle, String filename, String url, int idx, String size, String length){

		CastMediaUnit tempCastMediaUnit = new CastMediaUnit(filename, url, size, length);
		tempCastMediaUnit.SetVideoIdx(idx);
		if (! fgUsed){
			fgUsed = true;
			mTitle = mtitle;
			mMediaUnits.add(tempCastMediaUnit);
			mHashCode = null;
			mDuration = tempCastMediaUnit.DurationS2I(length);
			mSize = size;
			Log.i("", String.format("new mtitle:%s filename:%s url:%s video idx:%d", 
					tempCastMediaUnit.GetTitle(), 
					tempCastMediaUnit.getFileName(), 
					tempCastMediaUnit.GetVideoUrl(),
					tempCastMediaUnit.GetVideoIdx()));
			return true;
		}
		else{
			if (mTitle.equals(mtitle)){
				Log.i("", String.format("append mtitle:%s filename:%s url:%s video:%d", 
						tempCastMediaUnit.GetTitle(), 
						tempCastMediaUnit.getFileName(), 
						tempCastMediaUnit.GetVideoUrl(),
						tempCastMediaUnit.GetVideoIdx()));
				mMediaUnits.add(tempCastMediaUnit);
				mDuration += tempCastMediaUnit.DurationS2I(length);
				mSize = SizeAdd(mSize, size);
				return true;
			}
			else{
				return false;
			}
		}
	}
	
	public String DurationAdd(String dur1, String dur2){
		if (dur1 == null || dur2 == null){
			return null;
		}
		int min, sec;
		min = Integer.parseInt(dur1.split(":")[0]) + Integer.parseInt(dur2.split(":")[0]);
		sec = Integer.parseInt(dur1.split(":")[1]) + Integer.parseInt(dur2.split(":")[1]);
		if (sec >= 60){
			sec -= 60;
			min += 1;
		}
		return String.format("%02d:%02d", min, sec);
		
	}
	public String SizeAdd(String size1, String size2){
		if (size1 == null || size2 == null){
			return null;
		}
		float dsize;
		dsize = Float.parseFloat(size1.substring(0, size1.length()-2)) + Float.parseFloat(size2.substring(0, size2.length()-2));
		return String.format("%.2fMB", dsize);
	}
	
	public int getNextMedia(){
		mPlayIdx ++;
		if (mPlayIdx >= mMediaUnits.size()){
			mPlayIdx = 0;
		}
		return mPlayIdx;
	}
	public int getPrevMedia(){
		if (mPlayIdx > 0) {
			mPlayIdx --;
		}
		return mPlayIdx;		
	}
	public int getCurrMedia(){
		return mPlayIdx;
	}
	public void setCurrMedia(int idx){
		mPlayIdx = idx;
	}
	    

    public String getFileName() {
    	int idx;
    	for (idx = 0; idx<mMediaUnits.size(); idx++){
    		if (mMediaUnits.get(idx).GetVideoIdx() == mPlayIdx){
    			return mMediaUnits.get(idx).getFileName();
    		}
    	}
        return "";
    }

    public String getUrl() {
    	int idx;
    	for (idx = 0; idx<mMediaUnits.size(); idx++){
    		Log.i("", String.format("idx:%d  videoidx:%d playidx:%d", idx, mMediaUnits.get(idx).GetVideoIdx(), mPlayIdx));
    		if (mMediaUnits.get(idx).GetVideoIdx() == mPlayIdx){
    			return mMediaUnits.get(idx).GetVideoUrl();
    		}
    	}
        return mMediaUnits.get(0).GetVideoUrl();
       
    }
    
    public int getSubMediaNum(){
    	return mMediaUnits.size();
    }
    public String getTitle(){
    	return mTitle;
    }
    public double getBasePosition(){
    	double basePos=0;
    	for (int idx=0; idx<mPlayIdx; idx++){
    		basePos += geMediabyIdx(idx).GetLength();
    	}
    	return basePos;
    }
    public String getDuration(){
    	int min, sec, hour;
    	if (mDuration == 0){
    		return "00:00:00";
    	}
    	hour = mDuration/3600;
    	min = mDuration%3600/60;
		sec = mDuration%60;

		return String.format("%02d:%02d:%02d", hour, min, sec);
    }
    public double getDoubleDuration(){
    	return mDuration;
    }
    public int getFcsDuration(){
    	for (int idx = 0; idx<mMediaUnits.size(); idx++){
    		if (mMediaUnits.get(idx).GetVideoIdx() == mPlayIdx){
    			return mMediaUnits.get(idx).GetLength();
    		}
    	}
    	return 0;
    }
    public void setFcsDuration(int length){
    	for (int idx = 0; idx<mMediaUnits.size(); idx++){
    		if (mMediaUnits.get(idx).GetVideoIdx() == mPlayIdx){
    			if (mMediaUnits.get(idx).GetLength() != length){
    				mMediaUnits.get(idx).SetLength(length);
    				mDuration = 0;
    				for (int i = 0; i< mMediaUnits.size(); i++){
    					mDuration += mMediaUnits.get(i).GetLength();
    				}
    				//MainActivity.tabHost.setCurrentTab(0);
    			}
    			break;
    		}
    	}
    }
    public String getSize(){
    	return mSize;
    }
    public String getFileNamebyIdx(int Idx) {
    	int idx;
    	for (idx = 0; idx<mMediaUnits.size(); idx++){
    		if (mMediaUnits.get(idx).GetVideoIdx() == Idx){
    			return mMediaUnits.get(idx).getFileName();
    		}
    	}
        return "";
    }

    public String getUrlbyIdx(int Idx) {
    	int idx;
    	for (idx = 0; idx<mMediaUnits.size(); idx++){
    		if (mMediaUnits.get(idx).GetVideoIdx() == Idx){
    			return mMediaUnits.get(idx).GetVideoUrl();
    		}
    	}
        return "";
    }    
    public CastMediaUnit geMediabyIdx(int Idx) {
    	int idx;
    	for (idx = 0; idx<mMediaUnits.size(); idx++){
    		if (mMediaUnits.get(idx).GetVideoIdx() == Idx){
    			return mMediaUnits.get(idx);
    		}
    	}
        return null;
    } 
    public int getMediaIdxByPos(double pos){
    	double end_pos=0;
    	int location = 0;
    	for (; location<mMediaUnits.size(); location++){
    		end_pos += geMediabyIdx(location).GetLength();
    		if (end_pos > pos || location == mMediaUnits.size()-1){
    			break;
    		}
    	}
    	return location;
    }
    public boolean seekTo(double pos){
    	int dstIdx = getMediaIdxByPos(pos);
    	if (dstIdx != mPlayIdx ){
    		MainActivity.logVIfEnabled(TAG, String.format("Old idx:%d New Idx:%d", mPlayIdx, dstIdx));
    		mPlayIdx = dstIdx;
    		return true;
    	}
    	return false;
    }
}
