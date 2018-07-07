package com.tongtong.purchaser.model;

/**
 * Created by Administrator on 2018-02-03.
 */

public class OrderModel {
    public int getDingjin() {
        return dingjin;
    }

    private String farmer_name;

    public String getFarmer_name() {
        return farmer_name;
    }

    public void setFarmer_name(String farmer_name) {
        this.farmer_name = farmer_name;
    }

    public String getFarmer_phone() {
        return farmer_phone;
    }

    public void setFarmer_phone(String farmer_phone) {
        this.farmer_phone = farmer_phone;
    }

    public String getStatus_name() {
        return status_name;


    }
    private String farmer_phone;
    public void setStatus_name(String status_name) {
        this.status_name = status_name;
    }
    private int order_id;

    public void setDingjin(int dingjin) {
        this.dingjin = dingjin;
    }

    private int dingjin;
    private int jinzhong;
    private double total;
    private int purchase_id;

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    private String order_no;
    private String purchaser_name;
    private String icon_url;
    private String unit;

    public String getPurchaser_phone() {
        return purchaser_phone;
    }

    public void setPurchaser_phone(String purchaser_phone) {
        this.purchaser_phone = purchaser_phone;
    }

    private String produce_name;
    private String add_time;
    private String status_name;
    private String purchaser_phone;
    public String getAdd_time() {
        return add_time;
    }
    public void setAdd_time(String add_time) {
        this.add_time = add_time;
    }
    public int getOrder_id() {
        return order_id;
    }
    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }
    public int getJinzhong() {
        return jinzhong;
    }
    public void setJinzhong(int jinzhong) {
        this.jinzhong = jinzhong;
    }
    public double getTotal() {
        return total;
    }
    public void setTotal(double total) {
        this.total = total;
    }
    public int getPurchase_id() {
        return purchase_id;
    }
    public void setPurchase_id(int purchase_id) {
        this.purchase_id = purchase_id;
    }
    public String getPurchaser_name() {
        return purchaser_name;
    }
    public void setPurchaser_name(String purchaser_name) {
        this.purchaser_name = purchaser_name;
    }
    public String getIcon_url() {
        return icon_url;
    }
    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }
    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
    public String getProduce_name() {
        return produce_name;
    }
    public void setProduce_name(String produce_name) {
        this.produce_name = produce_name;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    private double price;
    private int amount;
    private int status;
}
