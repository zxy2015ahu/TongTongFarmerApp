package com.tongtong.purchaser.model;

/**
 * Created by zxy on 2018/3/10.
 */

public class HongBaoModel {
    private String headUrl;
    private String name;

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int is_best() {
        return is_best;
    }

    public void setIs_best(int is_best) {
        this.is_best = is_best;
    }

    private String time;

    public int getHb_id() {
        return hb_id;
    }

    public void setHb_id(int hb_id) {
        this.hb_id = hb_id;
    }

    private double amount;
    private int is_best;
    private int hb_id;
}
