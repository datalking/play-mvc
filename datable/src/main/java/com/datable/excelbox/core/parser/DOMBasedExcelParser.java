package com.datable.excelbox.core.parser;

import com.datable.excelbox.core.support.SheetHeader;
import com.datable.excelbox.core.util.POIBasedUtil;
import com.datable.excelbox.core.util.SheetDataUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 以DOM/Usermodel方式读取解析Excel
 * 基于poi的ss包，同时支持xlsx和xls
 *
 * @author jinyaoo
 */
public class DOMBasedExcelParser extends ParserBase {
    /**
     * 输入的excel内存中对应的Workbook对象
     */
    private Workbook wb;

    public DOMBasedExcelParser(InputStream inputStream, int parserFeatures) {
        super(parserFeatures);

        try {
            wb = WorkbookFactory.create(inputStream);
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }

    }

    public DOMBasedExcelParser(InputStream inputStream) {
        this(inputStream, 9999);
    }


    /**
     * 读取excel指定sheet的指定行范围的数据，返回字符串列表
     * 空行默认读取为空单元格
     *
     * @param sheetIndex    sheet索引，基于0
     * @param startRowIndex 起始行索引，基于0
     * @param endRowIndex   末尾行索引，基于0，输入值-1代表读取所有行
     * @return excel指定行数据的字符串列表
     */
    public List<List<String>> readAsListOfString(int sheetIndex,
                                                 int startRowIndex,
                                                 int endRowIndex) {
        List<List<String>> resultList = new ArrayList<>();
        Sheet sheet = wb.getSheetAt(sheetIndex);
        int rowIndexLast = sheet.getLastRowNum();

        /// 若是空sheet，则直接返回
        if (rowIndexLast == 0) {
            return resultList;
        }

        /// 若sheet非空，则逐行遍历
        if (endRowIndex == -1) {
            endRowIndex = rowIndexLast;
        }
        // 遍历范围内所有行，获取各单元格字符串
        for (int i = startRowIndex; i <= endRowIndex; i++) {

            List<String> curRowCellList = new ArrayList<>();
            Row row = sheet.getRow(i);

            /// 若是空行，则单独添加一个空字符串
            if (row == null) {
                curRowCellList.add("");
                resultList.add(curRowCellList);
                continue;
            }

            int cellIndexLast = row.getLastCellNum();
            // 遍历该行所有单元格
            for (int j = 0; j < cellIndexLast; j++) {
                Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String cellValStr = POIBasedUtil.getCellValueAsString(cell);

                curRowCellList.add(cellValStr);
            }

            resultList.add(curRowCellList);
        }


        SheetDataUtil.matrixDataToSameColumn(resultList);

        return resultList;
    }

    /**
     * 读取excel的内容，返回字符串列表
     * 默认只读取第一张sheet，从第一行读到最后一行
     * 默认空白格都返回空字符串""
     *
     * @return 字符串列表
     */
    @Override
    public List<List<String>> readAsListOfString() {
        return readAsListOfString(0, 0, -1);
    }

    /**
     * 读取excel指定sheet的指定行范围的数据，返回java bean模型对象列表
     *
     * @param sheetIndex    worksheet索引，基于0
     * @param startRowIndex 起始行索引，基于0
     * @param endRowIndex   末尾行索引，基于0，-1代表读取所有行
     * @param clazz         代表sheet列字段结构的模型类
     * @return excel指定行数据的对象列表
     */
    public <T> List<T> readAsListOfObject(int sheetIndex,
                                          int startRowIndex,
                                          int endRowIndex,
                                          Class<T> clazz) {

        if (clazz == null) {
            throw new RuntimeException("clazz cannot be null, it will be used to instantiate object.");
        }

        List<T> resultList = new ArrayList<>();

        Sheet sheet = wb.getSheetAt(sheetIndex);
        int rowIndexLast = sheet.getLastRowNum();

        /// 若是空sheet，则直接返回
        if (rowIndexLast == 0) {
            return resultList;
        }

        // 根据模型类确定属性与sheet列的映射关系
        SheetHeader sheetHeader = SheetDataUtil.getSheetHeaderFromAnnotatedClass(clazz);
        // 一般是表头行
        Row headerRow = sheet.getRow(startRowIndex);
        // 若order不存在，则根据表中列名重排序
        SheetDataUtil.sortSheetHeaderColumnByTitle(headerRow, sheetHeader);

        /// 若sheet非空，则逐行遍历
        if (endRowIndex == -1) {
            endRowIndex = rowIndexLast;
        }
        // 遍历范围内除表头行的所有行，每行生成一个对象
        for (int i = startRowIndex + 1; i <= endRowIndex; i++) {

            Row row = sheet.getRow(i);
            /// 若是空行，则单独添加一个空字符串
            if (row == null) {
//                curRowCellList.add(null);
//                resultList.add(curRowCellList);
                continue;
            }

            T curRowObj = (T) SheetDataUtil.createRowObjectFromRowData(row, sheetHeader);

            resultList.add(curRowObj);
        }

        return resultList;
    }

    @Override
    public <T> List<T> readAsListOfObject(Class<T> clazz) {
        return readAsListOfObject(0, 0, -1, clazz);
    }

    @Override
    public <T> List<T> readAsListOfObject() {
        return null;
    }

}
