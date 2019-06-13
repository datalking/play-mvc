package com.datable.excelbox.core.model;


import com.datable.excelbox.core.annotation.SheetField;

public class MonarchsOf3KingdomsModel {

    @SheetField(order = 1)
    private String monarchName;

    @SheetField(order = 2)
    private String bigEventInCareer;

    @SheetField(order = 3)
    private String title;

    @SheetField(order = 4)
    private String note;

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
