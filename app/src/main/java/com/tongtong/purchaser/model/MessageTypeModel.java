package com.tongtong.purchaser.model;

/**
 * Created by zxy on 2018/4/1.
 */

public class MessageTypeModel {
    private int unread_count;
    private String title;
    private String content;
    private String icon;
    private String add_time;

    public int getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(int msg_type) {
        this.msg_type = msg_type;
    }

    private int msg_type;
    public int getUnread_count() {
        return unread_count;
    }
    public void setUnread_count(int unread_count) {
        this.unread_count = unread_count;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public String getAdd_time() {
        return add_time;
    }
    public void setAdd_time(String add_time) {
        this.add_time = add_time;
    }
}
