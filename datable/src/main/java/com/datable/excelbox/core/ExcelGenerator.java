package com.datable.excelbox.core;

import java.util.List;

/**
 * excel生成器 抽象类
 *
 * @author jinyaoo
 */
public abstract class ExcelGenerator {

    protected ExcelGenerator() {

    }

    /**
     * 将数据导出excel，无excel模板，无模型映射
     */
    public abstract void writeListOfStringAsExcel(List<?> data, String outputPath);

    /**
     * 将数据导出excel，无excel模板，有模型映射
     */
    public abstract void writeListOfObjectAsExcel(List<?> data, String outputPath);

    /**
     * 将list类型的数据导出excel，有模板，有类型映射
     */
    public abstract void writeListDataAsExcelFromTemplateFile(List<?> data, String outputPath);

//    /**
//     * 根据模板将map类型的数据导出excel
//     */
//    public abstract void writeMapDataAsExcelWithTemplate(List<?> data, String outputPath);

}
