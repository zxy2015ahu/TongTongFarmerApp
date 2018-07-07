package com.tongtong.purchaser.model;

import java.io.Serializable;
import java.util.List;

public class RegionModel implements Serializable{
	private int id;
	private String adcode;
	private String name;
	private int parentId;
	private String parenAdcode;
	List<RegionModel> childrenRegions;


	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAdcode() {
		return adcode;
	}

	public void setAdcode(String adcode) {
		this.adcode = adcode;
	}

	public String getParenAdcode() {
		return parenAdcode;
	}

	public void setParenAdcode(String parenAdcode) {
		this.parenAdcode = parenAdcode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}



	public List<RegionModel> getChildrenRegions() {
		return childrenRegions;
	}

	public void setChildrenRegions(List<RegionModel> childrenRegions) {
		this.childrenRegions = childrenRegions;
	}



	

}
