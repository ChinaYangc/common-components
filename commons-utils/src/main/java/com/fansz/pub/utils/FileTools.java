package com.fansz.pub.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
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
}
