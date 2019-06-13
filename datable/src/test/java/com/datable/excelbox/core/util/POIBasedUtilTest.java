package com.datable.excelbox.core.util;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.io.FileInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * POIBasedUtil Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>May 26, 2019</pre>
 */
public class POIBasedUtilTest {

    private String basePath = "excel4test/blank/";
    private String xlsx01 = basePath + "blankWorkbook.xlsx";
    private String xlsx02 = basePath + "blankWorkbookNameXLSActuallyXLSX.xls";
    private String xls01 = basePath + "blankWorkbook.xls";
    private String xls02 = basePath + "blankWorkbookNameXLSXActuallyXLS.xlsx";
    private String csvFile = "excel4test/monarchsOf3kingdoms.csv";

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * 测试excel文件的真实类型
     * Method: getExcelVersion(InputStream inputStream)
     */
    @Test
    public void testGetExcelVersion() throws Exception {

        assertEquals("xlsx", POIBasedUtil.getExcelVersion(FileUtil.getResourceAsInputStream(xlsx01)));
        assertEquals("xlsx", POIBasedUtil.getExcelVersion(FileUtil.getResourceAsInputStream(xlsx02)));
        assertEquals("xls", POIBasedUtil.getExcelVersion(FileUtil.getResourceAsInputStream(xls01)));
        assertEquals("xls", POIBasedUtil.getExcelVersion(FileUtil.getResourceAsInputStream(xls02)));

        assertNull(POIBasedUtil.getExcelVersion(FileUtil.getResourceAsInputStream(csvFile)));
    }


} 
