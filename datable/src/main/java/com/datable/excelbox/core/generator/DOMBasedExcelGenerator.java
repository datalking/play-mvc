package com.datable.excelbox.core.generator;

import com.datable.excelbox.core.support.SheetHeader;
import com.datable.excelbox.core.util.SheetDataUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 以DOM/Usermodel方式读取解析Excel
 * 基于poi的ss包，同时支持xlsx和xls
 */
public class DOMBasedExcelGenerator extends GeneratorBase {
    /**
     * 输出的excel内存中对应的Workbook对象
     */
    private Workbook workbook;

    private OutputStream outputStream;

    public DOMBasedExcelGenerator(OutputStream out, int features) {
        super(features);
        this.outputStream = out;
    }

    @Override
    public void writeListOfStringAsExcel(List<List<String>> data, String outputPath) {

    }

    @Override
    public void writeListOfObjectAsExcel(List<?> data, String outputPath) {

    }

    @Override
    public <T> void writeListOfObjectAsExcel(List<T> data, Class<T> clazz) {
        if (data == null) {
            throw new RuntimeException("data parameter for writeListOfObjectAsExcel() cannot be null.");
        }
        int headerRowCount = 1;
        String excelType = "xlsx";
        String sheetName = "Sheet1";
        OutputStream out;

        if (excelType.equals("xls")) {
            workbook = new HSSFWorkbook();
        } else {
            workbook = new XSSFWorkbook();
        }

        Class headerClazz;
        /// 若列表中无数据 todo 无数据时如何获取元素类型
        if (data.size() == 0) {
//            headerClazz = TypeUtil.getGenericTypeFromMethodParameter(list);
            headerClazz = clazz;
        } else {
            /// 若列表中有数据
            headerClazz = data.get(0).getClass();
        }
        // 创建表头信息
        SheetHeader sheetHeader = SheetDataUtil.createSheetHeaderFromAnnotatedClass(headerClazz);
        // 根据表头信息创建sheet
        SheetDataUtil.createOneSheetFromList(data, workbook, sheetName, headerRowCount, sheetHeader);
        try {
            workbook.write(outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void writeListDataAsExcelFromTemplateFile(List<?> data, String outputPath) {

    }
}
