package com.tongtong.purchaser.model;

import java.io.Serializable;

public class FarmerReleaseInformationModel implements Serializable{
	private int id;
	private FarmerModel farmer;
	private String releaseTime;
	private String releaseLocation;

	public String getThumb_img() {
		return thumb_img;
	}

	public void setThumb_img(String thumb_img) {
		this.thumb_img = thumb_img;
	}

	private double releaseLongitude;
	private double releaseLatitude;
	private String thumb_img;
	private String geoHash;
	private String releaseVedioUrl;
	private String releaseVedioThumb;
	private ProduceModel produce;
	private double estimatedQuantity;
	private String startTime;
	private String endTime;
	private double unitPrice;
	private int state;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	

	public FarmerModel getFarmer() {
		return farmer;
	}

	public void setFarmer(FarmerModel farmer) {
		this.farmer = farmer;
	}

	public String getReleaseTime() {
		return releaseTime;
	}

	public void setReleaseTime(String releaseTime) {
		this.releaseTime = releaseTime;
	}

	public String getReleaseLocation() {
		return releaseLocation;
	}

	public void setReleaseLocation(String releaseLocation) {
		this.releaseLocation = releaseLocation;
	}

	public double getReleaseLongitude() {
		return releaseLongitude;
	}

	public void setReleaseLongitude(double releaseLongitude) {
		this.releaseLongitude = releaseLongitude;
	}

	public double getReleaseLatitude() {
		return releaseLatitude;
	}

	public void setReleaseLatitude(double releaseLatitude) {
		this.releaseLatitude = releaseLatitude;
	}

	public String getGeoHash() {
		return geoHash;
	}

	public void setGeoHash(String geoHash) {
		this.geoHash = geoHash;
	}

	public String getReleaseVedioUrl() {
		return releaseVedioUrl;
	}

	public void setReleaseVedioUrl(String releaseVedioUrl) {
		this.releaseVedioUrl = releaseVedioUrl;
	}

	

	public String getReleaseVedioThumb() {
		return releaseVedioThumb;
	}

	public void setReleaseVedioThumb(String releaseVedioThumb) {
		this.releaseVedioThumb = releaseVedioThumb;
	}

	public ProduceModel getProduce() {
		return produce;
	}

	public void setProduce(ProduceModel produce) {
		this.produce = produce;
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

	public double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

}
