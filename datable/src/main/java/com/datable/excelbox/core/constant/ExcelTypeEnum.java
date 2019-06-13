package com.datable.excelbox.core.constant;

/**
 * @author jinyaoo
 */
public enum ExcelTypeEnum {

    XLS("xls"), XLSX("xlsx");

    private String type;

    ExcelTypeEnum(String excelType) {
        this.type = excelType;
    }

}
