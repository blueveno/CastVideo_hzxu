package com.example.castvideo;

import java.io.Serializable;

import java.io.Serializable;

import android.graphics.Bitmap;

public class EVideo implements Serializable{
	private int id;
	private int format; //0 video 1  audio 2 image
	private String name;
	private String title;
	private String album;
	private String songer;
	private String path;
	private String duration;
	private String size;
	private String year;
	private String type;
	private int state;//0=not checked,1=checked,2=processing,3=completed
	private String description;
	private String ffmpegPath;
	private Bitmap bmp;
	private boolean fglocal;
	private float percentage;
	private boolean fgFav;
	
	public boolean fg_local(){
		return fglocal;
	}
	public boolean fg_fav(){
		return fgFav;
	}
	public void set_fav(boolean fav){
		fgFav = fav;
	}
	public void set_percentage(float pt){
		percentage = pt;
	}
	public float get_percentage(){
		return percentage;
	}
	
	public void setFormat(int fm){
		format = fm;
	}
	public int getFormat(){
		return format;
	}
	
	public Bitmap getBmp() {
		return bmp;
	}
	public void setBmp(Bitmap bmp) {
		this.bmp = bmp;
	}
	public String getFfmpegPath() {
		return ffmpegPath;
	}
	public void setFfmpegPath(String ffmpegPath) {
		this.ffmpegPath = ffmpegPath;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public EVideo(boolean fglocal_media) {
		super();
		fglocal = fglocal_media;
		percentage = 0;
		fgFav = false;
	}
	public EVideo(int id, String name, String title, String album,
			String songer, String path, String duration, String size,
			String year, String type) {
		super();
		this.id = id;
		this.name = name;
		this.title = title;
		this.album = album;
		this.songer = songer;
		this.path = path;
		this.duration = duration;
		this.size = size;
		this.year = year;
		this.type = type;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public String getSonger() {
		return songer;
	}
	public void setSonger(String songer) {
		this.songer = songer;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}