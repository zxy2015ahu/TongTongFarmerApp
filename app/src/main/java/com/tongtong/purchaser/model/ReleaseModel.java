package com.tongtong.purchaser.model;

public class ReleaseModel {

	private String thumb_img;
	private String releaseVedioThumb;
	private String produce_name;

	public int getFid() {
		return fid;
	}

	public void setFid(int fid) {
		this.fid = fid;
	}

	private int fid;



	public int getId() {
		return id;
	}

	public long getReleaseTime() {
		return releaseTime;
	}

	public void setReleaseTime(long releaseTime) {
		this.releaseTime = releaseTime;
	}

	public void setId(int id) {

		this.id = id;
	}

	private String releaseLocation;
	private long releaseTime;
	private int id;
	private double estimatedQuantity;

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	private String aunit,punit;
	private String startTime;

	public String getAunit() {
		return aunit;
	}

	public void setAunit(String aunit) {
		this.aunit = aunit;
	}

	public String getPunit() {
		return punit;
	}

	public void setPunit(String punit) {
		this.punit = punit;
	}

	private String endTime;
	private double price;
	private double distance;
	public String getThumb_img() {
		return thumb_img;
	}
	public void setThumb_img(String thumb_img) {
		this.thumb_img = thumb_img;
	}
	public String getReleaseVedioThumb() {
		return releaseVedioThumb;
	}
	public void setReleaseVedioThumb(String releaseVedioThumb) {
		this.releaseVedioThumb = releaseVedioThumb;
	}
	public String getProduce_name() {
		return produce_name;
	}
	public void setProduce_name(String produce_name) {
		this.produce_name = produce_name;
	}
	public String getReleaseLocation() {
		return releaseLocation;
	}
	public void setReleaseLocation(String releaseLocation) {
		this.releaseLocation = releaseLocation;
	}
	public double getEstimatedQuantity() {
		return estimatedQuantity;
	}
	public void setEstimatedQuantity(double estimatedQuantity) {
		this.estimatedQuantity = estimatedQuantity;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
}
