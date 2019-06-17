package com.datable.excelbox.core.util;

import java.io.InputStream;

/**
 * 文件操作工具类
 *
 * @author jinyaoo
 */
public final class FileUtil {
    /**
     * 根据文件路径返回文件输入流
     *
     * @param fileName 文件路径
     */
    public static InputStream getResourceAsInputStream(String fileName) {

//        return Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        return FileUtil.class.getClassLoader().getResourceAsStream(fileName);
    }

}
