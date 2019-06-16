package com.datable.excelbox.core;

import java.util.List;

/**
 * excel生成器 抽象类
 *
 * @author jinyaoo
 */
public abstract class ExcelGenerator {
    /**
     * 使用int中的bit记录启用的生成器特性
     */
    protected int features;

    protected ExcelGenerator() {
    }

    public ExcelGenerator(int features) {
        this.features = features;
    }

    /**
     * Excel生成器支持的特性 枚举类
     */
    public enum Feature {

        QUOTE_CELL_CONTENTS(false),
        WRITE_NUMBER_AS_STRING(false),
        WRITE_BIGDECIMAL_AS_PLAIN(false);

        private final boolean defaultState;

        private final int mask;

        private Feature(boolean state) {
            mask = (1 << ordinal());
            defaultState = state;
        }

        public static int collectDefaults() {
            int flags = 0;
            for (ExcelGenerator.Feature f : values()) {
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
     * 将数据导出excel，无excel模板，无模型映射
     */
    public abstract void writeListOfStringAsExcel(List<?> data, String outputPath);

    /**
     * 将数据导出excel，无excel模板，有模型映射
     */
    public abstract void writeListOfObjectAsExcel(List<?> data, String outputPath);

    public abstract void writeListOfObjectAsExcel(List<?> data);

    /**
     * 将list类型的数据导出excel，有模板，有类型映射
     */
    public abstract void writeListDataAsExcelFromTemplateFile(List<?> data, String outputPath);

//    /**
//     * 根据模板将map类型的数据导出excel
//     */
//    public abstract void writeMapDataAsExcelWithTemplate(List<?> data, String outputPath);

}
