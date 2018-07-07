package com.tongtong.purchaser.model;

/**
 * Created by zxy on 2018/4/8.
 */

public class MenuModel {
    public int getBg() {
        return bg;
    }

    public void setBg(int bg) {
        this.bg = bg;
    }

    public MenuModel(String name, int resId, int bg){
        this.name=name;
        this.resId=resId;
        this.bg=bg;

    }
    private int bg;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public boolean is_new() {
        return is_new;
    }

    public void setIs_new(boolean is_new) {
        this.is_new = is_new;
    }

    private String name;
    private int resId;
    private boolean is_new;
}
