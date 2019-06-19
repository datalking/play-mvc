package com.datable.excelbox.core.write;

import com.datable.excelbox.core.BaseTest;
import com.datable.excelbox.core.ExcelFactory;
import com.datable.excelbox.core.ExcelGenerator;
import com.datable.excelbox.core.model.MonarchsOf3KingdomsModel;
import com.datable.excelbox.core.util.MockDataUtil;
import org.junit.Test;

import java.util.List;

/**
 * excel生成器 基本功能测试
 */
public class ExcelGeneratorBasicTest extends BaseTest {

    @Test
    public void writeListOfStringAsXlsx() {
        List<List<String>> strListList = MockDataUtil.mockListListString(8);
        ExcelGenerator generator = new ExcelFactory().createGenerator("/home/yaoo/Downloads/list-list-str.xlsx");
        generator.writeListOfStringAsExcel(strListList);
    }

    @Test
    public void writeListOfObjectAsXlsx() {

        List<MonarchsOf3KingdomsModel> objList = MockDataUtil.mockObjectListWithAllPropString(16);

        ExcelGenerator generator = new ExcelFactory().createGenerator("/home/yaoo/Downloads/field-all-str.xlsx");
//        generator.writeListOfObjectAsExcel(objList);
        generator.writeListOfObjectAsExcel(objList, MonarchsOf3KingdomsModel.class);

    }

    @Test
    public void writeListOfObjectAsEmptyXlsx() {

        List<MonarchsOf3KingdomsModel> objList = MockDataUtil.mockObjectListWithAllPropString(0);

        ExcelGenerator generator = new ExcelFactory().createGenerator("/home/yaoo/Downloads/empty-with-header.xlsx");
//        generator.writeListOfObjectAsExcel(objList);
        generator.writeListOfObjectAsExcel(objList, MonarchsOf3KingdomsModel.class);

    }
}
