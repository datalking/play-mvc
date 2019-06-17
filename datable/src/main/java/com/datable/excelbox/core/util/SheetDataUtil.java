package com.datable.excelbox.core.util;

import com.datable.excelbox.core.annotation.SheetField;
import com.datable.excelbox.core.support.SheetHeaderColumn;
import com.datable.excelbox.core.support.SheetHeader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表格数据处理工具类，如添加空字符串
 *
 * @author jinyaoo
 */
public final class SheetDataUtil {

    /**
     * 按照表格数据的最大列数，将不足最大列数的行右侧填充空字符串
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
     * @param data 字符串列表
     * @return 所有行中最大的列数
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

    /**
     * 根据模型类确定模型类属性与列的映射关系
     *
     * @param headerClazz 该sheet数据对应的模型类
     * @return 表头对象
     */
    public static SheetHeader getSheetHeaderFromAnnotatedClass(Class<?> headerClazz) {
        SheetHeader sheetHeader = new SheetHeader();
        sheetHeader.setHeaderClazz(headerClazz);

        /// 获取模型类的所有字段
        List<Field> fieldList = new ArrayList<>();
        for (Class c = headerClazz; c != Object.class; c = c.getSuperclass()) {
            fieldList.addAll(Arrays.asList(c.getDeclaredFields()));
        }

        for (Field f : fieldList) {

            /// 若该模型类属性包含注解，则说明对应表中一列
            if (f.isAnnotationPresent(SheetField.class)) {
                SheetField anno = f.getAnnotation(SheetField.class);
                // 创建列对象
                SheetHeaderColumn sheetColumn = new SheetHeaderColumn();
                sheetColumn.setTitle(anno.title());
                sheetColumn.setField(f);
                sheetColumn.setHead(Arrays.asList(anno.value()));
                int order = anno.order();
                if (order < 1) {
                    throw new RuntimeException(headerClazz + " invalid order number. order indicates the index of the field.");
                }
                sheetColumn.setOrder(order - 1);

                sheetHeader.getColumnList().add(sheetColumn);
                sheetHeader.getHeaderColumnMap().put(order - 1, sheetColumn);
            }
        }

        Collections.sort(sheetHeader.getColumnList());

        return sheetHeader;
    }

    /**
     * 若order未指定，则根据表中列名顺序调整模型类属性的顺序
     *
     * @param headerRow 表头行
     * @param sHeader   表头信息
     */
    public static void sortSheetHeaderColumnByTitle(Row headerRow, SheetHeader sHeader) {

        Map<Integer, SheetHeaderColumn> map = new HashMap<>();

        /// 寻找模型类属性与列的对应关系
        for (Cell c : headerRow) {
            String cVal = POIBasedUtil.getCellValueAsString(c);
            for (SheetHeaderColumn col : sHeader.getColumnList()) {

                /// 若该列的order无效，则根据表中列名顺序调整map
                if (col.getOrder() > 16384) {
                    if ((cVal.trim()).equals(col.getTitle()) || cVal.trim().equals(col.getField().getName())) {
                        map.put(c.getColumnIndex(), col);
                    }
                }
            }
        }

        if (!map.isEmpty()) {
            sHeader.setHeaderColumnMap(map);
        }
    }

    /**
     * 根据表中一行数据创建相应的对象
     *
     * @param row         基于poi的一行
     * @param sheetHeader 表头信息
     * @return 代表一行数据的对象
     */
    public static Object createRowObjectFromRowData(Row row, SheetHeader sheetHeader) {

        int cellIndexLast = row.getLastCellNum();
        Object rowObj = null;
        try {
            // 代表当前行数据的对象
            rowObj = sheetHeader.getHeaderClazz().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        // 遍历该行所有单元格
        for (int j = 0; j < cellIndexLast; j++) {
            Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            // 先将单元格内容读取为字符串
            String cellValStr = POIBasedUtil.getCellValueAsString(cell);
            // 从表头中获取该列元信息
            SheetHeaderColumn col = sheetHeader.getHeaderColumnMap().get(j);
            Field field = col.getField();
            // 根据该列注解类型将字符串转换成对象
            Object obj = TypeUtil.convertStrToTargetClassObj(cellValStr, field.getType());
            /// 设置为该行数据对象的一个属性
            field.setAccessible(true);
            try {
                field.set(rowObj, obj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return rowObj;
    }

    /**
     * 将list中的数据创建一张表sheet
     *
     * @param list         建表用的数据
     * @param workbook     写入数据的excel
     * @param sheetName    表名
     * @param headerRowNum 表头行数
     */
    public static void createOneSheetFormList(List<?> list, Workbook workbook, String sheetName, int headerRowNum) {
        Sheet sheet = workbook.createSheet(sheetName);
        // 若没有表头，则生成空excel
        if (headerRowNum == 0) {
            // 若没有数据
            if (list.size() == 0) {
                return;
            }
        }

        // todo 列表中无数据
        Class headerClazz = list.get(0).getClass();
        SheetHeader sheetHeader = getSheetHeaderFromAnnotatedClass(headerClazz);
        Map<Integer, SheetHeaderColumn> colMap = sheetHeader.getHeaderColumnMap();

        // 若有表头，则创建表头行
        if (headerRowNum > 0) {
            Row row0 = sheet.createRow(0);
            for (int j = 0; j < colMap.size(); j++) {
                // todo 测试大于128无缓存的key是否匹配
                String colName = colMap.get(j).getTitle();
                // 若注解中不包含列名或列名为空字符串，则默认使用模型类属性名作为列名
                if (colName == null || colName.equals("")) {
                    colName = colMap.get(j).getField().getName();
                }
                row0.createCell(j).setCellValue(colName);
            }
        }
        // 将每个对象分别写入一行
        for (int i = 0; i < list.size(); i++) {
            Object curObj = list.get(i);
            Row row;
            if (headerRowNum > 0) {
                row = sheet.createRow(i + 1);
            } else {
                row = sheet.createRow(i);
            }
            for (int j = 0; j < colMap.size(); j++) {
                Field f = colMap.get(j).getField();
//                String colFieldName = f.getName();
                Object colObj = null;
                f.setAccessible(true);
                try {
                    colObj = f.get(curObj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                row.createCell(j).setCellValue(colObj.toString());
            }

        }

    }


}
