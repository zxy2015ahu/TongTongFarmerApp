package com.tongtong.purchaser.model;

/**
 * Created by Administrator on 2018-05-02.
 */

public class AddressModel {
    private int province;
    private int city;

    public int getProvince() {
        return province;
    }

    public void setProvince(int province) {
        this.province = province;
    }

    public int getCity() {
        return city;
    }

    public void setCity(int city) {
        this.city = city;
    }

    public int getDistrict() {
        return district;
    }

    public void setDistrict(int district) {
        this.district = district;
    }

    private int district;
}
