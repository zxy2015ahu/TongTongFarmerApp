package com.tongtong.purchaser.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zxy on 2018/4/11.
 */

public class ProduceType implements Serializable{
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParent_id() {
        return parent_id;
    }

    public void setParent_id(int parent_id) {
        this.parent_id = parent_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent_name() {
        return parent_name;
    }

    public void setParent_name(String parent_name) {
        this.parent_name = parent_name;
    }

    private int id;
    private int parent_id;

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    private String name;
    private String parent_name;

    public String getFirst_letter() {
        return first_letter;
    }

    public void setFirst_letter(String first_letter) {
        this.first_letter = first_letter;
    }

    private String icon_url;

    public List<ProduceType> getProduceTypes() {
        return produceTypes;
    }

    public void setProduceTypes(List<ProduceType> produceTypes) {
        this.produceTypes = produceTypes;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    private String first_letter;
    private List<ProduceType> produceTypes;
    private int level;
}
