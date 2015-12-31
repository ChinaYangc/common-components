package com.fansz.pub.utils;

/**
 * Created by allan on 15/12/2.
 */
public final class FileTools {
    public static String getExtension(String fileName) {
        if (fileName == null || fileName.trim().length() == 0) {
            return "unknown";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }
}
