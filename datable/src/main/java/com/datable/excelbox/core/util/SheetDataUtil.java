package com.datable.excelbox.core.util;

import java.util.List;

/**
 * 表格数据处理工具类，如添加空字符串
 *
 * @author jinyaoo
 */
public final class SheetDataUtil {

    /**
     * 按照表格数据的最大列数，将不足最大列数的行右侧填充空
     * 07格式excel最大支持100万行，list返回int能满足需求
     *
     * @param data 输入数据
     * @return 列数相等的表格数据
     */
    public static List<List<String>> matrixDataToSameColumn(List<List<String>> data) {

        if (data == null || data.size() == 0) {
            return null;
        }

        int maxColNum = getMaxColNum(data);

        int rowNum = data.size();
        for (int i = 0; i < rowNum; i++) {

            List<String> curRowList = data.get(i);

            if (curRowList.size() < maxColNum) {
                for (int j = 0; j < maxColNum - curRowList.size(); j++) {
                    curRowList.add("");
                }
            }

        }

        return data;
    }

    /**
     * 获取表格数据的最大列数
     */
    public static int getMaxColNum(List<List<String>> data) {
        int rowNum = data.size();
        int maxColNum = 0;
        for (int i = 0; i < rowNum; i++) {

            if (maxColNum < data.get(i).size()) {
                maxColNum = data.get(i).size();
            }

        }
        return maxColNum;
    }

}
