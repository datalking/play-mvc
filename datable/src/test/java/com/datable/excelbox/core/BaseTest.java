package com.datable.excelbox.core;

//import junit.framework.TestCase;

import java.util.List;

/**
 * 测试类抽象类基类 提供测试文件地址和公用方法
 *
 * @author jinyaoo
 */
public abstract class BaseTest {

    protected final static String SAMPLE_XLSX_REMOTE_URL = "https://raw.githubusercontent.com/uptonking/assets/master/sheet4demo/monarchsOf3kingdoms.xlsx";
    protected final static String SAMPLE_XLSX_LOCAL_PATH = "excel4test/monarchsOf3kingdoms.xlsx";
    protected final static String SAMPLE_XLS_REMOTE_URL = "https://raw.githubusercontent.com/uptonking/assets/master/sheet4demo/%E4%B8%89%E5%9B%BD%E4%B8%BB%E8%A6%81%E5%90%9B%E4%B8%BB.xls";
    protected final static String SAMPLE_XLS_LOCAL_PATH = "excel4test/三国主要君主.xls";

    protected final ExcelFactory excelFactory = new ExcelFactory();

    protected ExcelParser createParser(ExcelFactory factory) {

        return null;
    }

    protected ExcelMapper mapperForExcel() {
        return new ExcelMapper();
    }

    protected String quoteStr(String str) {
        return '"' + str + '"';
    }


    protected void prettyPrintListListString(List<List<String>> list) {

        if (list != null && list.size() == 0) {
            System.out.println("该sheet为空");
        }

        for (int i = 0, rowNum = list.size(); i < rowNum; i++) {

            System.out.print("第" + (i + 1) + "行: ");
            List<String> curRow = list.get(i);

            for (int j = 0, colNum = curRow.size(); j < colNum; j++) {

                /// 最后一列不打印逗号
                if (j == colNum - 1) {
//                    System.out.print(curRow.get(j).toString());
                    System.out.print(curRow.get(j).toString() + ", ");
                } else {
                    System.out.print(curRow.get(j).toString() + ", ");
                }
            }

            System.out.println();
        }
        System.out.println();

    }

    protected void prettyPrintListListString(List<List<String>> list, String sheetName) {
        System.out.println("当前打印的sheet是: " + sheetName);
        prettyPrintListListString(list);
    }


}


