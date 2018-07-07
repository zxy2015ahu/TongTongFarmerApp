package com.tongtong.purchaser.model;

import java.io.Serializable;

public class ProduceModel implements Serializable{

	private int id;
	private String name;
	private String alias;
	private String iconUrl;
	private String unit;

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	private int state;
	private int level;
	private ProduceTypeModel produceType;
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
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getIconUrl() {
		return iconUrl;
	}
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public ProduceTypeModel getProduceType() {
		return produceType;
	}
	public void setProduceType(ProduceTypeModel produceType) {
		this.produceType = produceType;
	}
	
}
