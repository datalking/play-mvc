package com.datable.excelbox.core;

import java.io.File;

/**
 * 读取器与生成器直接使用的入口类，暴露了大多常用的读写方法
 *
 * @author jinyaoo
 */
public class ExcelMapper {

    /**
     * 读取excel为java对象
     *
     * @param src       源文件
     * @param valueType 每行数据代表的模型映射class
     * @param <T>       每行数据代表的模型映射
     * @return 整个excel所表示
     */
    public <T> T readExcel(File src, Class<T> valueType) {

        return null;
    }

    /**
     * 根据数据生成excel文件
     *
     * @param resultFile 输出的excel文件
     * @param value      数据
     */
    public void writeExcel(File resultFile, Object value) {

    }


}
