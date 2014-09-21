package com.example.castvideo;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.widget.Toast;

public class AnalyzeMP4Url {
	private static final String TAG = AnalyzeMP4Url.class.getSimpleName();
	private Handler mhandler;//更新给前台的handler
	private static String mUrl, mTitle, mImgUrl;
	public static List<CastMedia> myVideos;
	private Context mContext;
	private boolean fgUsed;
	
	public AnalyzeMP4Url(Context context, Handler handler){
		mContext = context;
		if (myVideos == null) {
			myVideos = new ArrayList<CastMedia>();
		}
		mhandler = handler; 
		fgUsed = false;
	}
	public List<CastMedia> GetVideos(){
		return myVideos;
	}
	public CastMedia GetBestVideo(){
		int temp, submedianum = 0, submediaidx = 0;		
		for (int idx=0; idx<myVideos.size(); idx++){
			temp = myVideos.get(idx).getSubMediaNum();
			if (temp > submedianum){
				submedianum = temp;
				submediaidx = idx;
			}
		}
		MainActivity.logVIfEnabled(TAG, String.format("finall submdiea num:%d title:%s url:%s ", 
				myVideos.get(submediaidx).getSubMediaNum(), 
				myVideos.get(submediaidx).getFileName(),
				myVideos.get(submediaidx).getUrl()));
		return myVideos.get(submediaidx);
	}
	

	public String GetUrl(){
		return mUrl;
	}
	public String GetTitle(){
		return mTitle;
	}
	public boolean GetMP4Addr(String url, String title) {
		if (fgUsed){
			return false;
		}
		fgUsed = true;
		mUrl = url;
		mTitle = title;
		
		if (url.endsWith(".mp4") || url.endsWith(".mkv")){
			String file_name = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
			CastMedia tempVideo = new CastMedia(file_name, null, 0, "video/mp4", false);
			tempVideo.AddMedia(file_name, file_name, url, 0);
			mTitle = file_name;
			myVideos.clear();
			myVideos.add(tempVideo);
			fgUsed = false;
			SendDoneMsg(1, "Pass");
			return true;
		}
		
		if (url.contains("tv.sohu") || url.contains("v.qq")){
			GetSohuMovieTask GetSohuMovieTasktemp = new GetSohuMovieTask();
			GetSohuMovieTasktemp.execute(url);
			return true;
		}
		if (mUrl != null) {
			MainActivity.logVIfEnabled(TAG, mUrl + "\n");
			Pattern p = Pattern
					.compile(".*(http://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?).*");
			Matcher m = p.matcher(mUrl);
			if (m.matches()) {
				String link = m.group(1);
				MainActivity.logVIfEnabled(TAG, "Get Link:" + link + "\n");
				link =link.replace("m.pptv.com", "v.pptv.com");
				NetAndroidTask task = new NetAndroidTask();
				task.execute(link);
				return true;
			} else {
				//Toast toast = Toast.makeText(mContext, "What you shared does not contain a link", Toast.LENGTH_SHORT);
				//toast.show();
				MainActivity.logVIfEnabled(TAG, "Can not find a link!\n");
				fgUsed = false;
				SendDoneMsg(0, "Invalid url");
				return false;
			}
		}
		SendDoneMsg(0, "url is none");
		fgUsed = false;
		return false;
	}
	

	class GetSohuMovieTask extends AsyncTask<String, Integer, String> {

		public String get_sohu_movie(String sohu_url)throws IOException {
			MainActivity.logVIfEnabled(TAG, "open url:"+sohu_url);
	        URL sohu = new URL(sohu_url);
	        URLConnection sohu_con = sohu.openConnection();
	        BufferedReader in = new BufferedReader(new InputStreamReader(
	        		sohu_con.getInputStream(), "UTF-8"));
	        String inputLine;
			Pattern p;
			Matcher m;
	        String url_content = sohu_url;
	        String img_content = null;
	        
	        //FlvxzAnalyze_temp(sohu_url);
			FileWriter fw = new FileWriter("/sdcard/url.txt");  
			
	        while ((inputLine = in.readLine()) != null){
				fw.write(inputLine,0,inputLine.length());  
	        	if (inputLine.contains("og:url")){
	        		MainActivity.logVIfEnabled(TAG, "!!!" + inputLine);
					p = Pattern.compile(".*content=\"(.*)\".*");
					m = p.matcher(inputLine);
					if (m.matches()) {
						MainActivity.logVIfEnabled(TAG, String.format("get sohu-video content[%s] ", m.group(1)));
						url_content = m.group(1);
					}
	        	}else if (inputLine.contains("og:image") || inputLine.contains("itemprop=\"thumbnailUrl\"")){
	        		MainActivity.logVIfEnabled(TAG, "!!!" + inputLine);
					p = Pattern.compile(".*content=\"(.*)\".*");
					m = p.matcher(inputLine);
					if (m.matches()) {
						MainActivity.logVIfEnabled(TAG, String.format("get sohu-image content[%s] ", m.group(1)));
						img_content = m.group(1);
					}
	        	}
	        }
	        in.close();
			fw.flush();  
	        
	        if (url_content != null){
	        	FlvxzGetMP4(FlvxzAnalyze(url_content), img_content);
	        }

	        return null;
	    }
		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			try {
				return get_sohu_movie(arg0[0]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				SendDoneMsg(0, String.format("Get exception:%s when analyze url!", e.getMessage()));
			}			
			return null;
		}
	
	}
	
	private void SendDoneMsg(int fgSuc, String result){
		MainActivity.logVIfEnabled(TAG, String.format("Send msg:%s to MainActivity", result));
		if(mhandler!=null) {  
	        Message message = new Message();  
	        message.obj = result;  
	        message.arg1 = fgSuc;
	        mhandler.sendMessage(message);//更新消息  
			MainActivity.logVIfEnabled(TAG, String.format("Send msg:%s to MainActivity done", result));
        } 
		fgUsed = false;
	}	
	
	public String FlvxzAnalyze_temp(String link){
		AndroidHttpClient client = null;
		HttpGet httpRequest = null;
		HttpResponse httpResponse = null;
		String result = null;

		try {
			String httpUrl = link;
			httpUrl = httpUrl.trim();
			httpUrl = httpUrl.replace("=$","");
			MainActivity.logVIfEnabled(TAG, "Http Url:" + httpUrl + "\n");
			// 构造AndroidHttpClient的实例
			client = AndroidHttpClient
					.newInstance("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.49 Safari/537.36");
			// 创建 HttpGet 方法，该方法会自动处理 URL 地址的重定向
			httpRequest = new HttpGet(httpUrl);
			// 请求HttpClient，取得HttpResponse
			MainActivity.logVIfEnabled(TAG, "Get:" + httpUrl + "\n");
			Header httpRequestHeader[] = httpRequest.getAllHeaders();
			httpRequest.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			httpRequest.setHeader("Accept-Encoding", "deflate,sdch");
			httpRequest.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4");
			httpRequest.setHeader("Cookie", "bdshare_firstime=1376284860930; PHPSESSID=slpd6jfl1dgmgdn3o0f0glqns7; AJSTAT_ok_times=3");
			MainActivity.logVIfEnabled(TAG, "Header number:" + httpRequestHeader.length + "\n");
			for (int i = 0; i < httpRequestHeader.length; i++) {
				MainActivity.logVIfEnabled(TAG, httpRequestHeader[i].getName() + ":"
						+ httpRequestHeader[i].getValue());
			}
			httpResponse = client.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 取得返回的字符串
				MainActivity.logVIfEnabled(TAG, "Result get\n");
				MainActivity.logVIfEnabled(TAG, httpResponse.getStatusLine().getStatusCode()
						+ "\n");
				String strResult = EntityUtils.toString(httpResponse
						.getEntity());
				Header httpResponseHeader[] = httpResponse.getAllHeaders();
				for (int i = 0; i < httpResponseHeader.length; i++) {
					MainActivity.logVIfEnabled(TAG, httpResponseHeader[i].getName() + ":"
							+ httpResponseHeader[i].getValue());
				}
				result = strResult;
				

				FileWriter fw = new FileWriter("/sdcard/flvxz_temp.txt");  
				fw.write(result,0,result.length());  
				fw.flush();  
			} else {
				MainActivity.logVIfEnabled(TAG, httpResponse.getStatusLine().getStatusCode()
						+ "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
			MainActivity.logVIfEnabled(TAG, "HTTP Exception:" + e.getMessage() + "\n");
		} finally {
			// 关闭连接
			if (client != null)
				client.close();
		}
		return result;
	}
	
	public String FlvxzAnalyze(String link){
		AndroidHttpClient client = null;
		HttpGet httpRequest = null;
		HttpResponse httpResponse = null;
		String result = null;

		try {
			link = link.replace("http://", "http:##");

			link = Base64.encodeToString(link.getBytes(), Base64.NO_WRAP);
			String httpUrl = "http://api.flvxz.com/url/" + link;
			httpUrl = httpUrl.trim();
			httpUrl = httpUrl.replace("=$","");
			MainActivity.logVIfEnabled(TAG, "Http Url:" + httpUrl + "\n");
			// 构造AndroidHttpClient的实例
			client = AndroidHttpClient
					.newInstance("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.49 Safari/537.36");
			// 创建 HttpGet 方法，该方法会自动处理 URL 地址的重定向
			httpRequest = new HttpGet(httpUrl);
			// 请求HttpClient，取得HttpResponse
			MainActivity.logVIfEnabled(TAG, "Get:" + httpUrl + "\n");
			Header httpRequestHeader[] = httpRequest.getAllHeaders();
			httpRequest.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			httpRequest.setHeader("Accept-Encoding", "deflate,sdch");
			httpRequest.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4");
			httpRequest.setHeader("Cookie", "bdshare_firstime=1376284860930; PHPSESSID=slpd6jfl1dgmgdn3o0f0glqns7; AJSTAT_ok_times=3");
			MainActivity.logVIfEnabled(TAG, "Header number:" + httpRequestHeader.length + "\n");
			for (int i = 0; i < httpRequestHeader.length; i++) {
				MainActivity.logVIfEnabled(TAG, httpRequestHeader[i].getName() + ":"
						+ httpRequestHeader[i].getValue());
			}
			httpResponse = client.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 取得返回的字符串
				MainActivity.logVIfEnabled(TAG, "Result get\n");
				MainActivity.logVIfEnabled(TAG, httpResponse.getStatusLine().getStatusCode()
						+ "\n");
				String strResult = EntityUtils.toString(httpResponse
						.getEntity());
				Header httpResponseHeader[] = httpResponse.getAllHeaders();
				for (int i = 0; i < httpResponseHeader.length; i++) {
					MainActivity.logVIfEnabled(TAG, httpResponseHeader[i].getName() + ":"
							+ httpResponseHeader[i].getValue());
				}
				result = strResult;
			} else {
				MainActivity.logVIfEnabled(TAG, httpResponse.getStatusLine().getStatusCode()
						+ "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
			MainActivity.logVIfEnabled(TAG, "HTTP Exception:" + e.getMessage() + "\n");
		} finally {
			// 关闭连接
			if (client != null)
				client.close();
		}
		return result;
	}
	public void FlvxzGetMP4_sohu(String result, String img_url){
		// 返回HTML页面的内容
		try {
			MainActivity.logVIfEnabled(TAG, "Result:" + result);
			FileWriter fw = new FileWriter("/sdcard/flvxz.txt");  
			fw.write(result,0,result.length());  
			fw.flush();  
			boolean new_mp4 = false;
			String lines[] = result.split("<video>");
			Pattern p;
			Matcher m;
			String format, size, length;
			mTitle = "";
			mImgUrl = img_url;

			for (int i = 0; i < lines.length; i++) {
				if (lines[i].contains("</video>")){
					String[] temp_list = lines[i].split("<br/>");
					String temp_str, file_name="", url="";
					int video_idx=0;
					boolean fg_single_file = true;
					format = "unknown";
					size = null;
					length = null;
					
					p = Pattern.compile(".*\\[(.*)\\].*");
					m = p.matcher(temp_list[0]);
					if (m.matches()) {
						format = m.group(1);
						MainActivity.logVIfEnabled(TAG, "format" + i + ":" + format + "\n");
					}
					
					if (temp_list[0].contains("style=\"color:red;\"")){
						fg_single_file = false;
					}
					for (int temp_idx=1; temp_idx<temp_list.length; temp_idx++){
						temp_str = temp_list[temp_idx];
						MainActivity.logVIfEnabled(TAG, "temp_str:" + temp_str);
						if (!temp_str.contains(".mp4")){
							continue;
						}
						
						p = Pattern.compile(".*dropdown pull-right\">(.*).*");
						m = p.matcher(temp_str);
						if (m.matches()) {
							MainActivity.logVIfEnabled(TAG, "get@@@@@@@@@@@@ "+ m.group(1));
							if (m.group(1).split(" ")[2].contains("button") ){
								size = m.group(1).split(" ")[0];
								length = m.group(1).split(" ")[1];
								MainActivity.logVIfEnabled(TAG, "get size @@@@@@@@@@@@ "+ size);
								MainActivity.logVIfEnabled(TAG, "get length @@@@@@@@@@@@ "+ length);
							}
						}
						
						String[] temp_list2 = temp_str.split("<");
						
						for (int temp_idx2 =0; temp_idx2<temp_list2.length; temp_idx2++){
							if (temp_list2[temp_idx2].contains("a href=\"")){
								file_name = temp_list2[temp_idx2].split(">")[1];
								url = temp_list2[temp_idx2].split("\"")[1];
							}
						}


						
						if (file_name.contains(".") && file_name.contains(".mp4")){
							MainActivity.logVIfEnabled(TAG, "file name "+file_name);
							int pos = file_name.indexOf(".");
							file_name= file_name.substring(0, pos);
							if (fg_single_file || !file_name.contains("_")){
								mTitle= file_name;
								video_idx = 0;
							}else{
								MainActivity.logVIfEnabled(TAG, "file name "+file_name);
								pos = file_name.lastIndexOf("_");
								String chapter_str = file_name.substring(pos+1);	
								MainActivity.logVIfEnabled(TAG, String.format("chapter_str:%s mtitle:%s", chapter_str, file_name.substring(0, pos)));
								p = Pattern.compile("[0-9].*");

								if (chapter_str.length() == 2 && p.matcher(chapter_str).matches()){
									//mTitle = temp_str
									mTitle = file_name.substring(0, pos);
									video_idx = Integer.parseInt(chapter_str)-1;
								}else{
									//error
									mTitle = file_name.split(".")[0];
								}
							}
						}else{
							continue;
						}

						if (new_mp4 == false) {
							new_mp4 = true;
							myVideos.clear();
							//Toast toast = Toast.makeText(mContext, "Mp4 address get, tap to select!", Toast.LENGTH_SHORT);
							//toast.show();
						}


						boolean fgAdd = false;
						for (int idx=0; idx<myVideos.size(); idx++){
							if (size == null || length == null ) {								
								if (myVideos.get(idx).AddMedia(String.format("[%s]%s", format, mTitle), file_name, url, video_idx)){
									fgAdd = true;
									break;
								}
							}else{
								if (myVideos.get(idx).AddMedia(String.format("[%s]%s", format, mTitle), file_name, url, video_idx, size, length)){
									fgAdd = true;
									break;
								}
							}
						}
						if (fgAdd == false)
						{
							CastMedia tempVideo = new CastMedia(String.format("[%s]%s", format, mTitle), mImgUrl, 0, "video/mp4", false);
							if (size == null || length == null ) {
								tempVideo.AddMedia(String.format("[%s]%s", format, mTitle), file_name, url, video_idx);
							}else{
								tempVideo.AddMedia(String.format("[%s]%s", format, mTitle), file_name, url, video_idx, size, length);
							}
							myVideos.add(tempVideo);
						}
					}
				}
			} 

			if (new_mp4 == false) {
				//Toast toast = Toast.makeText(mContext, "Mp4 is not supported for this video!", Toast.LENGTH_SHORT);
				//toast.show();
				SendDoneMsg(0, "Do not find any MP4 files in this URL");
			} else {
				//post message(myVideos)
				SendDoneMsg(1, "Pass");
			}
		}catch (Exception e) {
			e.printStackTrace();
			SendDoneMsg(0, String.format("Get exception:%s when analyze url!", e.getMessage()));
			MainActivity.logVIfEnabled(TAG, "URL Exception:" + e.getMessage() + "\n");
		}
	}
	
	
		
	public void FlvxzGetMP4(String result, String imgurl){
		// 返回HTML页面的内容
		try {
			MainActivity.logVIfEnabled(TAG, "Result:" + result);
			FileWriter fw = new FileWriter("/sdcard/flvxz.txt");  
			fw.write(result,0,result.length());  
			fw.flush();  
			boolean new_mp4 = false;
			String lines[] = result.split("<video>");
			int video_idx=0;
			Pattern p;
			Matcher m;
			String format, size, length;
			mTitle = "";
			mImgUrl = imgurl;
			for (int i = 0; i < lines.length; i++) {
				if (!lines[i].contains("</video>") || (!lines[i].contains("<ftype>mp4</ftype>") && !lines[i].contains(".mp4"))){
					continue;
				}
				
				if (lines[i].contains("<title>") && lines[i].contains("</title>"))
				{
					mTitle = lines[i].substring(lines[i].indexOf("<title>"), lines[i].indexOf("</title>"));
					mTitle = mTitle.substring(mTitle.lastIndexOf("[")+1, mTitle.indexOf("]"));
				}
				
				format = "";
				if (lines[i].contains("<quality>") && lines[i].contains("</quality>"))
				{
					format = lines[i].substring(lines[i].indexOf("<quality>"), lines[i].indexOf("</quality>"));
					format = format.substring(format.lastIndexOf("[")+1, format.indexOf("]"));
				}
				

				if (lines[i].contains("<img>") && lines[i].contains("</img>"))
				{
					mImgUrl = lines[i].substring(lines[i].indexOf("<img>"), lines[i].indexOf("</img>"));
					mImgUrl = mImgUrl.substring(mImgUrl.lastIndexOf("[")+1, mImgUrl.indexOf("]"));
				}
				
				
				
				MainActivity.logVIfEnabled(TAG, "format" + i + ":" + format + "\n");
				String files[] = lines[i].split("<file>");
				for (int j = 0; j < files.length; j++) {
					if (!files[j].contains("</file>")){
						continue;
					}
					String url;
					MainActivity.logVIfEnabled(TAG, "file" + j + ":" + files[j] + "\n");
					size = null;
					length = null;
					if (files[j].contains("<size>") && files[j].contains("MB</size>")){
						size = files[j].substring(files[j].lastIndexOf("<size>")+"<size>".length(), files[j].lastIndexOf("MB</size>"));
						size += "MB";
						MainActivity.logVIfEnabled(TAG, "get size @@@@@@@@@@@@ "+ size);
					}
					if (files[j].contains("<seconds>") && files[j].contains("</seconds>")){
						length = files[j].substring(files[j].indexOf("<seconds>")+"<seconds>".length(), files[j].indexOf("</seconds>"));
						MainActivity.logVIfEnabled(TAG, "get length @@@@@@@@@@@@ "+ length);						
					}
					url = null;
					if (files[j].contains("<furl>") && files[j].contains("</furl>"))
					{
						url = files[j].substring(files[j].indexOf("<furl>"), files[j].indexOf("</furl>"));
						url = url.substring(url.lastIndexOf("[")+1, url.indexOf("]"));
					}
					if (url != null) {
						if (new_mp4 == false) {
							new_mp4 = true;
							myVideos.clear();
							//Toast toast = Toast.makeText(mContext, "Mp4 address get, tap to select!", Toast.LENGTH_SHORT);
							//toast.show();
						}
						boolean fgAdd = false;
						String file_name = String.format("%s_%d", mTitle, video_idx+1);
						for (int idx=0; idx<myVideos.size(); idx++){
							if (size == null || length == null ) {								
								if (myVideos.get(idx).AddMedia(String.format("[%s]%s", format, mTitle), file_name, url, video_idx)){
									fgAdd = true;
									break;
								}
							}else{
								if (myVideos.get(idx).AddMedia(String.format("[%s]%s", format, mTitle), file_name, url, video_idx, size, length)){
									fgAdd = true;
									break;
								}
							}
						}
						if (fgAdd == false)
						{
							CastMedia tempVideo = new CastMedia(String.format("[%s]%s", format, mTitle), mImgUrl, 0, "video/mp4", false);
							if (size == null || length == null ) {
								tempVideo.AddMedia(String.format("[%s]%s", format, mTitle), file_name, url, video_idx);
							}else{
								tempVideo.AddMedia(String.format("[%s]%s", format, mTitle), file_name, url, video_idx, size, length);
							}
							myVideos.add(tempVideo);
						}
						video_idx ++;
					}
				}
			}
			if (new_mp4 == false) {
				//Toast toast = Toast.makeText(mContext, "Mp4 is not supported for this video!", Toast.LENGTH_SHORT);
				//toast.show();
				SendDoneMsg(0, "Do not find any MP4 files");
			} else {
				//post message(myVideos)
				SendDoneMsg(1, "Pass");
			}
		} catch (Exception e) {
			e.printStackTrace();
			SendDoneMsg(0, String.format("Get exception:%s when analyze url!", e.getMessage()));
			MainActivity.logVIfEnabled(TAG, "URL Exception:" + e.getMessage() + "\n");
		}
	}
	class NetAndroidTask extends AsyncTask<String, Integer, String> {
		// 构造函数
		public NetAndroidTask() {
		}

		@Override
		protected String doInBackground(String... params) {
			// 声明变量
			return FlvxzAnalyze(params[0]);
		}		

		@Override
		protected void onPostExecute(String result) {
			FlvxzGetMP4(result, null);
		}
	}

}