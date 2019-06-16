package com.datable.excelbox.core.write;

import com.datable.excelbox.core.BaseTest;
import com.datable.excelbox.core.ExcelFactory;
import com.datable.excelbox.core.ExcelGenerator;
import com.datable.excelbox.core.model.MonarchsOf3KingdomsModel;
import com.datable.excelbox.core.util.MockDataUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * excel生成器 基本功能测试
 */
public class ExcelGeneratorBasicTest extends BaseTest {

    @Test
    public void writeListOfStringAsXlsx() {


    }

    @Test
    public void writeListOfObjectAsXlsx() {

        List<MonarchsOf3KingdomsModel> objList = MockDataUtil.mockAllStringPropObjectList();

        ExcelGenerator generator = new ExcelFactory().createGenerator("/home/yaoo/Downloads/a.xlsx");
//        generator.writeListOfObjectAsExcel(objList,MonarchsOf3KingdomsModel.class);
        generator.writeListOfObjectAsExcel(objList);


    }

}
