package com.tongtong.purchaser.model;

public class MapReleaseModel {

	private int id;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String name;
	private String produce_url;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getProduce_url() {
		return produce_url;
	}
	public void setProduce_url(String produce_url) {
		this.produce_url = produce_url;
	}
	private double lat,lng;
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
}
