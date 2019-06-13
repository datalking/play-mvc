package com.datable.excelbox.core.util;

/**
 * 字符串操作工具类
 *
 * @author jinyaoo
 */
public final class StrUtil {

    private StrUtil() {
    }

    /**
     * 从文件路径中获取文件名
     *
     * @param fileFullPath 仅支持linux风格的文件路径，如 /home/user/Downloads/a.xlsx
     * @return 文件名，如a.xlsx
     */
    public static String getFileNameFromPath(String fileFullPath) {
        String fileName = "";

        if (fileFullPath.indexOf("/") == -1) {
            fileName = fileFullPath;
        } else {
            String[] strArr = fileFullPath.split("/");
            fileName = strArr[strArr.length - 1];
        }

        return fileName;
    }

}
