package com.tongtong.purchaser.model;

public class MySpecificationModel {

	private int id;
	private String name;
	private int parent_id;
	private int produce_id;
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
	public int getParent_id() {
		return parent_id;
	}
	public void setParent_id(int parent_id) {
		this.parent_id = parent_id;
	}
	public int getProduce_id() {
		return produce_id;
	}
	public void setProduce_id(int produce_id) {
		this.produce_id = produce_id;
	}
}
