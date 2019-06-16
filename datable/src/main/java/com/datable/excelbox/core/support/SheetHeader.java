package com.datable.excelbox.core.support;

import com.datable.excelbox.core.util.SheetDataUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 一张表表头的元信息
 *
 * @author jinyaoo
 */
public class SheetHeader {
    /**
     * 表头数据对应的模型类
     */
    private Class<?> headerClazz;
    /**
     * 表头各列的集合
     */
    private List<SheetHeaderColumn> columnList = new ArrayList<>();
    /**
     * 表头各列 顺序索引 -> 列信息对象 的映射
     * 顺序索引基于0
     */
    private Map<Integer, SheetHeaderColumn> headerColumnMap = new HashMap<>();

    public SheetHeader() {
    }

    public SheetHeader(Class<?> headerClazz) {
        this.headerClazz = headerClazz;
    }

    public Class<?> getHeaderClazz() {
        return headerClazz;
    }

    public void setHeaderClazz(Class<?> headerClazz) {
        this.headerClazz = headerClazz;
    }

    public List<SheetHeaderColumn> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<SheetHeaderColumn> columnList) {
        this.columnList = columnList;
    }

    public Map<Integer, SheetHeaderColumn> getHeaderColumnMap() {
        return headerColumnMap;
    }

    public void setHeaderColumnMap(Map<Integer, SheetHeaderColumn> headerColumnMap) {
        this.headerColumnMap = headerColumnMap;
    }

    @Override
    public String toString() {
        return "SheetHeader{" +
                "headerClazz=" + headerClazz +
                ", columnList=" + columnList +
                ", headerColumnMap=" + headerColumnMap +
                '}';
    }
}
