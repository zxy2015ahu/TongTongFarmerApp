package com.tongtong.purchaser.model;

public class AccountModel {
	private int year;
	private int month;
	private double income;
	private double outcome;
	private long header_id;

	public long getHeader_id() {
		return header_id;
	}

	public void setHeader_id(long header_id) {
		this.header_id = header_id;
	}

	public int getYear() {

		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getMonth() {
		return month;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public double getIncome() {
		return income;
	}
	public void setIncome(double income) {
		this.income = income;
	}
	public double getOutcome() {
		return outcome;
	}
	public void setOutcome(double outcome) {
		this.outcome = outcome;
	}
	private int id;
	private int type;
	private double amount;
	private String add_time;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public String getAdd_time() {
		return add_time;
	}
	public void setAdd_time(String add_time) {
		this.add_time = add_time;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getTypeid() {
		return typeid;
	}
	public void setTypeid(int typeid) {
		this.typeid = typeid;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public int getSign() {
		return sign;
	}
	public void setSign(int sign) {
		this.sign = sign;
	}
	public int getRel_id() {
		return rel_id;
	}
	public void setRel_id(int rel_id) {
		this.rel_id = rel_id;
	}
	private String description;
	private int typeid;
	private String category;
	private int sign;
	private int rel_id;
}
