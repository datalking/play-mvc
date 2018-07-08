package com.datable.model;

/**
 * @author yaoo on 7/7/18
 */
public class UserExcelInfo {

    private String userId;

    private Integer excelNum;

    public UserExcelInfo(String userId, Integer excelNum) {
        this.userId = userId;
        this.excelNum = excelNum;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getExcelNum() {
        return excelNum;
    }

    public void setExcelNum(Integer excelNum) {
        this.excelNum = excelNum;
    }
}
