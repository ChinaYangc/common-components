package com.fansz.orm.dao;

import java.util.List;

/**
 * 扫描项目中的DAO
 */
public interface DaoScanner {
    /**
     * 扫描项目中的DAO类并返回
     *
     * @return DAO类
     */
    List<Class<?>> findDaoClasses();
}
