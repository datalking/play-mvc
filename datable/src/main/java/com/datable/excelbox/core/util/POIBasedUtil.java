package com.datable.excelbox.core.util;

import com.datable.excelbox.core.constant.NameConstant;
import org.apache.poi.poifs.filesystem.FileMagic;

import java.io.IOException;
import java.io.InputStream;

/**
 * 基于apache poi提供的api的常用方法
 *
 * @author jinyaoo
 */
public class POIBasedUtil {

    /**
     * 获取excel文件的真实类型，即判断是03格式还是07格式
     *
     * @param inputStream 输入excel文件流
     * @return xls或xlsx
     */
    public static String getExcelVersion(InputStream inputStream) {
        if (!inputStream.markSupported()) {
            return null;
        }
        // 利用poi从输入流获取magic，据此判断excel真实类型
        FileMagic fileMagic = null;
        try {
            fileMagic = FileMagic.valueOf(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (FileMagic.OLE2.equals(fileMagic)) {
            return NameConstant.XLS;
        }
        if (FileMagic.OOXML.equals(fileMagic)) {
            return NameConstant.XLSX;
        }

        return null;
    }

}
