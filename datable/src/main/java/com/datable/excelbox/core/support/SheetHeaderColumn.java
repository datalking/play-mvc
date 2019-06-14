package com.datable.excelbox.core.support;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 表头行一个列的元信息
 *
 * @author jinyaoo
 */
public class SheetHeaderColumn implements Comparable<SheetHeaderColumn> {
    /**
     * 该列名称
     */
    private String title;
    /**
     * 列顺序索引，基于0
     */
    private int order = 99999;
    /**
     * 列变量
     */
    private Field field;
    /**
     * 未使用
     */
    private List<String> head = new ArrayList<>();
    /**
     * 未使用
     */
    private String format;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public List<String> getHead() {
        return head;
    }

    public void setHead(List<String> head) {
        this.head = head;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public int compareTo(SheetHeaderColumn o) {
        return order - o.order;
    }

    @Override
    public String toString() {
        return "SheetColumn{" +
                "title='" + title + '\'' +
                ", order=" + order +
                ", field=" + field +
                ", head=" + head +
                ", format='" + format + '\'' +
                '}';
    }
}
