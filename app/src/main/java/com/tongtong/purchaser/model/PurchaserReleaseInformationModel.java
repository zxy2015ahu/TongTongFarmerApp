package com.tongtong.purchaser.model;

import java.io.Serializable;
import java.util.List;

public class PurchaserReleaseInformationModel implements Serializable {

	private int id;
	private PurchaserModel purchaser;
	private String remarks;

	public int getRelease_count() {
		return release_count;
	}

	public void setRelease_count(int release_count) {
		this.release_count = release_count;
	}

	private ProduceModel produce;


	private int release_count;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public PurchaserModel getPurchaser() {
		return purchaser;
	}

	public void setPurchaser(PurchaserModel purchaser) {
		this.purchaser = purchaser;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public ProduceModel getProduce() {
		return produce;
	}

	public void setProduce(ProduceModel produce) {
		this.produce = produce;
	}

	

}
