package com.datable.excelbox.core.read;

import com.datable.excelbox.core.BaseTest;
import com.datable.excelbox.core.ExcelFactory;
import com.datable.excelbox.core.ExcelParser;
import com.datable.excelbox.core.model.MonarchsOf3KingdomsModel;
import com.datable.excelbox.core.util.FileUtil;
import com.datable.excelbox.core.util.StrUtil;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * excel解析器 基本功能测试
 *
 * @author jinyaoo
 */
public class ExcelParserBasicTest extends BaseTest {

//    final ExcelMapper mapper = mapperForExcel();

    @Test
    public void readXlsxAsListOfString() {

        String xlsxFile = BaseTest.SAMPLE_XLSX_LOCAL_PATH;
        String xlsxFileName = StrUtil.getFileNameFromPath(xlsxFile);
        InputStream in = FileUtil.getResourceAsInputStream(xlsxFile);

        ExcelParser parser = new ExcelFactory().createParser(in);

        List<List<String>> strList = parser.readAsListOfString();

        prettyPrintListListString(strList, xlsxFileName);
    }

    @Test
    public void readXlsxAsListOfObject() {

        String xlsxFile = BaseTest.SAMPLE_XLSX_LOCAL_PATH;
        String xlsxFileName = StrUtil.getFileNameFromPath(xlsxFile);
        InputStream in = FileUtil.getResourceAsInputStream(xlsxFile);

        ExcelParser parser = new ExcelFactory().createParser(in);

        List<MonarchsOf3KingdomsModel> objList = parser.readAsListOfObject(MonarchsOf3KingdomsModel.class);

        System.out.println(objList.get(0));
        assertEquals(8, objList.size());
    }


    @Test
    public void readXlsAsListOfString() {

        String xlsFile = BaseTest.SAMPLE_XLS_LOCAL_PATH;
        String xlsFileName = StrUtil.getFileNameFromPath(xlsFile);
        InputStream in = FileUtil.getResourceAsInputStream(xlsFile);

        ExcelParser parser = new ExcelFactory().createParser(in);

        List<List<String>> strList = parser.readAsListOfString();

        prettyPrintListListString(strList, xlsFileName);
    }

    @Test
    public void readXlsAsListOfObject() {

        String xlsFile = BaseTest.SAMPLE_XLS_LOCAL_PATH;
        String xlsFileName = StrUtil.getFileNameFromPath(xlsFile);
        InputStream in = FileUtil.getResourceAsInputStream(xlsFile);

        ExcelParser parser = new ExcelFactory().createParser(in);

        List<MonarchsOf3KingdomsModel> objList = parser.readAsListOfObject(MonarchsOf3KingdomsModel.class);

        System.out.println(objList.get(0));
        assertEquals(8, objList.size());
    }


    @Test
    public void readXlsxWithBlankAsListOfString() {

        String xlsxFile = "excel4test/workbook-with-blank.xlsx";
        String xlsxFileName = StrUtil.getFileNameFromPath(xlsxFile);
        InputStream in = FileUtil.getResourceAsInputStream(xlsxFile);

        ExcelParser parser = new ExcelFactory().createParser(in);

        List<List<String>> strList = parser.readAsListOfString();

        prettyPrintListListString(strList, xlsxFileName);
    }

}
