package com.datable.excelbox.core.model;

import com.datable.excelbox.core.annotation.SheetField;

/**
 * 用于测试的简单模型类
 * 所有属性均为字符串
 */
public class MonarchsOf3KingdomsModel {

    @SheetField(order = 4)
    private String note;

    @SheetField(order = 2)
    private String bigEventInCareer;

    @SheetField(order = 3)
    private String title;

    @SheetField(order = 1)
    private String monarchName;

    public MonarchsOf3KingdomsModel() {
    }

    public MonarchsOf3KingdomsModel(String monarchName, String bigEventInCareer, String title, String note) {
        this.monarchName = monarchName;
        this.bigEventInCareer = bigEventInCareer;
        this.title = title;
        this.note = note;
    }

    public String getMonarchName() {
        return monarchName;
    }

    public void setMonarchName(String monarchName) {
        this.monarchName = monarchName;
    }

    public String getBigEventInCareer() {
        return bigEventInCareer;
    }

    public void setBigEventInCareer(String bigEventInCareer) {
        this.bigEventInCareer = bigEventInCareer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "m3k{" +
                "monarchName='" + monarchName + '\'' +
                ", bigEventInCareer='" + bigEventInCareer + '\'' +
                ", title='" + title + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
