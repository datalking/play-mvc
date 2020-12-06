package com.datable.excelbox.core.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 一张sheet表的元信息
 * 可以用来设置读取或生成excel的参数
 *
 * @author jinyaoo
 */
public class SheetSchema {
    /**
     * 当前sheet名称
     */
    private String sheetName;
    /**
     * 当前sheet顺序索引，基于1
     */
    private int sheetIndex;
    /**
     * 当前sheet标题行行数，默认-1，表示无标题行
     */
    private int sheetTitleLineNum = -1;
    /**
     * 当前sheet表头行行数，默认-1，表示无表头
     */
    private int sheetHeaderLineNum = -1;
    /**
     * sheet数据对应的模型类
     */
    private Class<?> clazz;
    /**
     * 表头各列名
     */
    private List<List<String>> fieldNameList;
    /**
     * 各列宽度，即
     */
    private Map<Integer, Integer> colWidthMap = new HashMap<>();
    /**
     * 是否自动设置列宽
     */
    private Boolean autoWidth = false;
    /**
     * 起始行号，默认为0
     */
    private int startRowIndex = 0;

}
