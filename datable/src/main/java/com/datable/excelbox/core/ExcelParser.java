package com.datable.excelbox.core;

import java.util.Iterator;
import java.util.List;

/**
 * excel解析器 抽象类
 *
 * @author jinyaoo
 */
public abstract class ExcelParser {
    /**
     * 使用int中的bit记录启用的解析器特性
     */
    protected int features;

    protected ExcelParser() {
    }


    protected ExcelParser(int features) {
        features = features;
    }

    /**
     * Excel解析器支持的特性 枚举类
     */
    public enum Feature {

        IGNORE_CHART_IN_SHEET(true),
        IGNORE_PIVOT_TABLE_IN_SHEET(true),
        IGNORE_COMMENT(true),
        IGNORE_STYLE(true),
        SUPPORT_PAGINATION(false);

        private final boolean defaultState;

        private final int mask;

        private Feature(boolean state) {
            mask = (1 << ordinal());
            defaultState = state;
        }

        public static int collectDefaults() {
            int flags = 0;
            for (ExcelParser.Feature f : values()) {
                if (f.enabledByDefault()) {
                    flags |= f.getMask();
                }
            }
            return flags;
        }


        public boolean enabledByDefault() {
            return defaultState;
        }

        public int getMask() {
            return mask;
        }
    }

    /**
     * 读excel，无模型映射，内容全部保存为String
     */
    public abstract List<List<String>> readAsListOfString();

    /**
     * 读excel，有模型映射，内容保存为java bean对象
     */
    public abstract <T> List<T> readAsListOfObject();
    public abstract <T> List<T> readAsListOfObject(Class<?> clazz);

//    /**
//     * 读excel，为了和jackson的接口兼容
//     */
//    public abstract <T> Iterator<T> readExcel();

    public ExcelParser enable(Feature f) {
        features |= f.getMask();
        return this;
    }

    /**
     *
     */
    public ExcelParser disable(Feature f) {
        features &= ~f.getMask();
        return this;
    }
}
