package com.fansz.orm.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.reflections.Reflections;

import com.fansz.orm.dao.DaoScanner;
import com.fansz.orm.dao.annotation.DAO;

/**
 * 扫描项目中的DAO
 */
public class DaoScannerImpl implements DaoScanner {

    // 扫描的包
    private String basePackage = "com.fansz";

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public List<Class<?>> findDaoClasses() {
        Reflections reflections = new Reflections(basePackage);
        return new ArrayList<Class<?>>(reflections.getTypesAnnotatedWith(DAO.class));
    }
}
