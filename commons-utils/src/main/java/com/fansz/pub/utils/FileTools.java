package com.fansz.pub.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by allan on 15/12/2.
 */
public final class FileTools {
    private final static Logger logger = LoggerFactory.getLogger(FileTools.class);

    public static String getExtension(String fileName) {
        if (fileName == null || fileName.trim().length() == 0) {
            return "unknown";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    public static String readStringFromStream(InputStream is) {
        try {
            return IOUtils.toString(is, Charset.forName("utf-8"));
        } catch (IOException e) {
            logger.error(String.format("读取文件时发生异常"), e);
        } finally {
            if (is != null) {
                IOUtils.closeQuietly(is);
            }
        }
        return null;
    }

    public static String readStringFromFile(String file) {
        try {
            return readStringFromStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            logger.error(String.format("文件不存在"), e);
        }
        return null;
    }

    public static boolean writeString(String json, String path) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(path);
            IOUtils.write(json, os, Charset.forName("utf-8"));
            return true;
        } catch (Exception e) {
            logger.error(String.format("写文件时发生异常"), e);
        } finally {
            if (os != null) {
                IOUtils.closeQuietly(os);
            }
        }
        return false;
    }
}
