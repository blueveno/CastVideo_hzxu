package com.example.castvideo;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class CastMediaUnit {
	private String mFileName, mTitle;
	private String mUrl;
	private double mSize;
	private int mLength;
	private String mHashCode;
	private int mVideoIdx;
	
	
	public CastMediaUnit(String filename, String url, String size, String length)
	{
		mTitle = filename;
		mFileName = filename;
		mUrl = url;
		
		if (size == null || length == null){
			mSize = 0;
			mLength = 0;
		}else{
			mSize = Double.parseDouble(size.substring(0, size.length()-2));
			mLength = DurationS2I(length);
		}
		GetHashCodeVideoIdx(url);
	}
	
	public int DurationS2I(String length){
		if (length == null){
			return 0;
		}
		String temp[] = length.split(":");
		int ilength = 0;
		
		
		for (int i=temp.length-1; i>=0; i--){
			if (temp.length-1-i == 0){ilength += Integer.parseInt(temp[i]);}
			if (temp.length-1-i == 1){ilength += Integer.parseInt(temp[i])*60;}
			if (temp.length-1-i == 2){ilength += Integer.parseInt(temp[i])*60*60;}
		}
		return ilength;
	}

	private void GetHashCodeVideoIdx(String url)
	{
		Pattern p;
		Matcher m;
		String HashString;
		
		p = Pattern.compile("fileid[^?]*");
		m = p.matcher(url);
		if (m.find())
		{
			HashString = m.group().substring("fileid/".length());
			if (HashString.contains("_")){
				HashString = HashString.split("_")[0];
			}
			mVideoIdx = Integer.parseInt(HashString.substring(8, 10), 16);
			mHashCode = HashString.substring(0, 8)+HashString.substring(10);
			Log.i("S2C", String.format("HashString:%s\n HashIdx:%d ", HashString, mVideoIdx));
		}else{
			mVideoIdx = 0;
			mHashCode = null;
		}
	}
	public String GetHashCode()
	{
		return mHashCode;
	}
	
	public String getFileName()
	{
		return mFileName;
	}
	public Double GetSize(){
		return mSize;
	}
	public int GetLength(){
		return mLength;
	}
	public void SetLength(int length){
		mLength = length;
	}
	public String GetVideoUrl()
	{
		return mUrl;
	}
	public int GetVideoIdx(){
		return mVideoIdx;
	}
	public void SetVideoIdx(int idx){
		mVideoIdx = idx;
	}
	public void SetTitle(String title){
		mTitle = title;
	}
	public String GetTitle(){
		return mTitle;
	}
}